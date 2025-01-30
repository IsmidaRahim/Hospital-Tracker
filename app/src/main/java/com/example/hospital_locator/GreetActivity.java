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

public class GreetActivity extends AppCompatActivity {
    private TextView greetTextView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greet);

        greetTextView = findViewById(R.id.txtUser);

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Get the user ID

            // Reference to the user's data in Firebase Realtime Database
            databaseReference = FirebaseDatabase.getInstance("https://hospitallocator-b7712-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(userId);

            // Fetch user data from the database
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get the full name from the database
                        String userName = dataSnapshot.child("name").getValue(String.class);
                        if (userName != null && !userName.isEmpty()) {
                            greetTextView.setText("Hello, " + userName + "!");
                        } else {
                            greetTextView.setText("Hello, User!");
                        }
                    } else {
                        greetTextView.setText("User data not found.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(GreetActivity.this, "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User is not logged in
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }
}
