package com.beckypotter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.beckypotter.services.ScreenCaptureService;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ScreenShareActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MEDIA_PROJECTION = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Button startSharingButton, stopSharingButton;
    private ImageView previewImageView;
    private TextView statusTextView;
    private MediaProjectionManager mediaProjectionManager;
    private FirebaseFirestore db;
    private boolean isSharing = false;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_share);

        sessionId = getIntent().getStringExtra("sessionId");
        db = FirebaseFirestore.getInstance();

        startSharingButton = findViewById(R.id.start_sharing_button);
        stopSharingButton = findViewById(R.id.stop_sharing_button);
        previewImageView = findViewById(R.id.preview_image);
        statusTextView = findViewById(R.id.status_text);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        startSharingButton.setOnClickListener(v -> startScreenSharing());
        stopSharingButton.setOnClickListener(v -> stopScreenSharing());

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void startScreenSharing() {
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                isSharing = true;
                statusTextView.setText("Status: Sharing Screen");
                startSharingButton.setEnabled(false);
                stopSharingButton.setEnabled(true);

                Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
                serviceIntent.putExtra("sessionId", sessionId);
                serviceIntent.putExtra("data", data);
                startService(serviceIntent);

                updateSessionStatus("sharing");
                Toast.makeText(this, "Screen sharing started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Screen sharing permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopScreenSharing() {
        isSharing = false;
        statusTextView.setText("Status: Idle");
        startSharingButton.setEnabled(true);
        stopSharingButton.setEnabled(false);

        stopService(new Intent(this, ScreenCaptureService.class));
        updateSessionStatus("idle");
        Toast.makeText(this, "Screen sharing stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateSessionStatus(String status) {
        if (sessionId == null) return;

        Map<String, Object> update = new HashMap<>();
        update.put("status", status);
        update.put("lastUpdate", System.currentTimeMillis());

        db.collection("sessions")
                .document(sessionId)
                .update(update)
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isSharing) {
            stopScreenSharing();
        }
    }
}
