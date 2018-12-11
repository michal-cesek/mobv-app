package boo.foo.org.mobvapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import boo.foo.org.mobvapp.models.Post;
import boo.foo.org.mobvapp.models.User;
import boo.foo.org.mobvapp.services.PostsService;
import boo.foo.org.mobvapp.services.UserService;

import static com.crashlytics.android.core.CrashlyticsCore.TAG;

public class PostsListActivity extends AppCompatActivity {

    private RecyclerView pRecyclerView;
    private RecyclerView.Adapter pAdapter;
    private RecyclerView.Adapter sAdapter;
    private RecyclerView.LayoutManager hLayoutManager;
    private RecyclerView.LayoutManager vLayoutManager;
    private UserService userService;
    private PostsService postsService;
    private User user;
    private PostsListActivity context;

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_posts_list);

        pRecyclerView = (RecyclerView) findViewById(R.id.primary_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        pRecyclerView.setHasFixedSize(true);

        //disable scrolling
//        pRecyclerView.setNestedScrollingEnabled(false);

        // force to slide one post per slide
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(pRecyclerView);

        // use a linear layout manager
        hLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        pRecyclerView.setLayoutManager(hLayoutManager);

        // get user
        userService = new UserService(this);

        // save context
        context = this;

        // specify an adapter (see also next example)
        postsService = new PostsService();
        postsService.getAllPosts(
                (posts) -> {
                    Post[] postsArray = new Post[posts.size()];
                    postsArray = posts.toArray(postsArray);
                    pAdapter = new PrimaryAdapter(postsArray);
                    pRecyclerView.setAdapter(pAdapter);

                    //Log.d("ProfileActivity", String.valueOf(posts.size()));
                    return null;
                }, (err) -> {
                    return null;
                }
        );

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    private class PrimaryViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView sRecyclerView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public PrimaryViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            sRecyclerView = (RecyclerView) itemView.findViewById(R.id.secondary_recycle_view);

            // force to slide one post per slide
            SnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(sRecyclerView);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            sRecyclerView.setHasFixedSize(true);

            //disable scrolling
//            sRecyclerView.setNestedScrollingEnabled(false);
        }

        // This get called in PrimaryAdapter onBindViewHolder method
        public void bindViews( String userId) {

            // create vertical layout manager
            vLayoutManager = new LinearLayoutManager(context);

            sRecyclerView.setLayoutManager(vLayoutManager);
            postsService.getPosts(userId,
                    (posts) -> {
                        Post[] postsArray = new Post[posts.size()];
                        postsArray = posts.toArray(postsArray);
                        sAdapter = new SecondaryAdapter(postsArray, context);
                        sRecyclerView.setAdapter(sAdapter);

                        //Log.d("ProfileActivity", String.valueOf(posts.size()));
                        return null;
                    }, (err) -> {
                        return null;
                    }
            );

        }

    }
    private class PrimaryAdapter extends RecyclerView.Adapter<PrimaryViewHolder> {
        private Post[] mDataset;

        // Provide a suitable constructor (depends on the kind of dataset)
        public PrimaryAdapter(Post[] post) {
            mDataset = post;
//            viewPool = new RecyclerView.RecycledViewPool(); //TODO:
        }


        // Create new views (invoked by the layout manager)
        public PrimaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.primary_recycler_view_item, parent, false);

//            .setRecycledViewPool(viewPool);               //TODO:
            return new PrimaryViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(PrimaryViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            String userId = mDataset[position].getUserid();
            holder.bindViews(userId);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    private class SecondaryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView postUser;
        public TextView postDate;
        public ImageView postContent;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public SecondaryViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);


            postUser = (TextView) itemView.findViewById(R.id.post_user);
            postDate = (TextView) itemView.findViewById(R.id.post_date);
            postContent = (ImageView) itemView.findViewById(R.id.post_content);
        }
    }

    private class SecondaryAdapter extends RecyclerView.Adapter<SecondaryViewHolder> {
        private Post[] mDataset;
        private Context postContext;

        // Provide a suitable constructor (depends on the kind of dataset)
        public SecondaryAdapter(Post[] post, Context context) {
            mDataset = post;
            postContext = context;
        }


        // Create new views (invoked by the layout manager)
        @Override
        public SecondaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_post, parent, false);
            return new SecondaryViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(SecondaryViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            Log.d(TAG, mDataset[position].getType());
            if (mDataset[position].getType().equals("image"))
            {
                String url = mDataset[position].getImageurl();
                Glide.with(postContext.getApplicationContext())
                        .load(url)
                        .into(holder.postContent);
            }

            holder.postUser.setText(mDataset[position].getUsername());
            holder.postUser.bringToFront();
            holder.postDate.setText(mDataset[position].getDate().toDate().toString());
            holder.postDate.bringToFront();
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }
}
