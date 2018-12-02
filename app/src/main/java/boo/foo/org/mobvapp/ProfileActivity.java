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

        userService = new UserService(this);
        postsService = new PostsService();

        User user = userService.getCurrentUser();

        tv_username.setText(user.getUsername());
        tv_registred.setText(user.getDate().toDate().toString());
//        tv_post_count.setText(user.getNumberOfPosts());

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


}
