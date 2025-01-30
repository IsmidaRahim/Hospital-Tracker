package com.example.hospital_locator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AboutActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about); // Ensure this is the correct layout

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // This should find the toolbar by its ID
        setSupportActionBar(toolbar);

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up the toggle button
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle Navigation Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(AboutActivity.this, HomeActivity.class));
                finish(); // Close AboutActivity after navigation
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(AboutActivity.this, ProfileActivity.class));
                finish(); // Close AboutActivity after navigation
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set up the clickable website link
        TextView websiteLink = findViewById(R.id.website_link);
        websiteLink.setMovementMethod(LinkMovementMethod.getInstance());  // Enable clickable links
        websiteLink.setOnClickListener(v -> {
            // Open the website in a browser when clicked
            Uri webpage = Uri.parse("https://github.com/AhmadZahidi/Hospital-Tracker");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(intent);
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(AboutActivity.this, LoginActivity.class));
        finish(); // Finish AboutActivity so the user can't navigate back
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close drawer if open
        } else {
            super.onBackPressed(); // Handle the back button
        }
    }
}
