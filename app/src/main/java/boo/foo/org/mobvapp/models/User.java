package boo.foo.org.mobvapp.models;


import com.google.firebase.Timestamp;


public class User extends Model {

    public static String collectionName = "users";

    private String username;
    //"datetime of registered"
    private Timestamp date;
    private Integer numberOfPosts;

    public User() {
    }

    public User(String id, String username, Timestamp date, Integer numberOfPosts) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.numberOfPosts = numberOfPosts;
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

    public Integer getNumberOfPosts() {
        return numberOfPosts;
    }

    public void setNumberOfPosts(Integer numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }
}
