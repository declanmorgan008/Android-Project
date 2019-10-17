package com.morgan.declan.samplelogin;

import com.google.firebase.database.FirebaseDatabase;

public class User {

    public String username;
    public String email;
    public long time;

    public User(){

    }

    public User(String uname, String email){
        this.username = uname;
        this.email = email;
        this.time = System.currentTimeMillis();
    }

    public void writeUser(String userId) {
        User newUser = new User(this.username, this.email);
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(newUser);
    }


}
