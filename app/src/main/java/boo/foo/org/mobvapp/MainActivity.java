package boo.foo.org.mobvapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.erikagtierrez.multiple_media_picker.Gallery;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import boo.foo.org.mobvapp.models.Post;
import boo.foo.org.mobvapp.services.PostsService;
import boo.foo.org.mobvapp.services.UserService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity:";

    private UserService userService;
    private PostsService postsService;

    private List<Post> posts;
    private ProgressBar pbMain;
    private TextView tvResponse;

    static final int OPEN_IMAGE_PICKER = 123;
    static final int OPEN_VIDEO_PICKER = 124;


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12345;
    private boolean storagePermissionsGranted = false;

    private final List<String> supportedImageTypes = Arrays.asList("image/jpeg", "image/png");
    private final List<String> supportedVideoTypes = Arrays.asList("video/mp4");

    @Override
    public void onStart() {
        super.onStart();
        storagePermissionsGranted = permissionsGranted();

        postsService.getAllPosts(
                posts -> {
                    this.posts = posts;
                    displayPosts();
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
        tvResponse = findViewById(R.id.tv_response);

        userService = new UserService(this);
        postsService = new PostsService();
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

    public void displayPosts() {
        pbMain.setVisibility(View.INVISIBLE);
        tvResponse.setVisibility(View.VISIBLE);
        tvResponse.setText("Posts found: " + this.posts.size());
    }

    @Override
    public void onBackPressed() {
        return;
    }

}
