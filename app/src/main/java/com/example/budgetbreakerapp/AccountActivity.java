package com.example.budgetbreakerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;
    private TextView userEmail;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        settingsToolbar = findViewById(R.id.my_feed_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("My Account");

        logoutBtn = findViewById(R.id.logoutBtn);
        userEmail = findViewById(R.id.userEmail);

        userEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        //logout function
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(AccountActivity.this) //AlertDialog confirmation prompt
                        .setTitle("Budget Breaker")
                        .setMessage("Are you sure you want to Log Out?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { //if yes
                                FirebaseAuth.getInstance().signOut(); //firebase logout function
                                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show(); //if no
            }
        });

    }
}