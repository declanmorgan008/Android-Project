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

    public String getPostTitle(){
        return this.postTitle;
    }

    public void setPostTitle(String postTitle){
        this.postTitle = postTitle;
    }

    public String getSize(){
        return this.size;
    }

    public void setSize(String size){
        this.size = size;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand){
        this.brand = brand;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour){
        this.colour = colour;
    }

    public String getCondition() {
        return condition;
    }

    public void setCndition(String condition){
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc){
        this.description = desc;
    }

    public String getPid(){
        return this.pid;
    }

    public void setPid(){
        this.pid = FirebaseDatabase.getInstance().getReference().child("posts").push().getKey();
    }



    public void setPicture(Uri uri){
        this.picture = uri;
    }

    public Uri getPicture(){
        return this.picture;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

}
