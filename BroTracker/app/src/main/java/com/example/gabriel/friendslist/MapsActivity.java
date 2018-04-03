package com.example.gabriel.friendslist;

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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.*;

import com.example.gabriel.friendslist.mLatLng;

//TODO: Make user sending location update his location if moving and also cater for the fact that the location of the user receiving the location must update the map marker
//TODO: Test^^
//TODO: make notifications and alerts display name instead of userIDs

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private DrawerLayout mDrawerLayout;
    private Marker marker;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mMyUserDatabase;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mDatabase;
    private DatabaseReference myReqRef;
    private DatabaseReference myReqTicketRef;
    private DatabaseReference nearbyRef;


    private FirebaseAuth mAuth;

    private String currentUserId;

    private Vector sendingLocTo = new Vector();
    private Vector nearby = new Vector();
    private Vector markers = new Vector();

    private boolean notified = false;
    private boolean showAlertDialog = true;

    private mLatLng trackingLatLng;
    private mLatLng myLatLng;

    private mLatLng emptyLatLng = new mLatLng(0,0);
    private Request emptyRequest = new Request("No one");

    private Switch publishSwitch;
    private ImageButton menuBtn;

    String reqPer[] = {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION","android.permission.INTERNET"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        mMyUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("userName");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        myReqRef = database.getReference("Users/"+currentUserId+"/Request");
        myReqTicketRef = database.getReference("Users/"+currentUserId+"/RequestTicket");
        nearbyRef = database.getReference("Nearby");

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
                if(!request.from.equals("No one")){
                    if(showAlertDialog){
                        showAlertDialog = false;
                        genReqNotification(request);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        myReqTicketRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                final RequestTicket rt = dataSnapshot.getValue(RequestTicket.class);
                trackingLatLng = rt.latLng;
                if (!rt.status.equals("Pending")) {

                    mUserDatabase.child(rt.receivingLocationFrom).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String userName = dataSnapshot.child("userName").getValue().toString();

                            if (!notified&&rt.status.equals("Approved")&&rt.timeRemaining!=0) {
                                Toast.makeText(getApplicationContext(), userName+" has "+rt.status+" your location request for "+rt.timeRemaining+" seconds", Toast.LENGTH_LONG).show();;
                                notified = true;
                            }

                            if (rt.status.equals("Approved")) {
                                updateTrackingMarker(new LatLng(trackingLatLng.latitude, trackingLatLng.longitude), rt.receivingLocationFrom);
                                if (rt.timeRemaining == 1) {
                                    Toast.makeText(getApplicationContext(), userName + " has stopped sharing their location with you", Toast.LENGTH_LONG).show();
                                    notified = false;
                                    removeRequestTicket(currentUserId);
                                }
                            }else if(rt.status.equals("Declined")) {
                                Toast.makeText(getApplicationContext(), userName+" has "+rt.status+" your location request", Toast.LENGTH_LONG).show();
                                removeRequestTicket(currentUserId);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

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
                            Intent startMenuIntent = new Intent(MapsActivity.this, FriendsList.class);
                            startActivity(startMenuIntent);
                        }
                        else if(menuItem.getTitle().equals("Requests")) {
                            startActivity(new Intent(MapsActivity.this, FriendsRequest.class));
                        }
                        else if(menuItem.getTitle().equals("Log Out")) {
                            mAuth.signOut();
                            finish();
                            startActivity(new Intent(MapsActivity.this, MainActivity.class));
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

    private void updateTrackingMarker(final LatLng latLng, String from){
        gMap.clear();

        marker =  gMap.addMarker(new MarkerOptions().position(latLng));

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

        mUserDatabase.child("Request").child("from").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.getValue().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Location Request").setMessage(userName+" is requesting to view your location")
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
                dialog.setCanceledOnTouchOutside(false);
                showAlertDialog = true;
                if(!dialog.isShowing()) {
                    dialog.show();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

                        new CountDownTimer(/*numberPicker.getValue()*60000*/10000   , 1000) {

                            public void onTick(long millisUntilFinished) {
                                mDatabase.child("Users").child(fRequest.from).child("RequestTicket").child("timeRemaining").setValue((int)millisUntilFinished/1000);
                            }

                            public void onFinish() {
                                removeRequestTicket(fRequest.from);
                                sendingLocTo.remove(fRequest.from);

                                mUserDatabase.child(fRequest.from).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String userName = dataSnapshot.child("userName").getValue().toString();

                                        Toast.makeText(getApplicationContext(), "You have stopped sharing your location with " + userName, Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }.start();
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                genReqNotification(fRequest);
            }});

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if(!dialog.isShowing()) {
            dialog.show();
        }
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

    private void updateReqTicketLoc(mLatLng latLng){
        for(int i = 0;i<sendingLocTo.size();i++) {
            mDatabase.child("Users").child((String)sendingLocTo.elementAt(i)).child("RequestTicket").child("latLng").setValue(latLng);
        }
    }

    private void writeToDb(String userId, Request request) {
        mDatabase.child("Users").child(userId).child("Request").setValue(request);
        RequestTicket emptyTicket = new RequestTicket("Pending","EmptyTicket");
        mDatabase.child("Users").child(userId).child("RequestTicket").setValue(emptyTicket);
    }

    private void nearbyMarkerCreate (Vector nearby){
        gMap.clear();
        //LatLng latLng;
        nearbyLatLng nearbyTemp;
        if(!nearby.isEmpty()) {
            for (int i = 0; i < nearby.size(); i++) {
                nearbyTemp = (nearbyLatLng)nearby.get(i);
                final LatLng latLng = new LatLng(nearbyTemp.latitude, nearbyTemp.longitude);
                if (latLng.latitude - myLatLng.latitude <= 1 && latLng.longitude - myLatLng.longitude <= 1 && latLng.latitude - myLatLng.latitude >= -1 &&latLng.longitude - myLatLng.longitude >= -1) {
                    if(!nearbyTemp.userId.equals(currentUserId)&&!nearbyTemp.userId.equals("00")) {

                        mUserDatabase.child(nearbyTemp.userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String userName = dataSnapshot.child("userName").getValue().toString();

                                marker = gMap.addMarker(new MarkerOptions().position(latLng).title(userName));

                                markers.add(marker);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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

    public void requestLocation(String fromUserId, String toUserId){
        generateRequest(fromUserId,toUserId);
    }

}