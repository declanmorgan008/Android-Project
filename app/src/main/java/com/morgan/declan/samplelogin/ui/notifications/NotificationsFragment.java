package com.morgan.declan.samplelogin.ui.notifications;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.*;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.morgan.declan.samplelogin.ItemArrayAdapter;
import com.morgan.declan.samplelogin.MainActivity;
import com.morgan.declan.samplelogin.ManagePosts;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.Upload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows user profile information. Allows ability to change user email, profile image
 * and manage posts. Users can also sign out from this fragment.
 * */

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference mDatabase;
    private TextView mTV;

    private StorageReference mStorageReference;

    private ArrayList<Post> postList = new ArrayList<Post>();
    ArrayList<Post> mPostTargetData = new ArrayList<>();
    private List<Upload> uploads;

    private FusedLocationProviderClient fusedLocationClient;

    private ImageView profilePic;
    private Uri filePath;
    private Button changeEmail, changePicture, managePosts;
    private View rootView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        setHasOptionsMenu(true);

        rootView = root;
        mStorageReference = FirebaseStorage.getInstance().getReference();
        TextView uNameTv = root.findViewById(R.id.username);
        uNameTv.setText("Welcome Back, " + mAuth.getCurrentUser().getDisplayName() + "!");

        profilePic = root.findViewById(R.id.profile_picture);
        mTV = root.findViewById(R.id.user_items_tv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView = root.findViewById(R.id.item_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        uploads = new ArrayList<>();
        recyclerView.setAdapter(mAdapter);
        fetchData();


        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("INFO", "Clicked on card.");
            }
        });

        changeEmail = root.findViewById(R.id.change_email_btn);
        changePicture = root.findViewById(R.id.change_profile_img_btn);

        //Set on click listener to call changeEmail method.
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeEmail();
            }
        });

        //Set on click listener for intent to manage posts.
        managePosts = root.findViewById(R.id.manage_posts_btn);
        managePosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ManagePosts.class);
                startActivity(intent);
            }
        });


        changePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });


        return root;
    }


    /**
     * Intent call to show a gallery file chooser to allow user ability to choose image
     * for upload to change their profile image.*/
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 234);
    }

    /**
     * Gets result from calling File Chooser intent. Gets file path of image selected
     * and updates profile ImageView to new image.*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Make sure the data collected is from the correct intent result.
        if (requestCode == 234 && data != null && data.getData() != null) {
            //get file path of new image selected.
            filePath = data.getData();
            Uri uri = data.getData();

            //Update ImageView in profile to new image and call for an update of
            //user image in the Firebase Database.
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                ImageView imageView = rootView.findViewById(R.id.profile_picture);
                imageView.setImageBitmap(bitmap);
                updateUserPicture();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Creates an Alert Dialog and requests user to enter new email address to change
     * on user profile.*/
    public void changeEmail(){
        //create a new dialog.
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        //set details of the dialog.
        final EditText newEmail = new EditText(getContext());
        alert.setMessage("Enter your new email address.");
        alert.setTitle("Change Email Address.");
        //set the view of the dialog.
        alert.setView(newEmail);

        //On clicking 'OK' users new email is set in the Firebase Database.
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String etEmail = newEmail.getText().toString();
                Log.e("New Email Set:", etEmail);
                FirebaseAuth.getInstance().getCurrentUser().updateEmail(etEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Snackbar.make(newEmail, "Your email address was updated successfully.", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        alert.show();
    }

    /**
     * When activity is resumed, update the recycler view details.*/
    @Override
    public void onResume(){
        super.onResume();
        mAdapter = new ItemArrayAdapter(getContext(),mPostTargetData, uploads, R.layout.dashboard_item);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * When the activity is started fetch all neccessary data from Firebase.*/
    @Override
    public void onStart(){
        super.onStart();
        fetchData();
    }

    /**
     * Show sign out button in options menu.*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Sign out user when option is selected from App Bar.*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                //Sign out user and call intent to Main Activity to allow for re-login or registration.
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    /**
     * Uploads new Profile Image to Firebase Storage.*/
    public void updateUserPicture(){
        //Get Database reference for user.
        final StorageReference userRef = mStorageReference.child("users/" + mAuth.getCurrentUser().getUid());
        if(userRef != null) {
            userRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                Uri userUri;
                //Compress image selected and upload to Firebase.
                @Override
                public void onSuccess(Void aVoid) {
                    //Get new image selected by image.
                    Bitmap bmpImage = null;
                    try {
                        bmpImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Compress image size.
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmpImage.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] data = baos.toByteArray();
                    //adding the file to database
                    userRef.putBytes(data)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    userRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final Uri downloadUri = uri;
                                            userUri = downloadUri;

                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference dbRef = database.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("photoUri");
                                            Log.e("user download url: ", userUri.toString());
                                            dbRef.setValue(userUri.toString());
                                            //displaying success toast
                                            Toast.makeText(getActivity().getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                            //Add user display picture url to user class
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                                            mAuth.getCurrentUser().updateProfile(profileUpdates);
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //display error toast for upload exception.
                                    Toast.makeText(getActivity().getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        }
    }

    /**
     * Get user posts and add to ItemArrayAdapter.*/
    public void fetchData(){
        //Crop profile display image to circle shape and add to user profile image view.
        Uri photo_url = mAuth.getCurrentUser().getPhotoUrl();
        Glide.with(getContext()).load(photo_url).apply(RequestOptions.circleCropTransform()).into(profilePic);
        Glide.with(this)
                .load(photo_url)
                .apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(50)))
                .into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                getActivity().findViewById(R.id.profile_background).setBackground(resource);
            }
        });
        //Get user ID.
        String uid = mAuth.getUid();
        //Get firebase database reference.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("posts").child(uid);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clear all previous user posts from arraylist.
                mPostTargetData.clear();
                for (DataSnapshot single : dataSnapshot.getChildren()) {
                    //add each post retrieved from firebase to user posts array list.
                    Post target =  single.getValue(Post.class);
                    mPostTargetData.add(target);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Declan.com", "fetchData onCancelled", databaseError.toException());
            }
        };
        myRef.addValueEventListener(postListener);

        //Get all images relating to each user post.
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("picture_uploads").child(uid);
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("childrenCount", ""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Add each image to an array list.
                    Upload upload = postSnapshot.getValue(Upload.class);
                    uploads.add(upload);
                }
                //Update UI informing user of how many posts they have in the app.
                if(mPostTargetData.size() > 0 ){
                    mTV.setText("Your items (" + mPostTargetData.size() + "):");
                    Log.e("SizeOfUploads_DC" , "" + uploads.size());
                }else{
                    mTV.setText("You have no items.");
                }

                //creating adapter
                mAdapter = new ItemArrayAdapter(getContext(),mPostTargetData, uploads, R.layout.list_item);

                //adding adapter to recyclerview
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}