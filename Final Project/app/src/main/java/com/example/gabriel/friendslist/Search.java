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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Search extends AppCompatActivity {

    private EditText mSearchField;
    private ImageButton mSearchBtn;

    private RecyclerView mResultList;

    private DatabaseReference mUserDatabase;

    private FirebaseStorage mStorage;
    private StorageReference storageRef;

    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");

        mStorage = FirebaseStorage.getInstance();

        storageRef = mStorage.getReferenceFromUrl("gs://final-project-25a7b.appspot.com");

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.add_friend_btn);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mResultList = (RecyclerView) findViewById(R.id.friend_requests);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = mSearchField.getText().toString();

                if(!searchText.equals("")){

                    firebaseUserSearch(searchText);

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                }
            }
        });

    }

    private void firebaseUserSearch(String searchText) {

        Toast.makeText(Search.this, "Started Search", Toast.LENGTH_LONG).show();

        Query firebaseSearchQuery = mUserDatabase.orderByChild("userName").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.list_layout,
                UsersViewHolder.class,
                firebaseSearchQuery

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                final String user_id = getRef(position).getKey();

                viewHolder.setDetails(getApplicationContext(), model.getEmail(),model.getUserName(),storageRef,user_id);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(user_id.equals(mCurrent_user_id)) {
                            startActivity(new Intent(Search.this,myProfileActivity.class));
                        }
                        else{
                            Intent profileIntent = new Intent(Search.this,ProfileActivity.class);
                            profileIntent.putExtra("user_id",user_id);
                            startActivity(profileIntent);
                        }

                    }
                });

            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);

    }

    //View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDetails(final Context ctx, String userEmail, String userUsername, StorageReference storageRef, String user_id){

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

            TextView user_email = (TextView) mView.findViewById(R.id.email_text);
            TextView user_username = (TextView) mView.findViewById(R.id.username_text);

            user_email.setText(userEmail);
            user_username.setText(userUsername);



        }

    }

}