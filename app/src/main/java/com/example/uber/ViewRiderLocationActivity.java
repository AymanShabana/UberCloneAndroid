package com.example.uber;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ViewRiderLocationActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Request request;
    LocationManager locationManager;
    String provider;
    Location location;
    private Button backBtn;
    private Button acceptBtn;
    ArrayList<Marker> markers;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rider_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        request= (Request) getIntent().getSerializableExtra("request");
        Toast.makeText(this, request.getRequesterId(), Toast.LENGTH_SHORT).show();
        reference=FirebaseDatabase.getInstance().getReference().child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        location = locationManager.getLastKnownLocation(provider);
        RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.relativeLayout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMap.clear();
                LatLng yourLocation = new LatLng(location.getLatitude(),location.getLongitude());
                markers = new ArrayList<Marker>();
                markers.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(yourLocation).title("Your Location")));
                LatLng riderLocation = new LatLng(request.getLatitude(),request.getLongitude());
                markers.add(mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider Location")));
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 100;
                CameraUpdate cu;
                if(markers.size()==1){
                    cu = CameraUpdateFactory.newLatLng(markers.get(0).getPosition());
                }
                else{
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                }
                mMap.animateCamera(cu);
            }
        });
        backBtn = (Button)findViewById(R.id.back_btn);
        acceptBtn = (Button)findViewById(R.id.accept_request_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Requests").child(request.getRequestId()).child("driverId").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+request.getLatitude()+","+request.getLongitude()));
                startActivity(intent);
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

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(riderLocation));
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        LatLng yourLocation = new LatLng(location.getLatitude(),location.getLongitude());
        //reference.child("latitude").setValue(yourLocation.latitude);
        //reference.child("longitude").setValue(yourLocation.longitude);
        markers = new ArrayList<Marker>();
        markers.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(yourLocation).title("Your Location")));
        LatLng riderLocation = new LatLng(request.getLatitude(),request.getLongitude());
        markers.add(mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider Location")));
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
