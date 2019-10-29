package com.morgan.declan.samplelogin.ui.additem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;

import java.util.ArrayList;


public class AddItemFragment extends Fragment {

    private AddItemViewModel addItemViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        addItemViewModel =
                ViewModelProviders.of(this).get(AddItemViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_additem, container, false);

        final FloatingActionButton postButton = root.findViewById(R.id.floatingAddPostButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"well hello",Toast.LENGTH_SHORT).show();
                postItem(root);
            }
        });

        return root;
    }
    private void postItem(View root){
        EditText nameText = root.findViewById(R.id.addItemName);
        String name = nameText.getText().toString();
        EditText sizeText = root.findViewById(R.id.addItemSize);
        String size = sizeText.getText().toString();
        EditText brandText = root.findViewById(R.id.addItemBrand);
        String brand = brandText.getText().toString();
        EditText colourText = root.findViewById(R.id.addItemColour);
        String colour = colourText.getText().toString();
        EditText conditionText = root.findViewById(R.id.addItemCondition);
        String condition = conditionText.getText().toString();
        EditText descriptionText = root.findViewById(R.id.addItemDescription);
        String description = descriptionText.getText().toString();
        Post post = new Post(name,size,colour,brand,condition,description);
        post.postToDatabase();
    }


}