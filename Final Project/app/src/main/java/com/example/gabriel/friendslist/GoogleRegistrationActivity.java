/*package com.example.gabriel.friendslist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Map;


public class GoogleRegistrationActivity extends AppCompatActivity {

    private EditText userName;
    private Button loginButton;
    private FirebaseAuth firebaseAuth;
    private ImageView userProfilePic;
    String name;
    DatabaseReference myRef;
    private FirebaseStorage firebaseStorage;
    private static int PICK_IMAGE = 123;
    Uri imagePath;
    private StorageReference storageReference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null)
        {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                userProfilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);
        setupUIViews();

        final String email = getIntent().getStringExtra("userEmail");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"),PICK_IMAGE);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
                if (validate())
                {
                    sendUserData(email);
                    startActivity(new Intent(GoogleRegistrationActivity.this, MapsActivity.class));

                }
            }
        });
    }

    private void setupUIViews()
    {
        userName = (EditText)findViewById(R.id.etUserName);
        loginButton = (Button)findViewById(R.id.btnLogin);
        userProfilePic = (ImageView)findViewById(R.id.ivProfile);
    }

    private Boolean validate()
    {
        Boolean result = false;

        name = userName.getText().toString();

        if(name.isEmpty() || imagePath == null)
        {
            Toast.makeText(this, "Login Failed. Please enter all the details", Toast.LENGTH_SHORT).show();
        }
        else
        {
            result = true;
        }

        return result;
    }

    private void sendUserData(String email){
        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());
        StorageReference imageReference = storageReference.child(firebaseAuth.getUid());
        UploadTask uploadTask = imageReference.putFile(imagePath);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GoogleRegistrationActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(GoogleRegistrationActivity.this, "Upload complete!", Toast.LENGTH_SHORT).show();

            }
        });
        UserProfile userProfile = new UserProfile(email, name);
        myRef.setValue(userProfile);

        myRef.child("Request").setValue(new Request("No one"));
        RequestTicket emptyTicket = new RequestTicket("Pending","EmptyTicket");
        myRef.child("RequestTicket").setValue(emptyTicket);
    }



}*/