package com.morgan.declan.samplelogin;

import android.view.View;

/**
 * Interface for click listener to be attached to each item in recycler view
 * for managing posts.*/
public interface RecyclerViewClickListener {
    void onClick(View view, Post postToDelete);
}
