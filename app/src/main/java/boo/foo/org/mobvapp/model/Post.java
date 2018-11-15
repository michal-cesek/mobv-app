package boo.foo.org.mobvapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;

public class Post extends Model {

    public static String collectionName = "posts";

    private String userid;
    private String username;
    @ServerTimestamp
    private Timestamp date;
    // image/video
    private String type;
    private String imageurl;
    private String videourl;


    public Post() {
    }

    public Post(String id, String userid, String username, Date date, String type, String videourl, String imageurl) {
        this.id = id;
        this.userid = userid;
        this.username = username;
        this.type = type;
        this.videourl = videourl;
        this.imageurl = imageurl;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
