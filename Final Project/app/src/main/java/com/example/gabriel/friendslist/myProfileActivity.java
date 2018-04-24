package com.example.gabriel.friendslist;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class myProfileActivity extends AppCompatActivity {

    private ImageView profilePic;
    private TextView profileName, profileEmail;
    private Button profileUpdate, changePassword;
    private FirebaseAuth mCurrentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private FirebaseStorage mStorage;
    private StorageReference storageRef;
    private String mCurrent_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        profilePic = findViewById(R.id.ivProfilePic);
        profileName = findViewById(R.id.tvProfileName);
        profileEmail = findViewById(R.id.tvProfileEmail);
        profileUpdate = findViewById(R.id.btnProfileUpdate);
        changePassword = findViewById(R.id.btnChangePassword);

        mAuth = FirebaseAuth.getInstance();

        mStorage = FirebaseStorage.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrent_user_id);

        storageRef = mStorage.getReferenceFromUrl("gs://final-project-25a7b.appspot.com");

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = dataSnapshot.child("userEmail").getValue().toString();
                String userName = dataSnapshot.child("userName").getValue().toString();

                storageRef.child(mCurrent_user_id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(myProfileActivity.this).load(uri).fit().centerCrop().into(profilePic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(myProfileActivity.this, "Failed to get Profile Picture", Toast.LENGTH_SHORT).show();
                    }
                });

                profileEmail.setText(email);
                profileName.setText(userName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(myProfileActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });

        profileUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(myProfileActivity.this, UpdateProfile.class));
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(myProfileActivity.this, UpdatePassword.class));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }
}