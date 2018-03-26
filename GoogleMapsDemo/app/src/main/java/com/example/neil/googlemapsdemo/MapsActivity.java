package com.example.neil.googlemapsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.location.Location;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.*;

import com.example.neil.googlemapsdemo.mLatLng;

//TODO: Make user sending location update his location if moving and also cater for the fact that the location of the user receiving the location must update the map marker
//TODO: Test^^
//TODO: make notifications and alerts display name instead of userIDs
//TODO: publish location and nearby users
//TODO: Test nearby functionality with 2 phones


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private DrawerLayout mDrawerLayout;
    private Marker marker;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;

    private String currentUserId = "01";

    private Vector sendingLocTo = new Vector();
    private Vector nearby = new Vector();
    private Vector markers = new Vector();

    private boolean notified = false;

    private mLatLng trackingLatLng;
    private mLatLng myLatLng;

    private mLatLng emptyLatLng = new mLatLng(0,0);
    private Request emptyRequest = new Request("No one");

    private Switch publishSwitch;
    private ImageButton menuBtn;

    DatabaseReference myReqRef = database.getReference("Users/"+currentUserId+"/Request");
    DatabaseReference myReqTicketRef = database.getReference("Users/"+currentUserId+"/RequestTicket");
    DatabaseReference nearbyRef = database.getReference("Nearby");

    String reqPer[] = {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION","android.permission.INTERNET"};

    public void requestLocation(String fromUserId, String toUserId){
        writeToDb(fromUserId,emptyRequest);
        generateRequest(fromUserId,toUserId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, reqPer, 1);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mDrawerLayout = this.findViewById(R.id.drawer_layout);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("0", "BroTracker", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
        }

        gMap = googleMap;
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.getUiSettings().setCompassEnabled(true);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        gMap.setBuildingsEnabled(true);


        mDatabase = FirebaseDatabase.getInstance().getReference();

        writeToDb(currentUserId,emptyRequest);

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final Location location2 = location;
        if(location!= null){
            myLatLng = new mLatLng(location.getLatitude(),location.getLongitude());
        }else{
        }

        if(location!=null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(15).build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }



        myReqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request request = dataSnapshot.getValue(Request.class);
                if(!request.from.equals("No one"))genReqNotification(request);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        myReqTicketRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                trackingLatLng = dataSnapshot.getValue(RequestTicket.class).latLng;
                if (!dataSnapshot.getValue(RequestTicket.class).status.equals("Pending")) {
                    if (dataSnapshot.getValue(RequestTicket.class).status.equals("Approved"));
                        updateTrackingMarker(new LatLng(trackingLatLng.latitude, trackingLatLng.longitude), dataSnapshot.getValue(RequestTicket.class).receivingLocationFrom);
                    }
                    if (!notified) {
                        genResultNotification(dataSnapshot.getValue(RequestTicket.class).status, dataSnapshot.getValue(RequestTicket.class).receivingLocationFrom, dataSnapshot.getValue(RequestTicket.class).timeRemaining);
                        notified = true;
                    }
                }

            @Override
            public void onCancelled (DatabaseError databaseError){
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        nearbyRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if(location2!=null) {
                    String name = dataSnapshot.getKey();
                    mLatLng tempLatLng = dataSnapshot.getValue(mLatLng.class);
                    nearbyLatLng nLatLng = new nearbyLatLng(name, tempLatLng.latitude, tempLatLng.longitude);
                    if (!nearby.contains(nLatLng)) nearby.add(nLatLng);
                    if(publishSwitch.isChecked())nearbyMarkerCreate(nearby);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                if(location2!=null) {
                    dataSnapshot.getKey();
                    String name = dataSnapshot.getKey();
                    mLatLng tempLatLng = dataSnapshot.getValue(mLatLng.class);
                    nearbyLatLng nLatLng = new nearbyLatLng(name, tempLatLng.latitude, tempLatLng.longitude);
                    nearbyLatLng tempNearby;

                    for(int i = 0;i<nearby.size();i++) {
                        tempNearby = (nearbyLatLng)nearby.get(i);
                        if(tempNearby.userId.equals(name)) {
                            nearby.setElementAt(nLatLng,i);
                            break;
                        }
                    }
                    if(publishSwitch.isChecked())nearbyMarkerCreate(nearby);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(location2!=null) {
                    dataSnapshot.getKey();
                    String name = dataSnapshot.getKey();
                    mLatLng tempLatLng = dataSnapshot.getValue(mLatLng.class);
                    nearbyLatLng nLatLng = new nearbyLatLng(name, tempLatLng.latitude, tempLatLng.longitude);
                    nearbyLatLng tempNearby;
                    for(int i = 0;i<nearby.size();i++) {
                        tempNearby = (nearbyLatLng)nearby.get(i);
                        if(tempNearby.userId.equals(dataSnapshot.getKey())) {
                            nearby.removeElementAt(i);
                            break;
                        }
                    }
                    if(publishSwitch.isChecked())nearbyMarkerCreate(nearby);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        publishSwitch = findViewById(R.id.publishSwitch);

        publishSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(publishSwitch.isChecked()) {
                    publishLocation(currentUserId, myLatLng);
                    nearbyMarkerCreate(nearby);
                }else if(!publishSwitch.isChecked()){
                    mDatabase.child("Nearby").child(currentUserId).removeValue();
                    removeNearbyMarkers();
                }

            }
        });

        menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        if(menuItem.getTitle().equals("Friends")) {
                            Intent startMenuIntent = new Intent(MapsActivity.this, Search.class);
                            startActivity(startMenuIntent);
                        }
                        return true;
                    }
                });
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location){
            if(location!=null&&publishSwitch.isChecked()){
                myLatLng = new mLatLng(location.getLatitude(),location.getLongitude());
                publishLocation(currentUserId,myLatLng);
                if(!sendingLocTo.isEmpty())updateReqTicketLoc(myLatLng);
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

    public void publishLocation(String currentUserId, mLatLng latLng) {
        mDatabase.child("Nearby").child(currentUserId).setValue(latLng);
    }

    private void updateTrackingMarker(LatLng latLng, String from){
        gMap.clear();
        marker =  gMap.addMarker(new MarkerOptions().position(latLng).title(from));
    }

    private void removeMarker(){
        gMap.clear();
    }

    private void generateRequest(String FromUserId, String toUserId){
        Request request = new Request(FromUserId);
        mDatabase.child("Users").child(toUserId).child("Request").setValue(request);
        createRequestTicket("Pending",toUserId);
    }

    private void genReqNotification(Request request){
        final Request fRequest = request;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Request").setMessage(request.from+" is requesting to view your location")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showNumberPickerDialog(fRequest);
                    }
                });
        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mDatabase.child("Users").child(currentUserId).child("Request").child("from").setValue("No one");
                requestTicketHandler(fRequest.from,"Declined",0,currentUserId,emptyLatLng);
            }
        });
        AlertDialog dialog = builder.create();
        if(!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void requestTicketHandler(String from, String status,int timeout, String receivingLocationFrom, mLatLng latLng){
        if(status.equals("Declined")){
            RequestTicket rTicket = new RequestTicket(status,receivingLocationFrom);
            mDatabase.child("Users").child(from).child("RequestTicket").setValue(rTicket);
        }else if(status.equals("Approved")){
            RequestTicket rTicket = new RequestTicket(status,receivingLocationFrom,latLng,timeout);
            mDatabase.child("Users").child(from).child("RequestTicket").setValue(rTicket);
        }
    }

    private void showNumberPickerDialog(Request request){
        final NumberPicker numberPicker = new NumberPicker(this);
        final Request fRequest = request;

        numberPicker.setMaxValue(60);
        numberPicker.setMinValue(1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(numberPicker);
        builder.setTitle("Location Request").setMessage("Please choose the ammount of time (in minutes) you wish to share your location with this user for")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        mDatabase.child("Users").child(currentUserId).child("Request").child("from").setValue("No one");
                        sendingLocTo.add(fRequest.from);
                        requestTicketHandler(fRequest.from,"Approved",numberPicker.getValue()*60,currentUserId,myLatLng);

                        new CountDownTimer(numberPicker.getValue()*60000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                mDatabase.child("Users").child(fRequest.from).child("RequestTicket").child("timeRemaining").setValue((int)millisUntilFinished/1000);
                            }

                            public void onFinish() {
                                removeRequestTicket(fRequest.from);
                                sendingLocTo.remove(fRequest.from);
                                notifySelfLocTerm(fRequest.from);
                            }
                        }.start();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createRequestTicket(String status,String receiveFrom){
        RequestTicket rTicket = new RequestTicket(status,receiveFrom);
        mDatabase.child("Users").child(currentUserId).child("RequestTicket").setValue(rTicket);
    }

    private void removeRequestTicket(String userId){
        RequestTicket emptyTicket = new RequestTicket("Pending","EmptyTicket");
        mDatabase.child("Users").child(userId).child("RequestTicket").setValue(emptyTicket);
        removeMarker();
    }

    private void genResultNotification(String status, final String receivingFrom, int timeout){
        if (!status.equals("Pending")) {
            final String receivingLocationFrom = receivingFrom;
            if(status.equals("Approved")) {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"0")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Location Request")
                        .setContentText(receivingFrom + " has " + status + " your location request")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                new CountDownTimer(timeout*1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        mDatabase.child("Users").child(currentUserId).child("RequestTicket").child("timeRemaining").setValue(Math.ceil(millisUntilFinished/1000));
                    }

                    public void onFinish() {
                        removeRequestTicket(currentUserId);
                        notifyLocTerm(receivingFrom);
                    }
                }.start();

                updateTrackingMarker(new LatLng(trackingLatLng.latitude,trackingLatLng.longitude), receivingLocationFrom);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, mBuilder.build());

            }else if(status.equals("Declined")){
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "BroTracker")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Location Request")
                        .setContentText(receivingLocationFrom + " has " + status + " your location request")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);


                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, mBuilder.build());

                removeRequestTicket(currentUserId);
            }
        }
    }

    private void updateReqTicketLoc(mLatLng latLng){
        for(int i = 0;i<sendingLocTo.size();i++) {
            mDatabase.child("Users").child((String)sendingLocTo.elementAt(i)).child("RequestTicket").child("latLng").setValue(latLng);
        }
    }

    private void notifyLocTerm(String receivingFrom){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Location Termination")
                .setContentText(receivingFrom + " has stopped sharing their location with you")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, mBuilder.build());
    }

    private void notifySelfLocTerm(String receivingFrom){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Location Termination")
                .setContentText("You have stopped sharing your location with "+receivingFrom)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, mBuilder.build());
    }

    private void writeToDb(String userId, Request request) {
        mDatabase.child("Users").child(userId).child("Request").setValue(request);
        RequestTicket emptyTicket = new RequestTicket("Pending","EmptyTicket");
        mDatabase.child("Users").child(userId).child("RequestTicket").setValue(emptyTicket);
    }

    private void nearbyMarkerCreate (Vector nearby){
        gMap.clear();
        LatLng latLng;
        nearbyLatLng nearbyTemp;
        if(!nearby.isEmpty()) {
            for (int i = 0; i < nearby.size(); i++) {
                nearbyTemp = (nearbyLatLng)nearby.get(i);
                latLng = new LatLng(nearbyTemp.latitude, nearbyTemp.longitude);
                if (latLng.latitude - myLatLng.latitude <= 1 && latLng.longitude - myLatLng.longitude <= 1 && latLng.latitude - myLatLng.latitude >= -1 &&latLng.longitude - myLatLng.longitude >= -1) {
                    if(!nearbyTemp.userId.equals(currentUserId)&&!nearbyTemp.userId.equals("00")) {
                        marker = gMap.addMarker(new MarkerOptions().position(latLng).title(nearbyTemp.userId));
                        markers.add(marker);
                    }
                }
            }
        }
    }

    private void removeNearbyMarkers(){
        Marker tempMarker;
        for(int i=markers.size()-1;i>=0;i--){
            tempMarker = (Marker)markers.get(i);
            tempMarker.remove();
            markers.removeElementAt(i);
        }
    }
}