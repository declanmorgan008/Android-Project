package com.morgan.declan.samplelogin.ui.search;

import android.content.Intent;
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

/**
 * Calls an intent to search results activity with extra data containing the text
 * inputted by the user into each search field.*/
public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;

    private ArrayList<Post> postsFromSearchAL;
    private ArrayList<Upload> uploadsFromSearchAL;
    private RecyclerView search_rv;
    public RecyclerView.Adapter mAdapter;

    private Spinner conditionSpinner, sizeSpinner;
    private String sizeSelected, conditionSelected;
    private Button searchBtn;
    private EditText title, brand, colour;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        postsFromSearchAL = new ArrayList<>();
        uploadsFromSearchAL = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        searchBtn = root.findViewById(R.id.search_button);
        conditionSpinner = root.findViewById(R.id.item_condition_spin);
        sizeSpinner = root.findViewById(R.id.item_size_spin);

        title = root.findViewById(R.id.search_title);
        brand = root.findViewById(R.id.search_brand);
        colour = root.findViewById(R.id.search_colour);

        //Set size spinner with all options from strings xml file.
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

        //Set condition spinner with all options from strings xml file.
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

        //calls an intent to search results activity with text from each field as extra data.
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleText = title.getText().toString().toLowerCase();
                String brandText = brand.getText().toString().toLowerCase();
                String colourText = brand.getText().toString().toLowerCase();
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

}