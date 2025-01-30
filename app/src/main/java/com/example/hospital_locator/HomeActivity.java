package com.example.hospital_locator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up the ActionBarDrawerToggle for the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the map fragment and set the map ready callback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);  // Ensures onMapReady is called
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permissions and enable location layer
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void getCurrentLocation() {
        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted, do nothing
        }

        // Get last known location using FusedLocationClient
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Convert location to LatLng
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Move camera to the user's current location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                // Add a marker at the user's current location
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                // Send location data to Firebase (optional)
                sendLocationToFirebase(location.getLatitude(), location.getLongitude());

                // Add hospital markers (pass the latitude and longitude of the user)
                addHospitalMarkers(location.getLatitude(), location.getLongitude());
            } else {
                // Location is null, handle the case if needed
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendLocationToFirebase(double latitude, double longitude) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("location");
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            userRef.setValue(locationData);
        }
    }

    private void addHospitalMarkers(double userLat, double userLon) {
        // List of hospital locations and their names
        LatLng[] hospitals = {
                new LatLng(6.433516657416993, 100.18737037723855),  // KPJ PERLIS SPECIALIST HOSPITAL
                new LatLng(6.4417765745716755, 100.19365874022147),  // Tuanku Fauziah Hospital, Kangar
                new LatLng(6.433350699476513, 100.2720002181569),  // Arau Health Clinic
                new LatLng(6.443905626666994, 100.19220115199528),  // Sistem Hospital Awasan Taraf
                new LatLng(6.252529711757229, 100.61021699802158),  // Kuala Nerang Hospital
                new LatLng(5.679637246269298, 100.92889061854751),  // Baling Hospital
                new LatLng(5.429859794649547, 101.1289944826795),  // Gerik Hospital
                new LatLng(5.427065599375521, 100.32121524404789),  // Gleneagles Hospital Penang
                new LatLng(5.67366155534453, 100.51468787473209),  // Pantai Hospital Sungai Petani
                new LatLng(5.683483646114019, 100.49687800780609)   // Pantai Hospital Laguna Merbok
        };

        String[] hospitalNames = {
                "KPJ PERLIS SPECIALIST HOSPITAL",
                "Tuanku Fauziah Hospital, Kangar",
                "Arau Health Clinic",
                "Sistem Hospital Awasan Taraf",
                "Kuala Nerang Hospital",
                "Baling Hospital",
                "Gerik Hospital",
                "Gleneagles Hospital Penang",
                "Pantai Hospital Sungai Petani",
                "Pantai Hospital Laguna Merbok"
        };

        // Add markers for the hospitals and set info window
        for (int i = 0; i < hospitals.length; i++) {
            LatLng hospital = hospitals[i];
            String hospitalName = hospitalNames[i];

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(hospital)
                    .title(hospitalName));

            // Set custom info window adapter (optional)
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null; // Default info window
                }

                @Override
                public View getInfoContents(Marker marker) {
                    // Create a simple TextView for the hospital name
                    TextView textView = new TextView(HomeActivity.this);
                    textView.setText(marker.getTitle());
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                    textView.setTextSize(16);
                    return textView;
                }
            });

            // Set a listener for the marker click to move the camera to the hospital
            mMap.setOnMarkerClickListener(markers -> {
                LatLng hospitalLocation = markers.getPosition();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitalLocation, 15));
                return false; // Allow the default InfoWindow behavior
            });
        }
    }


    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch location
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle home action
            Toast.makeText(HomeActivity.this, "Home", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_profile) {
            // Navigate to ProfileActivity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_about) {
            // Handle about action
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
