package com.morgan.declan.samplelogin.ui.notifications;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.*;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference mDatabase;

    private StorageReference mStorageReference;

    private ArrayList<Post> postList = new ArrayList<Post>();

    ArrayList<Post> mPostTargetData = new ArrayList<>();
    private List<Upload> uploads;

    private FirebaseRecyclerAdapter<Post, PostHolder> firebaseRecyclerAdapter;
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

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeEmail();
            }
        });

        managePosts = root.findViewById(R.id.manage_posts_btn);
        managePosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent managePostsIntent = new Intent(getActivity(), ManagePosts.class);
                startActivity(managePostsIntent);
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

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 234);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 234 && data != null && data.getData() != null) {
            filePath = data.getData();

            Uri uri = data.getData();

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

    public void changeEmail(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        final EditText newEmail = new EditText(getContext());
        alert.setMessage("Enter your new email address.");
        alert.setTitle("Change Email Address.");

        alert.setView(newEmail);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String etEmail = newEmail.getText().toString();
                Log.e("New Email Set:", etEmail);
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

    @Override
    public void onResume(){
        super.onResume();
        mAdapter = new ItemArrayAdapter(getContext(),mPostTargetData, uploads, R.layout.dashboard_item);

        //adding adapter to recyclerview
        recyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart(){
        super.onStart();
        fetchData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    public void updateUserPicture(){
        final StorageReference userRef = mStorageReference.child("users/" + mAuth.getCurrentUser().getUid());
        if(userRef != null) {


            userRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                Uri userUri;

                @Override
                public void onSuccess(Void aVoid) {
                    Bitmap bmpImage = null;
                    try {
                        bmpImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmpImage.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] data = baos.toByteArray();
                    //adding the file to reference
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
                                            //dismissing the progress dialog
                                            //displaying success toast
                                            Toast.makeText(getActivity().getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                            //Add user dp url to user class
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                                            mAuth.getCurrentUser().updateProfile(profileUpdates);
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                    Toast.makeText(getActivity().getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                }
            });
        }
    }

    public void fetchData(){
        System.out.println("******************" + mAuth.getUid());
        Uri photo_url = mAuth.getCurrentUser().getPhotoUrl();
        Glide.with(getContext()).load(photo_url).apply(RequestOptions.circleCropTransform()).into(profilePic);
        //final LinearLayout profileBack = getActivity().findViewById(R.id.profile_background);
//        Glide.with(getContext()).load(photo_url).apply(RequestOptions.bitmapTransform(new BlurTransformation(25))).into(profileBack);
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
        String uid = mAuth.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("posts").child(uid);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPostTargetData.clear();
                for (DataSnapshot single : dataSnapshot.getChildren()) {
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


        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("picture_uploads").child(uid);
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("childrenCount", ""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    uploads.add(upload);
                }

                //TextView mTV = getActivity().findViewById(R.id.user_items_tv);
                //mTV.setText("Your items (" + mPostTargetData.size() + "):");


                Log.e("SizeOfUploads_DC" , "" + uploads.size());
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

    private class PostHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView, descTextView;


        PostHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_title);
            descTextView = itemView.findViewById(R.id.item_desc);
        }

        void setPost(Post userPost) {
            String postTitle = userPost.getPostTitle();
            titleTextView.setText(postTitle);
            String desc = userPost.getDescription();
            descTextView.setText(desc);
        }

    }




}