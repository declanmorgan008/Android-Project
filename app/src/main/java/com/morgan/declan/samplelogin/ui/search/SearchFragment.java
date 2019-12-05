package com.morgan.declan.samplelogin.ui.search;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.morgan.declan.samplelogin.ItemArrayAdapter;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.SearchResults;
import com.morgan.declan.samplelogin.Upload;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;

    private ArrayList<Post> postsFromSearchAL;
    private ArrayList<Upload> uploadsFromSearchAL;
    private RecyclerView search_rv;
    public RecyclerView.Adapter mAdapter;

    private Spinner conditionSpinner, sizeSpinner;
    private String sizeSelected, conditionSelected;
    private Button searchBtn;
    private EditText title, brand, colour , distance;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        postsFromSearchAL = new ArrayList<>();
        uploadsFromSearchAL = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        Location location = ItemArrayAdapter.getLocation();

//        searchDB();
//        mAdapter = new ItemArrayAdapter(getContext(), postsFromSearchAL, uploadsFromSearchAL, R.layout.dashboard_item);
//
//        //adding adapter to recyclerview
//        search_rv.setAdapter(mAdapter);
//
//        mAdapter.notifyDataSetChanged();
        searchBtn = root.findViewById(R.id.search_button);
        conditionSpinner = root.findViewById(R.id.item_condition_spin);
        sizeSpinner = root.findViewById(R.id.item_size_spin);

        title = root.findViewById(R.id.search_title);
        brand = root.findViewById(R.id.search_brand);
        colour = root.findViewById(R.id.search_colour);
        distance = root.findViewById(R.id.distance);


        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sizeSelected = sizeSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                sizeSelected = null;
            }
        });
        conditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                conditionSelected = conditionSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                conditionSelected = null;
            }

        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleText = title.getText().toString().toLowerCase();
                String brandText = brand.getText().toString().toLowerCase();
                String colourText = brand.getText().toString().toLowerCase();
                String distanceText = distance.getText().toString().toLowerCase();
                //searchFirebase(titleText, brandText, colourText);
                Intent resultsPage = new Intent(getActivity(), SearchResults.class);
                resultsPage.putExtra("titleSearch", titleText);
                resultsPage.putExtra("brandSearch", brandText);
                resultsPage.putExtra("colourText", colourText);
                startActivity(resultsPage);

            }
        });
        return root;
    }


    public void searchFirebase(String titleSearch, String brandSearch, final String colourSearch){
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("posts");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                   String pid = ds.getKey();
                   Query dbQuery = mRef.child(pid).orderByChild("colour").equalTo(colourSearch);
                   dbQuery.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           for(DataSnapshot dsPosts : dataSnapshot.getChildren()){
                               Post postFromSearch = dsPosts.getValue(Post.class);
                               postsFromSearchAL.add(postFromSearch);
                               getImages(postFromSearch.getUid(), postFromSearch.getPid());
                               Log.e("DS", dsPosts.getValue().toString());
                               mAdapter.notifyDataSetChanged();
                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mAdapter = new ItemArrayAdapter(getContext(), postsFromSearchAL, uploadsFromSearchAL, R.layout.dashboard_item);

        //adding adapter to recyclerview
        search_rv.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

    }

    public void getImages(final String alUid, final String alPid){
        final DatabaseReference imageDBRef = FirebaseDatabase.getInstance().getReference().child("picture_uploads")
                .child(alUid).child(alPid);
        imageDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Outright DS", dataSnapshot.toString());

                Upload userUpload = dataSnapshot.getValue(Upload.class);
                Log.e("userUpload", userUpload.getUrl());
                uploadsFromSearchAL.add(userUpload);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mAdapter = new ItemArrayAdapter(getContext(), postsFromSearchAL, uploadsFromSearchAL, R.layout.dashboard_item);

        //adding adapter to recyclerview
        search_rv.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<Post> getPostsFromSearchAL(){
        return this.getPostsFromSearchAL();
    }

    public ArrayList<Upload> getUploadsFromSearchAL(){
        return this.uploadsFromSearchAL;
    }

}