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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView textRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textRegister = findViewById(R.id.textRegister);

        // Login button click listener
        btnLogin.setOnClickListener(view -> loginUser());

        // Register text click listener
        textRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid(); // Firebase user ID
                            final String[] username = {""}; // Initialize username as empty

                            // Reference to the user's data in Firebase Realtime Database
                            databaseReference = FirebaseDatabase.getInstance("https://hospitallocator-b7712-default-rtdb.asia-southeast1.firebasedatabase.app")
                                    .getReference("users").child(userId);

                            // Fetch user data from Realtime Database
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Get the full name from the database
                                        String userName = dataSnapshot.child("name").getValue(String.class);
                                        if (userName != null && !userName.isEmpty()) {
                                            username[0] = "Hello, " + userName + "!";
                                        } else {
                                            username[0] = "Hello, User!";
                                        }
                                    } else {
                                        username[0] = "User data not found.";
                                    }

                                    // Redirect to HomeActivity with user info
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.putExtra("userId", userId);
                                    intent.putExtra("username", username[0]); // Pass the username
                                    startActivity(intent);
                                    finish();

                                    // Display welcome message
                                    Toast.makeText(LoginActivity.this,  username[0], Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(LoginActivity.this, "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        // Sign-in fails
                        Toast.makeText(LoginActivity.this, "Login failed! Check your credentials", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Login error: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
