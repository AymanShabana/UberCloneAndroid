package com.example.uber.Adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uber.MainActivity;
import com.example.uber.Model.Request;
import com.example.uber.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> implements LocationListener {
    private Context context;
    private List<Request> requestList;

    private OnItemClickListener listener;

    private FirebaseUser firebaseUser;
    LocationManager locationManager;
    String provider;
    public RequestAdapter(Context context, List<Request> requestList, OnItemClickListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.distance_item,parent,false);
        return new RequestAdapter.ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Request request = requestList.get(position);
        holder.bind(request,listener);




    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    @Override
    public void onLocationChanged(Location location) {

        Collections.sort(requestList, new Comparator<Request>() {
            @Override
            public int compare(Request o1, Request o2) {
                return o1.getLongitude() > o2.getLongitude() ? -1 : (o1.getLatitude() < o2.getLatitude() ) ? 1 : 0;
            }

        });
    this.notifyDataSetChanged();

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


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView distance;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            distance=itemView.findViewById(R.id.distance);
        }
        @SuppressLint("MissingPermission")
        public void bind(final Request item, final OnItemClickListener listener){
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(provider, 400, 1, RequestAdapter.this);
            @SuppressLint("MissingPermission") final Location location = locationManager.getLastKnownLocation(provider);
            //Log.i("Location",location.toString());
            Location targetLocation = new Location("");
            targetLocation.setLatitude(item.getLatitude());
            targetLocation.setLongitude(item.getLongitude());
            //Log.i("Location",targetLocation.toString());

            //Log.i("TESTING",location.toString());
            distance.setText(targetLocation.distanceTo(location)+" m");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
    public interface OnItemClickListener{
        void onItemClick(Request request);
    }
}
