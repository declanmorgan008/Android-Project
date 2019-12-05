package com.morgan.declan.samplelogin;

import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.morgan.declan.samplelogin.ui.additem.AddItemFragment;

import java.io.File;
import java.io.IOException;

import static java.security.AccessController.getContext;

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
    public String address;
    public Double latitude;
    public Double longitude;
    public Location loc;

    public StorageReference pictureReference;

    private StorageReference mStorageRef;
    public Post(){};
    public Post(String name, String size, String colour, String brand, String condition, String description , String address ,  Double latitude, Double longitude){

    }

    public Post(String title, String size, String colour, String brand,
                String condition, String description, String uid , String address, Double latitude, Double longitude ){
        this.postTitle = title;
        this.size = size;
        this.colour = colour;
        this.brand = brand;
        this.condition = condition;
        this.description = description;
        this.uid = uid;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public Post(String title, String size, String colour, String brand,
                String condition, String description, String uid, Uri picture, String address, Double latitude, Double longitude){
        this.postTitle = title;
        this.size = size;
        this.colour = colour;
        this.brand = brand;
        this.condition = condition;
        this.description = description;
        this.uid = uid;
        this.picture = picture;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;

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
                + ", "  + this.getPicture()
                + ", "  +               this.getAddress()
                + ", "  +               this.getLatitude()
                + ", "  +               this.getLongitude());

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

    public String getUid(){
        return this.uid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getAddress(){
        return this.address;
    }

    public void setAddress(String address){ this.address = address;
    }

    public Double getLatitude(){
        return this.latitude;
    }

    public void setLatitude(Double latitude){ this.latitude = latitude;
    }

    public Double getLongitude(){
        return this.longitude;
    }

    public void setLongitude(Double longitude){ this.longitude = longitude;
    }

}
