package com.example.googlemapandlocationdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE = 101;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap googleMap;
    private String searchLat;
    private String searchLong;

    //UI Components
    private LinearLayout activityLayout;
    private EditText latEditText;
    private EditText longEditText;
    private Button findButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latEditText = findViewById(R.id.lat_editText);
        longEditText = findViewById(R.id.long_editText);
        findButton = findViewById(R.id.find_btn);
        activityLayout = findViewById(R.id.mapLayout);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getUserPermission();

        // Find latitude and longitude that user entered
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLat = latEditText.getText().toString();
                searchLong = longEditText.getText().toString();

                // Check if text in Empty
                if (!searchLat.isEmpty() && !searchLong.isEmpty()) {
                    try {
                        LatLng latLng = new LatLng(Double.valueOf(searchLat), Double.valueOf(searchLong));
                        drawMarker(latLng, googleMap);
                    } catch (NumberFormatException e) {
                        // this is not a valid number
                        showSnakeBar(getResources().getString((R.string.not_valid)));
                    }
                } else {
                    showSnakeBar(getResources().getString( R.string.empty_text));
                }
            }
        });
    }

    /**
     * This Method Displays Snackbar
     */
    private void showSnakeBar(String message) {
        Snackbar snackbar = Snackbar.make(activityLayout,message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * This Method Asks User For Permission To Access Location
     */
    private void getUserPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show user alert with explanation to allow application access his position
                new AlertDialog.Builder(this)
                        .setTitle("Required Location Permission")
                        .setMessage("You have to give this permission to acess this feature")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE);
            }
        } else {
            // Permission has already been granted
            fetchLastLocation();
        }
    }

    /**
     * This Method Finds User's Current Location
     */
    private void fetchLastLocation() {
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Toast.makeText(MainActivity.this, currentLocation.getLatitude()
                            + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map_fragment);
                    supportMapFragment.getMapAsync(MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Draw user's location on map load
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        drawMarker(latLng, googleMap);
    }

    /**
     * This Method Draw Marker On The Map
     *
     * @param latLng
     * @param googleMap
     */
    private void drawMarker(LatLng latLng, GoogleMap googleMap) {
        MarkerOptions marker = new MarkerOptions().position(latLng).title("Hey There!")
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_marker_icon));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        googleMap.addMarker(marker);
    }

    /**
     * This Method Convert Marker Asset Image To Bitmap Image
     *
     * @param context
     * @param vectorDrawableResourceId
     * @return
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                }
                break;
        }
    }
}
