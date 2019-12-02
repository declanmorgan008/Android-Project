package com.morgan.declan.samplelogin;

import android.location.Location;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;


/**
 * Creates a user instance storing user details to be posted to Firebase Database.
 * */
public class User {

    public String username;
    public String email;
    public long time;
    public Location location;
    public String photoUri;

    public User(){

    }

    /**
     * Creates a user instance
     * @param email user email address provided on registration.
     * @param uname username stated by user on registration.*/
    public User(String uname, String email){
        this.username = uname;
        this.email = email;
        this.time = System.currentTimeMillis();
        this.photoUri = "empty";
    }

    /**
     * Creates a user instance that can be used to write data to Firebase Database.
     * @param uname username provided by user on registration.
     * @param email user email address provided on registration.
     * @param location users location when registering.
     * @param photoUri user display picture URI.
     * */
    public User(String uname, String email, Location location, String photoUri){
        this.username = uname;
        this.email = email;
        this.location = location;
        this.time = System.currentTimeMillis();
        this.photoUri = photoUri;

    }

    /**
     * Writes user to Firebase Database as a child of 'users' using their userID as key.
     * @param userId ID of user provided by Firebase when writing new child to database.*/
    public void writeUser(String userId) {
        User newUser = new User(this.username, this.email, this.location, this.photoUri);
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(newUser);
    }

    /**
    * set the users location.
     * @param userLocation new location of user.*/
    public void setLocation(Location userLocation){
        this.location = userLocation;
    }

    /**
     * @return user email address.*/
    public String getEmail(){
        return this.email;
    }

}
