package com.morgan.declan.samplelogin;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.morgan.declan.samplelogin.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Creates a new user instance and uploads to Firebase database.
 * */
public class RegisterActivity extends AppCompatActivity {

    private EditText emailTV, passwordTV, nameTV;
    private Button regBtn;
    private ProgressBar progressBar;
    private Button userDP;
    private Uri filePath;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabase;
    private Uri userDownloadUri;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        userDP = findViewById(R.id.choose_dp_button);
        initializeUI();

        userDownloadUri = Uri.parse("http://www.google.com");

        //Calls the register new user method to create a new user.
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
        //Calls the show file chooser method to allow user to choose image for profile
        //display picture.
        userDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });
    }

    /**
     * Registers a new user and updates Firebase to add new user.*/
    private void registerNewUser() {
        progressBar.setVisibility(View.VISIBLE);

        final String email, password, displayName;
        email = emailTV.getText().toString();
        password = passwordTV.getText().toString();
        displayName = nameTV.getText().toString();

        //Make sure user has entered details for all fields.
        if(TextUtils.isEmpty(displayName)){
            Toast.makeText(getApplicationContext(), "Please enter username.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password.", Toast.LENGTH_LONG).show();
            return;
        }
        if(filePath == null){
            Toast.makeText(getApplicationContext(), "Please select an image and try again.", Toast.LENGTH_LONG).show();
            return;
        }
        //Updates Firebase to register a new user with email and password
        //also uploads image to Firebase for user account.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    Uri userUri;
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            //Create a new user with name and email address.
                            User newUser = new User(displayName, email);
                            //Write the new user to Firebase
                            newUser.writeUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            //Update user profile on Firebase to add a display name.
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                            mAuth.getCurrentUser().updateProfile(profileUpdates);

                            if (filePath != null) {
                                //displaying progress dialog while image is uploading
                                Log.e("FilePath: ", filePath.toString());
                                Log.e("Current User: ", mAuth.getCurrentUser().getEmail());
                                final StorageReference sRef = mStorageReference.child("users/" + mAuth.getCurrentUser().getUid());
                                //Get image from users phone media.
                                Bitmap bmpImage = null;
                                try {
                                    bmpImage = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //Compress image size.
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bmpImage.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                                byte[] data = baos.toByteArray();
                                //adding the file to firebase reference
                                sRef.putBytes(data)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final Uri downloadUri = uri;
                                                        userUri = downloadUri;
                                                        //Upload image file to Firebase database.
                                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                        DatabaseReference dbRef = database.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("photoUri");
                                                        Log.e("user download url: ", userUri.toString());
                                                        dbRef.setValue(userUri.toString());
                                                        //displaying success toast
                                                        Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                                        //Add user dp url to user class in Firebase.
                                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                                                        mAuth.getCurrentUser().updateProfile(profileUpdates);
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(getApplicationContext(), "Select a picture and try again.", Toast.LENGTH_SHORT).show();
                            }
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * Show file chooser to user to allow to choose an image from gallery for a user profile.*/
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 234);
    }

    /**
     * Get image from the intent result of file chooser and update imageview in activity.*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 234 && data != null && data.getData() != null) {
            filePath = data.getData();
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ImageView imageView = findViewById(R.id.displayPicture);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Initialises the UI and references each UI component for registration.*/
    private void initializeUI() {
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        regBtn = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);
        nameTV = findViewById(R.id.displayName);
    }
}