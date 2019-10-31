package com.morgan.declan.samplelogin;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Upload{

    public String name;
    public String url;
    public String post_id;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Upload() {
    }

    public Upload(String name, String url, String post_id) {
        this.name = name;
        this.url= url;
        this.post_id = post_id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getPost_id(){
        return this.post_id;
    }
}
