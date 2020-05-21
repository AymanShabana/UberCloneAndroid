package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.uber.Adapter.RequestAdapter;
import com.example.uber.Model.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity implements LocationListener {

    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;
    private List<Request> requestList;
    DatabaseReference reference;
    LocationManager locationManager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
            return;
        }
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        requestList=new ArrayList<Request>();

        reference=FirebaseDatabase.getInstance().getReference().child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.child("driverId").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        recyclerView.setLayoutManager(linearLayoutManager);

        requestAdapter = new RequestAdapter(getApplicationContext(), requestList, new RequestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Request request) {
                //Toast.makeText(ViewRequestsActivity.this, request.getRequesterId(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),ViewRiderLocationActivity.class);
                intent.putExtra("request",request);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(requestAdapter);

        updateList();

    }

    private void updateList() {
        FirebaseDatabase.getInstance().getReference().child("Requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Request request = snapshot.getValue(Request.class);
                    if(request.getDriverId().equals("default")){
                        requestList.add(request);
                    }
                }

                requestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        reference.child("latitude").setValue(location.getLatitude());
        reference.child("longitude").setValue(location.getLongitude());
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
