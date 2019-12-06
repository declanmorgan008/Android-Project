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
    public Double latitude,longitude;
    public String photoUri;
    public String address;

    public User(){

    }

    /**
     * Creates a user instance
     * @param email user email address provided on registration.
     * @param uname username stated by user on registration.*/
    public User(String uname, String email, String address, Double latitude, Double longitude){
        this.username = uname;
        this.email = email;
        this.time = System.currentTimeMillis();
        this.photoUri = "empty";

        this.address = address;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Creates a user instance that can be used to write data to Firebase Database.
     * @param uname username provided by user on registration.
     * @param email user email address provided on registration.
     * @param location users location when registering.
     * @param photoUri user display picture URI.
     * */
    public User(String uname, String email, String photoUri, String address, Double latitude, Double longitude){
        this.username = uname;
        this.email = email;

        this.time = System.currentTimeMillis();
        this.photoUri = photoUri;
        this.address = address;

        this.latitude = latitude;
        this.longitude = longitude;

    }

    /**
     * Writes user to Firebase Database as a child of 'users' using their userID as key.
     * @param userId ID of user provided by Firebase when writing new child to database.*/
    public void writeUser(String userId) {
        User newUser = new User(this.username, this.email, this.photoUri, this.address, this.latitude, this.longitude);
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(newUser);
    }


    public String GetLocationAddress(){ return address;}

    /**
     * @return user email address.*/
    public String getEmail(){
        return this.email;
    }

}
