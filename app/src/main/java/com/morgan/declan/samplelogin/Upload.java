package com.morgan.declan.samplelogin;
import com.google.firebase.database.IgnoreExtraProperties;


/**
 * Creates an upload object to store in Firebase Database. Represents an image
 * stored in Firebase Storage to a post stored in Firebase Database.
 * */
@IgnoreExtraProperties
public class Upload{

    public String name;
    public String url;
    public String post_id;

    public Upload() {
    }

    /**
     * Creats new upload object with name, photo url and post id number.
     * @param name Name of the uploaded image.
     * @param url URL of image to represent a post.
     * @param post_id ID of post that is connected to image.*/
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
