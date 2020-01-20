package com.example.ahsan.masumvaiproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class PatientRegistrationActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mBio;
    private EditText mContactNo;

    private Button mRegButton;
    private ImageButton mSetupImageBtn;
    private DatabaseReference mDatabase;

    private ProgressBar mprogressBar;

    private DatabaseReference mdatabaseUsers;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registration);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mNameField = (EditText) findViewById(R.id.name_field);
        mEmailField = (EditText) findViewById(R.id.email_field);
        mPasswordField = (EditText) findViewById(R.id.password_field);
        mRegButton = (Button) findViewById(R.id.reg_button);
        mContactNo = findViewById(R.id.number_field);
        mBio = findViewById(R.id.bio_field);

        mprogressBar = (ProgressBar) findViewById(R.id.progressBar);


        mRegButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mRegButton.getBackground().setAlpha(90);
                        break;

                    case MotionEvent.ACTION_UP:
                        mRegButton.getBackground().setAlpha(255);
                        startRegistration();
                        break;
                }

                return false;
            }
        });
    }


    private void startRegistration() {


        final String name = mNameField.getText().toString().trim();
        final String bio = mBio.getText().toString().trim();
        String email1 = mEmailField.getText().toString().trim();
        String password1 = mPasswordField.getText().toString().trim();
        final String contactNo = mContactNo.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email1) || TextUtils.isEmpty(password1)){

            Toast.makeText(PatientRegistrationActivity.this,"Field can't be empty",Toast.LENGTH_SHORT).show();

        }else {

            //Toast.makeText(RegisterActivity.this,"Yes in registration 2",Toast.LENGTH_SHORT).show();

            mprogressBar.setVisibility(View.VISIBLE);
            mRegButton.setText("Signing in..");

            mAuth.createUserWithEmailAndPassword(email1, password1).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //Toast.makeText(RegisterActivity.this,"Yes in registration 3",Toast.LENGTH_SHORT).show();

                    if (!task.isSuccessful()){
                        //Toast.makeText(RegisterActivity.this,"Failed Registration!!",Toast.LENGTH_LONG).show();
                        mprogressBar.setVisibility(View.INVISIBLE);
                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        Toast.makeText(PatientRegistrationActivity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        Toast.makeText(PatientRegistrationActivity.this,"Please wait a while...!!",Toast.LENGTH_SHORT).show();

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        String user_id = mAuth.getCurrentUser().getUid();
                        String searchName = name.toLowerCase();
                        mdatabaseUsers = mDatabase.child(user_id);

                        mdatabaseUsers.child("name").setValue(name);
                        mdatabaseUsers.child("bio").setValue(bio);
                        mdatabaseUsers.child("search_name").setValue(searchName);
                        mdatabaseUsers.child("device_token").setValue(deviceToken);
                        mdatabaseUsers.child("contact_no").setValue("tel:+88"+contactNo);



                        //Toast.makeText(RegisterActivity.this,"Yes 3",Toast.LENGTH_SHORT).show();

                        mprogressBar.setVisibility(View.INVISIBLE);
                        Intent mainIntent = new Intent(PatientRegistrationActivity.this,PatientsMapsActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(mainIntent);
                        finish();
                    }
                }
            });

        }
    }
}
