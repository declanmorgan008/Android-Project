package com.morgan.declan.samplelogin;

import android.location.Location;
import android.net.Uri;

import com.google.firebase.database.FirebaseDatabase;

import java.net.URI;
import java.util.ArrayList;

public class User {

    public String username;
    public String email;
    public long time;
    public Double latitude,longitude;
    public String photoUri;
    public String address;

    public ArrayList<Post> userPosts;

    public User(){

    }

    public User(String uname, String email, String address, Double latitude, Double longitude){
        this.username = uname;
        this.email = email;
        this.time = System.currentTimeMillis();
        this.photoUri = "empty";

        this.address = address;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public User(String uname, String email, String photoUri, String address, Double latitude, Double longitude){
        this.username = uname;
        this.email = email;

        this.time = System.currentTimeMillis();
        this.photoUri = photoUri;
        this.address = address;

        this.latitude = latitude;
        this.longitude = longitude;

    }

    public void writeUser(String userId) {
        User newUser = new User(this.username, this.email, this.photoUri, this.address, this.latitude, this.longitude);
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(newUser);
    }

    public String GetLocationAddress(){ return address;}

    public String getEmail(){
        return this.email;
    }

}
