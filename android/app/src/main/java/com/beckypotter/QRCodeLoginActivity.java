package com.beckypotter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.beckypotter.utils.CryptoUtil;

public class QRCodeLoginActivity extends AppCompatActivity {

    private Button scanQRButton, backButton;
    private EditText pinEditText;
    private FirebaseFirestore db;
    private IntentIntegrator qrScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_login);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        scanQRButton = findViewById(R.id.scan_qr_button);
        pinEditText = findViewById(R.id.pin_input);
        backButton = findViewById(R.id.back_button);

        // QR Code Scanner
        qrScanner = new IntentIntegrator(this);
        qrScanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        qrScanner.setPrompt("Scan QR Code");
        qrScanner.setCameraId(0);
        qrScanner.setBeepEnabled(true);
        qrScanner.setBarcodeImageEnabled(true);

        // Scan button listener
        scanQRButton.setOnClickListener(v -> qrScanner.initiateScan());

        // Back button listener
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String qrContent = result.getContents();
                String pin = pinEditText.getText().toString().trim();

                if (pin.isEmpty()) {
                    pinEditText.setError("PIN is required");
                    return;
                }

                // Verify QR Code and PIN
                verifyQRCodeAndPIN(qrContent, pin);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void verifyQRCodeAndPIN(String qrContent, String pin) {
        // QR Content format: deviceId|sessionToken|timestamp
        String[] parts = qrContent.split("\\|");

        if (parts.length != 3) {
            Toast.makeText(this, "Invalid QR Code format", Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = parts[0];
        String sessionToken = parts[1];
        long timestamp = Long.parseLong(parts[2]);

        // Check if session is still valid (within 5 minutes)
        if (System.currentTimeMillis() - timestamp > 300000) {
            Toast.makeText(this, "QR Code expired", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify PIN in Firestore
        db.collection("sessions")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String storedPin = documentSnapshot.getString("pin");
                        String storedToken = documentSnapshot.getString("token");

                        if (storedPin != null && storedPin.equals(pin) && 
                            storedToken != null && storedToken.equals(sessionToken)) {
                            
                            // Login successful
                            Toast.makeText(QRCodeLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(QRCodeLoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(QRCodeLoginActivity.this, "Invalid PIN or token", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(QRCodeLoginActivity.this, "Session not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(QRCodeLoginActivity.this, 
                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
