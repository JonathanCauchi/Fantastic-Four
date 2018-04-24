package com.example.gabriel.friendslist;

import android.app.ProgressDialog;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profilePic;
    private TextView mProfileEmail, mProfileUsername;
    private Button mProfileSendReqBtn, mProfileDeclineBtn;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    private FirebaseStorage mStorage;
    private StorageReference storageRef;

    private FirebaseUser mCurrent_user;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private String mCurrent_state;

    private boolean buttonEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mStorage = FirebaseStorage.getInstance();

        storageRef = mStorage.getReferenceFromUrl("gs://final-project-25a7b.appspot.com");

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        profilePic = (ImageView) findViewById(R.id.profile_image);
        mProfileEmail = (TextView) findViewById(R.id.profile_email);
        mProfileUsername = (TextView) findViewById(R.id.profile_username);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please Wait while the user data is loaded");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();



        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String email = dataSnapshot.child("userEmail").getValue().toString();
                String userName = dataSnapshot.child("userName").getValue().toString();

                mProfileDeclineBtn.setVisibility(View.GONE);

                mProfileEmail.setText(email);
                mProfileUsername.setText(userName);

                storageRef.child(user_id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(ProfileActivity.this).load(uri).fit().centerCrop().into(profilePic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ProfileActivity.this, "Failed to get Profile Picture", Toast.LENGTH_SHORT).show();
                    }
                });

                //Friends List, Request Feature

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                if(buttonEnabled == true) {
                                    mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                }
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mProfileDeclineBtn.setText("Decline Friend Request");

                            }else if(req_type.equals("sent")){

                                mProfileDeclineBtn.setVisibility(View.GONE);
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                            }

                            mProgressDialog.dismiss();

                        } else {

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                        if(buttonEnabled == true) {
                                            mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                        }
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend");
                                        mProfileDeclineBtn.setText("Request Location");

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mProgressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setVisibility(View.GONE);
                mProfileDeclineBtn.setVisibility(View.GONE);

                //Send Friend Request

                if(mCurrent_state.equals("not_friends")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {

                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mCurrent_state = "req_sent";
                                        mProfileSendReqBtn.setText("Cancel Friend Request");
                                        mProfileDeclineBtn.setText("Decline Friend Request");
                                        mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                        mProfileDeclineBtn.setVisibility(View.GONE);

                                        Toast.makeText(ProfileActivity.this,"Request Sent Successfully.",Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }else{

                                Toast.makeText(ProfileActivity.this,"Failed to Send Request.",Toast.LENGTH_SHORT).show();

                            }

                            //mProfileSendReqBtn.setVisibility(View.VISIBLE);
                            //mProfileDeclineBtn.setVisibility(View.VISIBLE);

                        }
                    });

                }

                //Cancel Friend Request

                if(mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                    mProfileDeclineBtn.setVisibility(View.GONE);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    mProfileDeclineBtn.setText("Decline Friend Request");

                                }
                            });

                        }
                    });

                }

                //Accept Friend Request

                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                                    if(buttonEnabled == true) {
                                                        mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                                    }
                                                    mCurrent_state = "friends";
                                                    mProfileSendReqBtn.setText("Unfriend");
                                                    mProfileDeclineBtn.setText("Request Location");

                                                }
                                            });

                                        }
                                    });

                                }
                            });

                        }
                    });

                }

                //Unfriend

                if(mCurrent_state.equals("friends")){

                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                    mProfileDeclineBtn.setVisibility(View.GONE);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    mProfileDeclineBtn.setText("Decline Friend Request");

                                }
                            });

                        }
                    });


                }

            }
        });

        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setVisibility(View.GONE);
                mProfileDeclineBtn.setVisibility(View.GONE);

                if(mCurrent_state.equals("req_received")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                    mProfileDeclineBtn.setVisibility(View.GONE);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    mProfileDeclineBtn.setText("Decline Friend Request");

                                }
                            });

                        }
                    });

                }

                if(mCurrent_state.equals("friends")){

                    //mapsActivity.requestLocation("3nGM93BTtHfcnNEyJR8rNksAOMy2","dPJEAzGHDUXk9fdUTLcQUczkDq93");

                    mDatabase.child("Users").child(mCurrent_user_id).child("RequestTicket").child("receivingLocationFrom").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String receivedLocationFrom = dataSnapshot.getValue().toString();

                            if(receivedLocationFrom.equals("EmptyTicket")){

                                buttonEnabled = true;

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);

                            }
                            else{

                                buttonEnabled = false;

                                mProfileDeclineBtn.setVisibility(View.GONE);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mDatabase.child("Users").child(user_id).child("Request").setValue(new Request(mCurrent_user_id));

                    mDatabase.child("Users").child(mCurrent_user_id).child("RequestTicket").setValue(new RequestTicket("Pending",user_id));

                }


            }
        });

    }

}