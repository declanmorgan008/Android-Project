package com.morgan.declan.samplelogin;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;


/**
 * Stores specific details for a specific post instance.
 * */
public class Post {

    public String pid;
    public String postTitle;
    public String size;
    public String colour;
    public String brand;
    public String condition;
    public String description;
    public String uid;
    public Uri picture;
    public StorageReference pictureReference;

    private StorageReference mStorageRef;
    public Post(){};
    public Post(String name, String size, String colour, String brand, String condition, String description){

    }

    public Post(String title, String size, String colour, String brand,
                String condition, String description, String uid){
        this.postTitle = title;
        this.size = size;
        this.colour = colour;
        this.brand = brand;
        this.condition = condition;
        this.description = description;
        this.uid = uid;

    }

    public Post(String title, String size, String colour, String brand,
                String condition, String description, String uid, Uri picture){
        this.postTitle = title;
        this.size = size;
        this.colour = colour;
        this.brand = brand;
        this.condition = condition;
        this.description = description;
        this.uid = uid;
        this.picture = picture;
    }

    /**
     * Uploads a post instance to Firebase Database.
     * @param myPost post to be uploaded to Firebase.*/
    public void postToDatabase (Post myPost){
        mStorageRef = FirebaseStorage.getInstance().getReference();
        Post newPost = myPost;
        Log.e("sampleLogIn", this.getPostTitle() + ", "
                + ", "  + this.getPid()
                + ", "  +               this.getBrand()
                + ", "  +               this.getColour()
                + ", "  +              this.getCondition()
                + ", "  +               this.getSize()
                + ", "  + this.getPicture());

        FirebaseDatabase.getInstance().getReference().child("posts").child(uid).child(newPost.getPid()).setValue(newPost);
        Log.e("sampleLogIn", "Completed Post Upload");
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("posts").setValue(newPost.getPid());
        Log.e("sampleLogIn", "Completed Post Upload");
    }

    /**
     * @return post title*/
    public String getPostTitle(){
        return this.postTitle;
    }

    /**
     * @return item size.*/
    public String getSize(){
        return this.size;
    }

    /**
     * Sets an item size for a post.
     * */
    public void setSize(String size){
        this.size = size;
    }

    /**
     * @return brand of item.
     * */
    public String getBrand() {
        return brand;
    }

    /**
     * Sets an item brand for a post.
     * */
    public void setBrand(String brand){
        this.brand = brand;
    }

    /**
     * @return colour of item.
     * */
    public String getColour() {
        return colour;
    }

    /**
     * Sets an item colour for a post.
     * */
    public void setColour(String colour){
        this.colour = colour;
    }

    /**
     * @return item condition.
     * */
    public String getCondition() {
        return condition;
    }

    /**
     * Sets an item condition for a post.
     * */
    public void setCondition(String condition){
        this.condition = condition;
    }
    /**
     * @return item description.
     * */
    public String getDescription() {
        return description;
    }

    /**
     * Sets an item description for a post.
     * */
    public void setDescription(String desc){
        this.description = desc;
    }

    /**
     * @return post ID.
     * */
    public String getPid(){
        return this.pid;
    }

    /**
     * Sets an item ID for a post by retrieving a new ID from Firebase.
     * */
    public void setPid(){
        this.pid = FirebaseDatabase.getInstance().getReference().child("posts").push().getKey();
    }

    /**
     * Sets an item picture uri for a post.
     * */
    public void setPicture(Uri uri){
        this.picture = uri;
    }

    /**
     * @return post image URI.
     * */
    public Uri getPicture(){
        return this.picture;
    }

    /**
     * @return post user ID.
     * */
    public String getUid(){
        return this.uid;
    }
    /**
     * Sets an posts User ID.
     * */
    public void setUid(String uid){
        this.uid = uid;
    }

}
