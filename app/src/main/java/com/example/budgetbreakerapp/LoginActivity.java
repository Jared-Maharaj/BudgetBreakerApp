package com.example.budgetbreakerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private TextView loginQn;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private FirebaseAuth.AuthStateListener authStateListener; //remain logged in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialization
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        loginQn = findViewById(R.id.loginQn);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        authStateListener = new FirebaseAuth.AuthStateListener() { //auto login if user has logged in before
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();//validation
                if (user!=null){ //if user is already logged
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); //redirect
                    startActivity(intent);
                    finish();
                }
            }
        };

        loginQn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class); //redirect
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getText().toString(); //get email
                String passwordString = password.getText().toString(); //get password

                if (TextUtils.isEmpty(emailString)){ //error check
                    email.setError("Email is Required");
                }
                if (TextUtils.isEmpty(passwordString)){ //error check
                    password.setError("Password is Required");
                }

                else{

                    progressDialog.setMessage("Login is in Progress..."); //pass error check progress
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    mAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() { //firebase authenticate user adapter
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class); //redirect
                                startActivity(intent);
                                finish();
                                progressDialog.dismiss();
                            }else {
                                Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show(); //display message if login was unsuccessful
                                progressDialog.dismiss();
                            }
                        }
                    });

                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(authStateListener); //auto login check start

    }

    @Override
    protected void onStop() {
        super.onStop();

        mAuth.removeAuthStateListener(authStateListener); //auto login check stop

    }
}