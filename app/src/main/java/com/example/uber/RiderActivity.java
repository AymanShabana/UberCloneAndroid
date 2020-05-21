package com.example.uber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uber.Model.Driver;
import com.example.uber.Model.Request;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    private Button requestUber;
    private TextView findingUber;
    private DatabaseReference mRootRef;
    private ProgressDialog pd;
    private String currentRequestId;
    private String currentDriverId;
    private boolean requestActive=false;
    ValueEventListener eventListener;
    ArrayList<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
            return;
        }
        mRootRef = FirebaseDatabase.getInstance().getReference();
        requestUber = (Button) findViewById(R.id.request_uber_button);
        findingUber = (TextView) findViewById(R.id.finding_uber_text);

        locationManager.requestLocationUpdates(provider, 400, 1, this);
        Location location = locationManager.getLastKnownLocation(provider);

        requestUber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(RiderActivity.this, "Uber requested", Toast.LENGTH_SHORT).show();
                processRequest();
            }
        });
        pd = new ProgressDialog(this);
        currentRequestId = "";
        checkIfRequestExists();

    }

    private void checkIfRequestExists() {
        mRootRef.child("Requests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Request request = snapshot.getValue(Request.class);
                    if(request.getRequesterId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        requestActive=true;
                        if(request.getDriverId().equals("default")){
                            findingUber.setText("Finding Uber driver...");
                        }
                        else{
                            findingUber.setText("Your driver in on their way");
                            requestUber.setEnabled(false);
                            requestUber.setVisibility(View.GONE);
                            currentDriverId = request.getDriverId();
                            trackDriver();
                        }
                        requestUber.setText("Cancel Ride");
                        currentRequestId=request.getRequestId();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void processRequest() {
        pd.setMessage("Hold on a second.");
        pd.show();
        if (!requestActive) {
            if (eventListener != null) {
                mRootRef.child("Requests").removeEventListener(eventListener);
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            currentRequestId = mRootRef.child("Requests").push().getKey();
            map.put("requestId", currentRequestId);
            map.put("requesterId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            map.put("driverId", "default");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location temp=locationManager.getLastKnownLocation(provider);

            map.put("latitude",temp.getLatitude());
            map.put("longitude",temp.getLongitude());
            mRootRef.child("Requests").child(currentRequestId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RiderActivity.this, "Uber requested", Toast.LENGTH_SHORT).show();
                        findingUber.setText("Finding Uber driver...");
                        requestUber.setText("Cancel Ride");
                        requestActive = true;
                        mRootRef.child("Requests").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    Request request = snapshot.getValue(Request.class);
                                    if(request.getRequestId().equals(currentRequestId) && !request.getDriverId().equals("default")){
                                        findingUber.setText("Your driver in on their way");
                                        requestUber.setEnabled(false);
                                        requestUber.setVisibility(View.GONE);
                                        currentDriverId = request.getDriverId();
                                        trackDriver();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        pd.dismiss();
                    }
                }
            });
        }
        else{
            eventListener = mRootRef.child("Requests").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Request request = snapshot.getValue(Request.class);
                        if(request.getRequestId().equals(currentRequestId)){
                            mRootRef.child("Requests").child(currentRequestId).removeValue();
                            findingUber.setText("");
                            requestUber.setText("Request Uber");
                            requestActive = false;
                            pd.dismiss();

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void trackDriver() {
        FirebaseDatabase.getInstance().getReference().child("Drivers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Driver driver = snapshot.getValue(Driver.class);
                    if(driver.getDriverId().equals(currentDriverId)){
                        Location targetLocation = new Location("");
                        targetLocation.setLatitude(driver.getLatitude());
                        targetLocation.setLongitude(driver.getLongitude());
                        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);
                        findingUber.setText("Your driver is "+targetLocation.distanceTo(location)+" meters away");
                        mMap.clear();
                        LatLng driverLocation = new LatLng(driver.getLatitude(),driver.getLongitude());
                        markers = new ArrayList<Marker>();
                        markers.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(driverLocation).title("Driver Location")));
                        LatLng riderLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        markers.add(mMap.addMarker(new MarkerOptions().position(riderLocation).title("Your Location")));
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (Marker marker : markers) {
                            builder.include(marker.getPosition());
                        }
                        LatLngBounds bounds = builder.build();
                        int padding = 100;
                        CameraUpdate cu;
                        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mMap.animateCamera(cu);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),10));
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("You are here."));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
