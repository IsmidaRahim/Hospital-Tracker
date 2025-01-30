package com.example.hospital_locator;

import android.os.Bundle;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName;
    private TextView profileEmail;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Reference to the profile layout

        // Initialize the TextViews
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);

        // Get the current user from Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid(); // Firebase user ID

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
                            profileName.setText("Name: " + userName);
                        } else {
                            profileName.setText("Name not found");
                        }
                    } else {
                        profileName.setText("No data available");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });

            // Set the user's email in the TextView
            profileEmail.setText("Email: " + user.getEmail());

        } else {
            // Handle the case where the user is not logged in
            profileName.setText("No user data available");
            profileEmail.setText("Please log in.");
        }
    }
}
