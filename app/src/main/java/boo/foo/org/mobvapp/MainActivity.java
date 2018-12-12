package boo.foo.org.mobvapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import boo.foo.org.mobvapp.models.Post;
import boo.foo.org.mobvapp.services.PostsService;
import boo.foo.org.mobvapp.services.UserService;

import static com.crashlytics.android.core.CrashlyticsCore.TAG;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity:";

    private UserService userService;
    private PostsService postsService;

    private List<Post> posts;
    private ProgressBar pbMain;

    static final int OPEN_IMAGE_PICKER = 123;
    static final int OPEN_VIDEO_PICKER = 124;


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12345;
    private boolean storagePermissionsGranted = false;

    private final List<String> supportedImageTypes = Arrays.asList("image/jpeg", "image/png");
    private final List<String> supportedVideoTypes = Arrays.asList("video/mp4");

    private RecyclerView pRecyclerView;
    private RecyclerView.Adapter pAdapter;
    private RecyclerView.Adapter sAdapter;
    private RecyclerView.LayoutManager hLayoutManager;
    private RecyclerView.LayoutManager vLayoutManager;
    private MainActivity context;
    private SimpleExoPlayer player;


    @Override
    public void onStart() {
        super.onStart();
        storagePermissionsGranted = permissionsGranted();

        postsService.getAllPosts(
                posts -> {
                    this.posts = posts; //???
                    pbMain.setVisibility(View.INVISIBLE);
                    Post[] postsArray = new Post[posts.size()];
                    postsArray = posts.toArray(postsArray);
                    pAdapter = new MainActivity.PrimaryAdapter(postsArray);
                    pRecyclerView.setAdapter(pAdapter);
                    return null;
                },
                err -> {
                    pbMain.setVisibility(View.INVISIBLE);
                    return null;
                }
        );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSpeedDialMenu();
        pbMain = findViewById(R.id.pb_main);

        context = this;

        userService = new UserService(this);
        postsService = new PostsService();

        pRecyclerView = (RecyclerView) findViewById(R.id.primary_recycle_view);

        player = ExoPlayerFactory.newSimpleInstance(context);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        pRecyclerView.setHasFixedSize(true);

        // force to slide one post per slide
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(pRecyclerView);

        // use a linear layout manager
        hLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        pRecyclerView.setLayoutManager(hLayoutManager);
        pRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                player.seekTo(0);
                player.setPlayWhenReady(true);
            }
        });

    }

    public void persistPost(File file, Post newPost) {
        postsService.addPost(this, file, newPost,
                post -> {
                    Log.d(TAG, "onPostImageSelect add successful " + post.getId());
                    pbMain.setVisibility(View.INVISIBLE);

                    //todo should be hidden
                    userService.increaseUserPostsNumber();

                    Toast.makeText(this, getString(R.string.toast_post_add_success),
                            Toast.LENGTH_LONG)
                            .show();
                    return null;
                },
                err -> {
                    Log.d(TAG, "onPostImageSelect add fail");
                    pbMain.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, getString(R.string.toast_post_add_fail),
                            Toast.LENGTH_LONG)
                            .show();
                    return null;
                }
        );
    }

    public void onPostImageSelect(String filePath) {
        Log.d(TAG, "onPostImageSelect " + filePath);

        if (Utils.checkIfIsSupportedFileType(filePath, supportedImageTypes)) {
            File file = new File(filePath);

            Post newPost = new Post();
            newPost.setType("image");
            newPost.setUserid(userService.getCurrentUser().getId());
            newPost.setUsername(userService.getCurrentUser().getUsername());

            pbMain.setVisibility(View.VISIBLE);
            persistPost(file, newPost);

        } else {
            Toast.makeText(this, getString(R.string.error_not_supported_file_type),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void onPostVideoSelect(String filePath) {
        Log.d(TAG, "onPostVideoSelect " + filePath);

        if (Utils.checkIfIsSupportedFileType(filePath, supportedVideoTypes)) {
            File file = new File(filePath);

            Post newPost = new Post();
            newPost.setType("video");
            newPost.setUserid(userService.getCurrentUser().getId());
            newPost.setUsername(userService.getCurrentUser().getUsername());

            pbMain.setVisibility(View.VISIBLE);
            persistPost(file, newPost);

        } else {
            Toast.makeText(this, getString(R.string.error_not_supported_file_type),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }


    // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
    public void tryOpenMediaPicker(int mode, int resCode) {
        if (storagePermissionsGranted) {
            Intent intent = new Intent(this, Gallery.class);
            // Set the title
            intent.putExtra("title", "Select media");
            intent.putExtra("mode", mode);
            intent.putExtra("maxSelection", 1); // Optional
            startActivityForResult(intent, resCode);
        } else {
            requestPermisions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case OPEN_IMAGE_PICKER: {
                if (resultCode == RESULT_OK && data != null) {
                    String filePath = data.getStringArrayListExtra("result").get(0);
                    onPostImageSelect(filePath);
                }
                //TODO toas some error
            }
            break;
            case OPEN_VIDEO_PICKER: {
                if (resultCode == RESULT_OK && data != null) {
                    String filePath = data.getStringArrayListExtra("result").get(0);
                    onPostVideoSelect(filePath);
                }
            }
            break;
        }
    }


    // ----------------------------- PERMISSIONS and others

    public boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermisions() {
        if (!permissionsGranted()) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

//                CharSequence message = "Permisions needed to add images/videos";
//                Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
//                toast.show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Log.d(TAG, "Permission has already been granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "Permisions granted");
                    storagePermissionsGranted = true;
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Log.d(TAG, "Permisions rejected");
                    storagePermissionsGranted = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }


    private void setSpeedDialMenu() {
        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.menu_speed_dial);
        speedDialView.setOnActionSelectedListener(speedDialActionItem -> {
            switch (speedDialActionItem.getId()) {
                case R.id.action_logout:
                    logout();
                    return false;
                case R.id.action_show_profile:
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                    return false;
                case R.id.action_add_video:
                    tryOpenMediaPicker(3, OPEN_VIDEO_PICKER);
                    return false;
                case R.id.action_add_image:
                    tryOpenMediaPicker(2, OPEN_IMAGE_PICKER);
                    return false;
                default:
                    //true to keep menu open
                    return false;
            }
        });
    }

    public void logout() {
        userService.logout();
        goToLoginScreen();
    }

    public void goToLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        return;
    }



    private class CustomLinearLayoutManager extends LinearLayoutManager {

        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollHorizontally() {
            return false;
        }
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

        }

        // This get called in PrimaryAdapter onBindViewHolder method
        public void bindViews( String userId) {

            // create vertical layout manager
            vLayoutManager = new MainActivity.CustomLinearLayoutManager(context);

            sRecyclerView.setLayoutManager(vLayoutManager);
            postsService.getPosts(userId,
                    (posts) -> {
                        Post[] postsArray = new Post[posts.size()];
                        postsArray = posts.toArray(postsArray);
                        sAdapter = new MainActivity.SecondaryAdapter(postsArray, context);
                        sRecyclerView.setAdapter(sAdapter);
                        sRecyclerView.scrollToPosition(1);

                        //Log.d("ProfileActivity", String.valueOf(posts.size()));
                        return null;
                    }, (err) -> {
                        return null;
                    }
            );

            sRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    player.seekTo(0);
                    player.setPlayWhenReady(true);

                }
            });

        }

    }
    private class PrimaryAdapter extends RecyclerView.Adapter<MainActivity.PrimaryViewHolder> {
        private Post[] mDataset;

        // Provide a suitable constructor (depends on the kind of dataset)
        public PrimaryAdapter(Post[] post) {
            mDataset = post;
//            viewPool = new RecyclerView.RecycledViewPool(); //TODO:
        }


        // Create new views (invoked by the layout manager)
        public MainActivity.PrimaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.primary_recycler_view_item, parent, false);

//            .setRecycledViewPool(viewPool);               //TODO:
            return new MainActivity.PrimaryViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MainActivity.PrimaryViewHolder holder, int position) {
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

    //TODO: tu chceme viewholder pre profile
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    private class SecondaryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView postUser;
        public TextView postDate;
        public ImageView postContent;
        public PlayerView postContentVideo;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public SecondaryViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            postUser = (TextView) itemView.findViewById(R.id.post_user);
            postDate = (TextView) itemView.findViewById(R.id.post_date);
            postContent = (ImageView) itemView.findViewById(R.id.post_content);
            postContentVideo = (PlayerView) itemView.findViewById(R.id.post_content_video);
        }
    }

    private class SecondaryAdapter extends RecyclerView.Adapter<MainActivity.SecondaryViewHolder> {
        private Post[] mDataset;
        private Context postContext;

        // Provide a suitable constructor (depends on the kind of dataset)
        public SecondaryAdapter(Post[] post, Context context) {
            mDataset = post;
            postContext = context;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MainActivity.SecondaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_post, parent, false);
            return new MainActivity.SecondaryViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MainActivity.SecondaryViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            Log.d(TAG, mDataset[position].getType());
            if (mDataset[position].getType().equals("image"))
            {
                String url = mDataset[position].getImageurl();
                Glide.with(postContext.getApplicationContext())
                        .load(url)
                        .into(holder.postContent);
                holder.postContent.setVisibility(View.VISIBLE);
                holder.postContentVideo.setVisibility(View.INVISIBLE);
                player.stop();

            } else if (mDataset[position].getType().equals("video"))
            {
                //player = ExoPlayerFactory.newSimpleInstance(context);
                // This is the MediaSource representing the media to be played.
                String url = mDataset[position].getVideourl();
                Uri uri = Uri.parse(url);

                MediaSource mediaSource = buildMediaSource(uri);
                // Prepare the player with the source.
                player.prepare(mediaSource);
                player.setPlayWhenReady(true);

                holder.postContentVideo.setPlayer(player);

                holder.postContentVideo.setVisibility(View.VISIBLE);
                holder.postContent.setVisibility(View.INVISIBLE);

            }

            holder.postUser.setText(mDataset[position].getUsername());
            holder.postUser.bringToFront();
            holder.postDate.setText(mDataset[position].getDate().toDate().toString());
            holder.postDate.bringToFront();
        }
        private MediaSource buildMediaSource(Uri uri) {
            return new ExtractorMediaSource.Factory(
                    new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                    createMediaSource(uri);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }


    }

    
}
