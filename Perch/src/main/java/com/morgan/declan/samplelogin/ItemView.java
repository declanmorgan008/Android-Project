package com.morgan.declan.samplelogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Populates activity to show details on a specific post.
 * This is instantiated when a user clicks on an item from the users profile activity or
 * in the home fragment.
 * */
public class ItemView extends AppCompatActivity {

    TextView title, description, size, brand;
    ArrayList<Post> targetsArrayList;
    List<Upload> uploads;
    private String uid;
    private String contactEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Integer myInt = getIntent().getIntExtra("Description", 0);
        setContentView(R.layout.activity_item_view);
        //Load the image from the intent to the image view
        String imageURL = getIntent().getStringExtra("imageURL");
        Log.d("IMAGE URL ", imageURL);
        ImageView ig = findViewById(R.id.postImage);
        Glide.with(this).load(imageURL).into(ig);

        //Set all details about the post to its corresponding text views in activity.
        title = findViewById(R.id.post_view_title);
        final String postTitle = getIntent().getStringExtra("postTitle");
        title.setText(getIntent().getStringExtra("postTitle"));
        description = findViewById(R.id.post_view_description);
        description.setText(getIntent().getStringExtra("postDescription"));
        brand = findViewById(R.id.post_view_brand);
        brand.setText(getIntent().getStringExtra("postBrand"));
        size = findViewById(R.id.post_view_size);
        size.setText(getIntent().getStringExtra("postSize"));

        this.uid = getIntent().getStringExtra("uid");

        getEmailFromPost();

        //Calls an intent for email application to contact the owner of a post.
        ImageButton contactSeller = findViewById(R.id.contact_seller_btn);
        contactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Format the email to be sent to the owner of the post.
                String[] TO = {contactEmail};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Interest in your Perch post - " + postTitle);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi, I am interested in the " + postTitle + " you have " +
                        "posted on the Perch app. It would be great to hear back from you soon.");
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    finish();
                    Log.i("Finished sending email.", "");
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * Retrieves the post owners email address to allow user to contact the owner about
     * a product.
     * */
    public void getEmailFromPost(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference().child("users");

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot single : dataSnapshot.getChildren()) {
                    if(single.getKey().equals(uid)){
                        Log.e("UID COMPARE: ", single.getKey() + ", " + uid);
                        User contactUser = single.getValue(User.class);
                        contactEmail = contactUser.getEmail();
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Declan.com", "fetchData onCancelled", databaseError.toException());
            }
        };
        myRef.addValueEventListener(postListener);

    }
}
