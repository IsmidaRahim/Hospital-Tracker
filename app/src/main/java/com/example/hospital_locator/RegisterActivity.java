package com.example.hospital_locator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText editFullName, editEmailRegister, editPasswordRegister;
    private Button btnRegister;
    private TextView textLogin;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance("https://hospitallocator-b7712-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // Initialize UI components
        editFullName = findViewById(R.id.editFullName);
        editEmailRegister = findViewById(R.id.editEmailRegister);
        editPasswordRegister = findViewById(R.id.editPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        textLogin = findViewById(R.id.textLogin);

        // Register button click listener
        btnRegister.setOnClickListener(view -> registerUser());

        // Text to switch to login page
        textLogin.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmailRegister.getText().toString().trim();
        String password = editPasswordRegister.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register the user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save user data to Firebase Realtime Database
                        if (user != null) {
                            String userId = user.getUid();
                            saveUserData(userId, fullName, email);
                        }
                    } else {
                        // Registration failed
                        Log.e("Firebase", "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId, String fullName, String email) {
        // Create a map for the user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", fullName);
        userData.put("email", email);
        userData.put("password", editPasswordRegister.getText().toString().trim()); // ⚠️ Not secure, consider hashing in a real app

        // Save the user data to Firebase Realtime Database
        databaseReference.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "User data saved successfully");
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    // Redirect to LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error saving user data", e);
                    Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
