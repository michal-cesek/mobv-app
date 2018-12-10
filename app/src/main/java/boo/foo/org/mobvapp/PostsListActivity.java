package boo.foo.org.mobvapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;

import boo.foo.org.mobvapp.models.Post;
import boo.foo.org.mobvapp.models.User;
import boo.foo.org.mobvapp.services.PostsService;
import boo.foo.org.mobvapp.services.UserService;

public class PostsListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private UserService userService;
    private PostsService postsService;
    private User user;

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.postslist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // force to slide one post per slide
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //get user
        userService = new UserService(this);
        user = userService.getCurrentUser();

        PostsListActivity context = this;
        // specify an adapter (see also next example)
        postsService = new PostsService();
        postsService.getPosts(user.id,
                (posts) -> {
                    Post[] postsArray = new Post[posts.size()];
                    postsArray = posts.toArray(postsArray);
                    mAdapter = new MyAdapter(postsArray, context);
                    mRecyclerView.setAdapter(mAdapter);

                    //Log.d("ProfileActivity", String.valueOf(posts.size()));
                    return null;
                }, (err) -> {
                    return null;
                }
        );

    }
    // ...
}
