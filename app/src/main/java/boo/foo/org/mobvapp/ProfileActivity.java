package boo.foo.org.mobvapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        String dateStr = Utils.getFormatedDate("MM-dd-yyyy",user.getDate().toDate());

        tv_username.setText(user.getUsername());
        tv_registred.setText(dateStr);
        tv_post_count.setText(String.valueOf(user.getNumberOfPosts()));
    }


}
