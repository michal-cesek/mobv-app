package boo.foo.org.mobvapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import boo.foo.org.mobvapp.models.Post;
import boo.foo.org.mobvapp.models.User;
import boo.foo.org.mobvapp.services.PostsService;
import boo.foo.org.mobvapp.services.UserService;

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

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        pRecyclerView.setHasFixedSize(true);

        // force to slide one post per slide
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(pRecyclerView);

        // use a linear layout manager
        hLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        pRecyclerView.setLayoutManager(hLayoutManager);

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
        public void bindViews(Post post) {
            User user;

            // create vertical layout manager
            vLayoutManager = new MainActivity.CustomLinearLayoutManager(context);

            sRecyclerView.setLayoutManager(vLayoutManager);

            userService.getUseRecordById(post.getUserid(),
                    (u) -> {
                        User us = (User) u.get(0);

                        List<Post> postsFiltered = posts.stream().filter(p -> p.getUserid().equals(post.getUserid())).collect(Collectors.toList());
                        postsFiltered.add(0, post);

                        Post[] postsArray = new Post[postsFiltered.size()];
                        postsArray = postsFiltered.toArray(postsArray);


                        User[] usersArray = new User[1];
                        usersArray[0] = us;

                        sAdapter = new MainActivity.SecondaryAdapter(context, postsArray, usersArray);
                        sRecyclerView.setAdapter(sAdapter);
                        sRecyclerView.scrollToPosition(1);

                        return null;
                    }, (err) -> {
                        return null;
                    }
            );

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
            //String userId = mDataset[position].getUserid();
            holder.bindViews(mDataset[position]);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

    private class ProfileViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_username;
        public TextView tv_registred;
        public TextView tv_post_count;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ProfileViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tv_username = (TextView) itemView.findViewById(R.id.tv_username);
            tv_registred = (TextView) itemView.findViewById(R.id.tv_registred);
            tv_post_count = (TextView) itemView.findViewById(R.id.tv_post_count);
        }

        public void populate(User data){
            String dateStr = Utils.getFormatedDate("MM-dd-yyyy",data.getDate().toDate());

            tv_username.setText(data.getUsername());
            tv_registred.setText(dateStr);
            tv_post_count.setText(String.valueOf(data.getNumberOfPosts()));
        }
    }
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    private class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView postUser;
        public TextView postDate;
        public ImageView postContent;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public PostViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            postUser = (TextView) itemView.findViewById(R.id.post_user);
            postDate = (TextView) itemView.findViewById(R.id.post_date);
            postContent = (ImageView) itemView.findViewById(R.id.post_content);
        }

        public void populate(Post data){

            if (data.getType().equals("image")) {
                String url = data.getImageurl();
                Glide.with(context.getApplicationContext())
                        .load(url)
                        .into(postContent);
            }
            //TODO: elseif video by exoplayer

            String dateStr = Utils.getFormatedDate("MM-dd-yyyy HH:mm",data.getDate().toDate());

            postUser.setText(data.getUsername());
            postUser.bringToFront();
            postDate.setText(dateStr);
            postDate.bringToFront();
        }
    }

    private class SecondaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final int VIEW_TYPE_PROFILE = 0;
        final int VIEW_TYPE_MEDIA = 1;

        private User[] uDataset;
        private Post[] pDataset;
        private Context postContext;

        // Provide a suitable constructor (depends on the kind of dataset)
        public SecondaryAdapter(Context context, Post[] post, User[] user) {
            pDataset = post;
            uDataset = user;
            postContext = context;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if(viewType == VIEW_TYPE_PROFILE){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_profile, parent, false);
                return new ProfileViewHolder(v);
            }

            if(viewType == VIEW_TYPE_MEDIA){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_post, parent, false);
                return new PostViewHolder(v);
            }

            return null;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof ProfileViewHolder){
                ((ProfileViewHolder) holder).populate(uDataset[position]);
            }

            if(holder instanceof PostViewHolder){
                ((PostViewHolder) holder).populate(pDataset[position - uDataset.length]);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return pDataset.length + uDataset.length;
        }

        @Override
        public int getItemViewType(int position){
            if(position < uDataset.length){
                return VIEW_TYPE_PROFILE;
            }

            if(position - uDataset.length < pDataset.length){
                return VIEW_TYPE_MEDIA;
            }

            return -1;
        }
    }
}
