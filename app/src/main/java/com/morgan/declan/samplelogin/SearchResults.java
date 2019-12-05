package com.morgan.declan.samplelogin;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.morgan.declan.samplelogin.ui.search.SearchFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Populates a recycler view with all posts from a specific search query.*/
public class SearchResults extends AppCompatActivity {


    private ArrayList<Post> postsFromSearchAL;
    private ArrayList<Upload> uploadsFromSearchAL;
    private RecyclerView search_rv;
    public RecyclerView.Adapter mAdapter;
    private ArrayList<String> uidList;
    private TextView resultsInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        uidList = new ArrayList<>();
        postsFromSearchAL = new ArrayList<>();
        uploadsFromSearchAL = new ArrayList<>();

        resultsInfo = findViewById(R.id.results_info_tv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        search_rv = findViewById(R.id.search_results_list);
        search_rv.setHasFixedSize(true);
        search_rv.setLayoutManager(layoutManager);
        mAdapter = new ItemArrayAdapter(getApplicationContext(), postsFromSearchAL, uploadsFromSearchAL, R.layout.dashboard_item);
        search_rv.setAdapter(mAdapter);
        Bundle extras = getIntent().getExtras();
        String titleText = extras.getString("titleSearch");
        String brandText = extras.getString("brandSearch");
        String colourText = extras.getString("colourText");
        postsFromSearchAL.clear();
        uploadsFromSearchAL.clear();

        Log.e("Before searching: ", titleText + ", " + brandText + ", " + colourText);
        //Call for a search on each field the user has entered text for.
        if(!titleText.isEmpty() && !colourText.isEmpty() && !brandText.isEmpty()){
            searchFirebase(titleText, colourText, brandText, 1);
        }else if(!titleText.isEmpty() && !colourText.isEmpty() && brandText.isEmpty()){
            searchFirebase(titleText, colourText, brandText, 2);
        }else if(!titleText.isEmpty() && colourText.isEmpty() && !brandText.isEmpty()) {
            searchFirebase(titleText, colourText, brandText, 3);
        }else if(!titleText.isEmpty() && colourText.isEmpty() && brandText.isEmpty()){
            searchFirebase(titleText, colourText, brandText, 4);
        }

        resultsInfo.setVisibility(View.INVISIBLE);


    }


    /**
     * Searches Firebase Database for specific posts conforming to a search query.
     * @param titleToSearch the type of search, e.g. title search, colour search or brand search
     *
     */

    public void searchFirebase(final String titleToSearch,
                                final String colourToSearch,
                               final String brandToSearch,
                               final int searchType){
        postsFromSearchAL.clear();
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("posts");
        mRef.addValueEventListener(new ValueEventListener() {
            //Gets all posts relating to a specific query.
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String uid = ds.getKey();

                    uidList.add(uid);
                    Log.e("usrr id list", uidList.toString());

                    FirebaseDatabase.getInstance().getReference().child("posts").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                Post foundPost = postSnapshot.getValue(Post.class);
                                String postTitle = foundPost.getPostTitle().toLowerCase();
                                String postColour = foundPost.getColour().toLowerCase();
                                String postBrand = foundPost.getBrand().toLowerCase();
                                Log.e("brand", postBrand);
                                Log.e("titles", titleToSearch + ", " + colourToSearch + ", " + brandToSearch);

                                switch(searchType){
                                    case 1:
                                        if(postTitle.contains(titleToSearch)
                                                && postColour.contains(colourToSearch)
                                                && postBrand.contains(brandToSearch)){
                                            postsFromSearchAL.add(foundPost);
                                            getImages(foundPost.getUid(), foundPost.getPid());
                                            Log.e("all three", foundPost.toString());
                                        }
                                        break;
                                    case 2:
                                        if(postTitle.contains(titleToSearch)
                                                && postColour.contains(colourToSearch)){
                                            postsFromSearchAL.add(foundPost);
                                            getImages(foundPost.getUid(), foundPost.getPid());
                                            Log.e("title & colour", foundPost.toString());
                                        }
                                        break;
                                    case 3:
                                        if(postTitle.contains(titleToSearch)
                                                && postBrand.contains(brandToSearch)){
                                            postsFromSearchAL.add(foundPost);
                                            getImages(foundPost.getUid(), foundPost.getPid());
                                            Log.e("title & brand", foundPost.toString());
                                        }
                                        break;
                                    case 4:
                                        if(postTitle.contains(titleToSearch)){
                                            postsFromSearchAL.add(foundPost);
                                            getImages(foundPost.getUid(), foundPost.getPid());
                                            Log.e("title", foundPost.toString());
                                        }
                                        break;

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Log.e("POSTS SIZE", "" + postsFromSearchAL.size());
                    mAdapter.notifyDataSetChanged();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    /**
     * Retrieves images relating to a specific post.
     * @param alUid User ID for a specific post.
     * @param alPid Post ID for a specific post.
     * */
    public void getImages(final String alUid, final String alPid){
        final DatabaseReference imageDBRef = FirebaseDatabase.getInstance().getReference().child("picture_uploads")
                .child(alUid).child(alPid);
        imageDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Add the image once it has been found to an array list.
                Upload userUpload = dataSnapshot.getValue(Upload.class);
                uploadsFromSearchAL.add(userUpload);
                Log.e("UPLOADS SIZE", "" + uploadsFromSearchAL.size());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        mAdapter = new ItemArrayAdapter(getApplicationContext(),postsFromSearchAL, uploadsFromSearchAL, R.layout.dashboard_item);

        //adding adapter to recyclerview
        search_rv.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }
}