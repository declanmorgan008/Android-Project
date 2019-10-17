package com.morgan.declan.samplelogin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        Button registerButton = findViewById(R.id.registerBtn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        Button loginButton = findViewById(R.id.loginBtn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent goToDashboard = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(goToDashboard);
        } else {
            //No com.morgan.declan.samplelogin.User is Signed in.
        }

    }
}
