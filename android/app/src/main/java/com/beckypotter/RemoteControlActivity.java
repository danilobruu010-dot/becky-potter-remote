package com.beckypotter;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RemoteControlActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private ImageView remoteScreenImageView;
    private TextView statusTextView;
    private FirebaseFirestore db;
    private GestureDetectorCompat gestureDetector;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);

        sessionId = getIntent().getStringExtra("sessionId");
        db = FirebaseFirestore.getInstance();

        remoteScreenImageView = findViewById(R.id.remote_screen_image);
        statusTextView = findViewById(R.id.status_text);

        gestureDetector = new GestureDetectorCompat(this, this);

        remoteScreenImageView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        loadRemoteScreen();
    }

    private void loadRemoteScreen() {
        if (sessionId == null) {
            statusTextView.setText("Status: No session");
            return;
        }

        db.collection("sessions")
                .document(sessionId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        statusTextView.setText("Status: Error loading screen");
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String screenData = snapshot.getString("screenImage");
                        if (screenData != null) {
                            statusTextView.setText("Status: Connected");
                        }
                    }
                });
    }

    @Override
    public boolean onDown(MotionEvent e) { return true; }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        sendGestureEvent("tap", e.getX(), e.getY());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        sendGestureEvent("scroll", distanceX, distanceY);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        sendGestureEvent("longPress", e.getX(), e.getY());
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        sendGestureEvent("fling", velocityX, velocityY);
        return true;
    }

    private void sendGestureEvent(String type, float x, float y) {
        if (sessionId == null) return;

        Map<String, Object> gesture = new HashMap<>();
        gesture.put("type", type);
        gesture.put("x", x);
        gesture.put("y", y);
        gesture.put("timestamp", System.currentTimeMillis());

        db.collection("sessions")
                .document(sessionId)
                .collection("gestures")
                .add(gesture)
                .addOnFailureListener(e -> Toast.makeText(this, "Error sending gesture: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
