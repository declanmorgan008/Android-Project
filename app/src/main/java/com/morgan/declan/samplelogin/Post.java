package com.morgan.declan.samplelogin;

import com.google.firebase.database.FirebaseDatabase;

public class Post {

    public String postTitle;
    public String size;
    public String colour;
    public String brand;
    public String condition;
    public String description;

    public Post(){

    }

    public Post(String title, String size, String colour, String brand,
                String condition, String description){
        this.postTitle = title;
        this.size = size;
        this.colour = colour;
        this.brand = brand;
        this.condition = condition;
        this.description = description;

    }

    public void postToDatabase (){
        Post myPost = new Post(this.postTitle, this.size, this.colour, this.brand, this.condition, this.description);
        String postId = FirebaseDatabase.getInstance().getReference().child("posts").push().getKey();
        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).setValue(myPost);
    }

}
