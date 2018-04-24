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
import android.widget.ImageButton;
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

public class FriendsList extends AppCompatActivity {

    private ImageButton mAddFriends;

    private ImageView mNoFriendsImage;

    private TextView mNoFriendsText;

    private ImageView image;

    private RecyclerView mFriendsList;

    private DatabaseReference mUserDatabase;

    private DatabaseReference mFriendDatabase;

    private FirebaseStorage mStorage;

    private StorageReference storageRef;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();

        mStorage = FirebaseStorage.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        storageRef = mStorage.getReferenceFromUrl("gs://final-project-25a7b.appspot.com");

        mAddFriends = (ImageButton) findViewById(R.id.add_friend_btn);
        mNoFriendsImage = (ImageView) findViewById(R.id.no_friend_btn);
        mNoFriendsText = (TextView) findViewById(R.id.no_friend_text);

        image = (ImageView) findViewById(R.id.profile_image);

        mNoFriendsImage.setVisibility(View.GONE);
        mNoFriendsText.setVisibility(View.GONE);

        mFriendsList = (RecyclerView) findViewById(R.id.friend_requests);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(this));

        mAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(FriendsList.this, Search.class));

            }
        });

        firebaseFriendList();

    }

    private void firebaseFriendList() {

        final FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.friend_list_layout,
                FriendsViewHolder.class,
                mFriendDatabase


        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position) {

                friendsViewHolder.setDate(friends.getDate());

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

                        Intent profileIntent = new Intent(FriendsList.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);

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

        public void setDate(String date){

            TextView dateView = (TextView) mView.findViewById(R.id.date_text);
            dateView.setText(date);

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