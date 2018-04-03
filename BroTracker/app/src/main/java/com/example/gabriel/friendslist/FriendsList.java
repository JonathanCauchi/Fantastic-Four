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

public class FriendsList extends AppCompatActivity {

    private ImageButton mAddFriends;

    private ImageView mNoFriendsImage;

    private TextView mNoFriendsText;

    private RecyclerView mFriendsList;

    private DatabaseReference mUserDatabase;

    private DatabaseReference mFriendDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mAddFriends = (ImageButton) findViewById(R.id.add_friend_btn);
        mNoFriendsImage = (ImageView) findViewById(R.id.no_friend_btn);
        mNoFriendsText = (TextView) findViewById(R.id.no_friend_text);

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