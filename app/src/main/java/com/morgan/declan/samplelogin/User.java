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
    public Location location;
    public String photoUri;

    public ArrayList<Post> userPosts;

    public User(){

    }

    public User(String uname, String email){
        this.username = uname;
        this.email = email;
        this.time = System.currentTimeMillis();
        this.photoUri = "empty";
    }

    public User(String uname, String email, Location location, String photoUri){
        this.username = uname;
        this.email = email;
        this.location = location;
        this.time = System.currentTimeMillis();
        this.photoUri = photoUri;

    }

    public void writeUser(String userId) {
        User newUser = new User(this.username, this.email, this.location, this.photoUri);
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(newUser);
    }


    public void setLocation(Location userLocation){
        this.location = userLocation;
    }


    public String getEmail(){
        return this.email;
    }

}
