package com.morgan.declan.samplelogin;

import android.location.Location;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class User {

    public String username;
    public String email;
    public long time;
    public Location location;

    public ArrayList<Post> userPosts;

    public User(){

    }

    public User(String uname, String email){
        this.username = uname;
        this.email = email;
        this.time = System.currentTimeMillis();
    }

    public User(String uname, String email, Location location){
        this.username = uname;
        this.email = email;
        this.location = location;
        this.time = System.currentTimeMillis();
    }

    public void writeUser(String userId) {
        User newUser = new User(this.username, this.email, this.location);
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(newUser);
    }


    public void setLocation(Location userLocation){
        this.location = userLocation;
    }


    public String getEmail(){
        return this.email;
    }

}
