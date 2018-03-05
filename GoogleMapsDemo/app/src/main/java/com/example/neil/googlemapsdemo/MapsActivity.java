package com.example.neil.googlemapsdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference
        ;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    String reqPer[] = {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION","android.permission.INTERNET"};

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference mDatabase;
    String userId = "01";
    String name = "Neil Borg";
    String username = "nzneil2";
    String password = "1234";
    String email = "test@gmail.com";
    Request EmptyRequest = new Request(0,"None");
    private LatLng mylatLng;

    DatabaseReference myLocRef = database.getReference("Users/"+userId+"/Location");




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, reqPer, 1);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mylatLng = new LatLng(location.getLatitude(),location.getLongitude());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        writeNewUser(userId,name,email,username,password,EmptyRequest,mylatLng);

        LatLng Uom= new LatLng(35.902175, 14.483749);
        mMap.addMarker(new MarkerOptions().position(Uom).title("University of Malta"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Uom));
        String toUserId = "02";

        generateRequest(20,userId,toUserId);

        myLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request request = dataSnapshot.getValue(Request.class);
                System.out.println(request);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }


    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location){
            if(location!=null){
                mylatLng = new LatLng(location.getLatitude(),location.getLongitude());
                updateLocation(userId,mylatLng);
            }
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
    };

    //private ValueEventListener postListener;



    private void writeNewUser(String userId, String name, String email,String username,String password,Request request, LatLng latlng) {

        User user = new User(name, username, email, password);

        mDatabase.child("Users").child(userId).child("Details").setValue(user);
        mDatabase.child("Users").child(userId).child("Request").setValue(request);
        mDatabase.child("Users").child(userId).child("Location").setValue(latlng);

    }

    private void updateLocation(String userId, LatLng latlng) {
        mDatabase.child("Users").child(userId).child("Location").setValue(latlng);
    }

    public void generateRequest(int timeOut, String userId, String toUserId){
        Request request = new Request(timeOut,"From: "+userId);

        mDatabase.child("Users").child(toUserId).child("Request").setValue(request);
    }


    //get uid of currently logged in user

    //ask firebase to send notification to this user if there is a pending request

    //get uid of user asking for location permission

    //display a notification of the user and a button to allow/deny permission



}


