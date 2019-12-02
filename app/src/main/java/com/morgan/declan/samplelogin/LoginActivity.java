package com.morgan.declan.samplelogin;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Authenticates user's email and password input. If user account exists the user is logged in.
 * If the user account does not exist a Toast is shown to user.
 *
 * Password reset link can be sent to user when forgot password TextView is clicked.*/

public class LoginActivity extends AppCompatActivity {

    private EditText emailTV, passwordTV;
    private Button loginBtn;
    private ProgressBar progressBar;
    private TextView forgotPassTV;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailTV = findViewById(R.id.email);
        mAuth = FirebaseAuth.getInstance();
        //Initialise forgot password TextView and add listener for on click event.
        forgotPassTV = findViewById(R.id.reset_password);
        forgotPassTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(emailTV.getText().toString().equals("")){
                    Snackbar.make(forgotPassTV, "Enter an email address to reset a password.", Snackbar.LENGTH_LONG).show();
                }else{
                    sendResetEmail(emailTV.getText().toString());
                }

            }
        });

        initializeUI();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });
    }

    /**
     * Sends a reset email to the users email address.
     * User has to have typed email address in email EditText field before sending.
     *
     * @param email User email address to send reset email to.
     * */
    public void sendResetEmail(String email){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    View contextView = findViewById(R.id.reset_password);
                    Snackbar.make(contextView, "A reset link has been sent to your email address.", Snackbar.LENGTH_LONG).show();

                }
            }
        });
    }


    /**
     * Log's a user into their account by connecting to Firebase & Authenticating email & password.*/
    private void loginUserAccount() {
        //Set progress spinner visibility.
        progressBar.setVisibility(View.VISIBLE);

        String email, password;
        email = emailTV.getText().toString();
        password = passwordTV.getText().toString();

        //Request user to enter email before continuing to log in.
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //Request user to enter password before continuing to log in.
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password...", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //Firebase authentication connection to sign user into account.
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            //Start main app activity that stores all main content.
                            Intent intent = new Intent(LoginActivity.this, SearchActivity.class);
                            startActivity(intent);
                        }
                        else {
                            //Error signing in, account may not exist.
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * Creates instances of each UI element for login UI.
     * Instances include:
     * Email TextView
     * Password TextView
     * Login Button
     * Progress Bar*/
    private void initializeUI() {
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);

        loginBtn = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);
    }


}
