package com.example.gabriel.friendslist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class FriendsRequest extends AppCompatActivity {

    private ImageView mNoFriendRequestImage;

    private TextView mNoFriendRequestText;

    private ImageView image;

    private RecyclerView mFriendRequest;

    private DatabaseReference mUserDatabase;

    private DatabaseReference mFriendReqDatabase;

    private FirebaseAuth mAuth;

    private FirebaseStorage mStorage;

    private StorageReference storageRef;

    private String mCurrent_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        mAuth = FirebaseAuth.getInstance();

        mStorage = FirebaseStorage.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        storageRef = mStorage.getReferenceFromUrl("gs://final-project-25a7b.appspot.com");

        mNoFriendRequestImage = (ImageView) findViewById(R.id.no_friend_request_btn);
        mNoFriendRequestText = (TextView) findViewById(R.id.no_friend_request_text);

        image = (ImageView) findViewById(R.id.ivProfilePic);

        mNoFriendRequestImage.setVisibility(View.GONE);
        mNoFriendRequestText.setVisibility(View.GONE);

        mFriendRequest = (RecyclerView) findViewById(R.id.friend_requests);
        mFriendRequest.setHasFixedSize(true);
        mFriendRequest.setLayoutManager(new LinearLayoutManager(this));

        firebaseFriendRequest();

    }

    private void firebaseFriendRequest() {

        final FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.list_layout,
                FriendsViewHolder.class,
                mFriendReqDatabase


        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position) {

                final String friend_id = getRef(position).getKey();

                mUserDatabase.child(friend_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("userName").getValue().toString();
                        String userEmail = dataSnapshot.child("userEmail").getValue().toString();

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setEmail(userEmail);
                        friendsViewHolder.setImage(getApplicationContext(), storageRef,friend_id);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                final String user_id = getRef(position).getKey();

                friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(FriendsRequest.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };

        mFriendRequest.setAdapter(friendsRecyclerViewAdapter);

    }

    //View Holder Class

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView){
            super(itemView);

            mView = itemView;

        }

        public void setImage(final Context ctx, StorageReference storageRef, String user_id){

            final ImageView image = (ImageView) mView.findViewById(R.id.profile_image);

            storageRef.child(user_id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(ctx).load(uri).fit().centerCrop().into(image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ctx, "Failed to get Profile Picture", Toast.LENGTH_SHORT).show();
                }
            });

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.username_text);
            userNameView.setText(name);

        }

        public void setEmail(String email){

            TextView emailView = (TextView) mView.findViewById(R.id.email_text);
            emailView.setText(email);

        }

    }

}