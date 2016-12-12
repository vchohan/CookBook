package com.vchohan.cookbook;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerUserName;

    private EditText registerUserEmail;

    private EditText registerUserPassword;

    private Button registerButton;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgressDialog = new ProgressDialog(this);

        registerUserName = (EditText) findViewById(R.id.register_user_name);
        registerUserEmail = (EditText) findViewById(R.id.register_user_email);
        registerUserPassword = (EditText) findViewById(R.id.register_user_password);

        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegistrationProcess();
            }
        });
    }

    private void startRegistrationProcess() {

        final String userName = registerUserName.getText().toString().trim();
        String userEmail = registerUserEmail.getText().toString().trim();
        String userPassword = registerUserPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPassword)) {

            mProgressDialog.setMessage("Please wait while we register your account.");
            mProgressDialog.show();

            mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        DatabaseReference currentUserDatabase = mDatabase.child(userId);
                        currentUserDatabase.child("name").setValue(userName);
                        currentUserDatabase.child("image").setValue("default");

                        mProgressDialog.dismiss();

                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
