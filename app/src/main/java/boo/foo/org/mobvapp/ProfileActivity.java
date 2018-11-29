package boo.foo.org.mobvapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import boo.foo.org.mobvapp.models.User;
import boo.foo.org.mobvapp.services.PostsService;
import boo.foo.org.mobvapp.services.UserService;

public class ProfileActivity extends AppCompatActivity {


    private UserService userService;
    private PostsService postsService;

    private ProgressBar pb;
    private LinearLayout ll;
    private TextView tv_username;
    private TextView tv_registred;
    private TextView tv_post_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pb = findViewById(R.id.pb_profile);
        ll = findViewById(R.id.ll_profile);
        tv_username = findViewById(R.id.tv_username);
        tv_registred = findViewById(R.id.tv_registred);
        tv_post_count = findViewById(R.id.tv_post_count);

        userService = new UserService();
        postsService = new PostsService();

        //todo
        String userName = "peter";
        //todo move to more appropriate lifecycle method
        login(userName);


    }

    private void onLoginSuccess(User user) {

        postsService.getPosts(user.id,
                (posts) -> {
                    if (posts.size() < 1) {
                        pb.setVisibility(View.INVISIBLE);
                        return null;
                    }
                    Log.d("ProfileActivity", String.valueOf(posts.size()));
                    return null;
                }, (err) -> {
                    return null;
                }
        );

//        Post post = new Post();
//        post.setUserid(user.getId());
//        post.setUsername(user.getUsername());
//        post.setType("image");
//        post.setImageurl("https://picsum.photos/g/1600/1600?image=123");
//            postsService.addPost(post, user);
    }


    private void login(String username) {
        userService.login(username,
                (u) -> {
                    if (u == null) {
                        //user not found
                        pb.setVisibility(View.INVISIBLE);
                        return null;
                    }

                    tv_username.setText(u.getUsername());
                    tv_registred.setText(u.getDate().toString());
                    //todo
                    tv_post_count.setText("posts: " + u.getNumberOfPosts());

                    onLoginSuccess(u);

                    pb.setVisibility(View.INVISIBLE);
                    ll.setVisibility(View.VISIBLE);
                    return null;
                }, (err) -> {
                    pb.setVisibility(View.INVISIBLE);
                    //todo show error
                    return null;
                }
        );
    }
}
