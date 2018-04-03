package com.example.gabriel.friendslist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FriendsRequest extends AppCompatActivity {

    private ImageView mNoFriendRequestImage;

    private TextView mNoFriendRequestText;

    private RecyclerView mFriendRequest;

    private DatabaseReference mUserDatabase;

    private DatabaseReference mFriendReqDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mNoFriendRequestImage = (ImageView) findViewById(R.id.no_friend_request_btn);
        mNoFriendRequestText = (TextView) findViewById(R.id.no_friend_request_text);

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

                String friend_id = getRef(position).getKey();

                mUserDatabase.child(friend_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("userName").getValue().toString();
                        String userEmail = dataSnapshot.child("userEmail").getValue().toString();

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setEmail(userEmail);

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