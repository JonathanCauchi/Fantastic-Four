package com.example.gabriel.friendslist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText Name;
    private EditText Password;
    private TextView Info;
    private Button Login;
    private int counter = 5;
    private TextView userRegistration;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private TextView forgotPassword;
    private DatabaseReference mDatabase;

    String reqPer[] = {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION","android.permission.INTERNET"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, reqPer, 1);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Name=(EditText)findViewById(R.id.etName);
        Password=(EditText)findViewById(R.id.etPassword);
        Info=(TextView)findViewById(R.id.tvInfo);
        Login=(Button)findViewById(R.id.btnLogin);
        userRegistration = (TextView)findViewById(R.id.tvRegister);
        forgotPassword = (TextView)findViewById(R.id.tvForgotPassword);

        Info.setText("No of attempts remaining: 5");

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){

            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                finish();
                startActivity(new Intent(MainActivity.this, MapsActivity.class));

            }
            else{
                ActivityCompat.requestPermissions(this, reqPer, 1);
            }

        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                if(ContextCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                 ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    validate(Name.getText().toString(), Password.getText().toString());

                }
                else{
                    ActivityCompat.requestPermissions(MainActivity.this, reqPer, 1);
                }

            }

        });

        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PasswordActivity.class));
            }
        });
    }

    private void validate(String userName, String userPassword)
    {

        progressDialog.setMessage("Please wait!");
        progressDialog.show();

       firebaseAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               if(task.isSuccessful()){
                   progressDialog.dismiss();
                   //Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                   checkEmailVerification();
               }else{
                   Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                   counter--;
                   Info.setText("No of attempts remaining: " + counter);
                   progressDialog.dismiss();
                   if(counter == 0){
                       Login.setEnabled(false);
                   }
               }
           }
       });
    }

    private void checkEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        Boolean emailflag = firebaseUser.isEmailVerified();
        if(emailflag){

            mDatabase.child("Users").child(firebaseAuth.getUid()).child("Request").setValue(new Request("No one"));
            RequestTicket emptyTicket = new RequestTicket("Pending","EmptyTicket");
            mDatabase.child("Users").child(firebaseAuth.getUid()).child("RequestTicket").setValue(emptyTicket);

            startActivity(new Intent(MainActivity.this, MapsActivity.class));

        }
        else{

            Toast.makeText(MainActivity.this, "Login Failed. Make sure email is verified.", Toast.LENGTH_SHORT).show();

        }




 //       if(emailflag){
 //           finish();
 //           startActivity(new Intent(MainActivity.this, SecondActivity.class));
 //       }else{
 //           Toast.makeText(this, "Verify your email", Toast.LENGTH_SHORT).show();
 //         firebaseAuth.signOut();
  //     }
    }
}