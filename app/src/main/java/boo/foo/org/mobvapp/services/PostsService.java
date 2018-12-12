package boo.foo.org.mobvapp.services;


import android.app.Activity;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import boo.foo.org.mobvapp.models.Post;

public class PostsService {
    private final String TAG = "PostService:";

    private FirebaseFirestore db;
    private UserService userservice;
    private MediaUploaderService uploaderService;

    public PostsService() {
        db = FirebaseFirestore.getInstance();
        uploaderService = new MediaUploaderService();

    }

    //TODO increase number of post in user profile
    public void addPost(
            Activity activity,
            File file,
            Post post,
            Function<Post, Object> onResolved,
            Function<String, Void> onFail
    ) {

        if (!post.getType().equals("image") && !post.getType().equals("video")) {
            onFail.apply("Post type must be set to image|video");
            return;
        }

        uploaderService.upload(activity, "upfile", file,
                fileUrl -> {
                    Log.d(TAG, "addPost upload success " + fileUrl);

                    if (post.getType().equals("image")) {
                        post.setImageurl(fileUrl);
                    } else {
                        post.setVideourl(fileUrl);
                    }

                    db.collection(Post.collectionName)
                            .add(post)
                            .addOnSuccessListener(documentReference -> {
                                post.setId(documentReference.getId());
                                //TODO id of post is set but date (created) not
                                // needed posts refresh ?!
                                Log.d(TAG, "addPost successful id: " + post.getId());
                                onResolved.apply(post);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "addPost fail", e);
                                onFail.apply(e.getMessage());
                            });

                    return null;
                },
                err -> {
                    Log.d(TAG, "addPost upload fail " + err);
                    onFail.apply(err);
                    return null;
                }
        );

    }


    public void getAllPosts(
            Function<List<Post>, Object> onResolved,
            Function<String, Void> onFail
    ) {
        db.collection(Post.collectionName)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //todo refactor
                        ArrayList<Post> posts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            posts.add(
                                    document.toObject(Post.class).withId(document.getId())
                            );
                        }
                        Log.d(TAG, "getAllPosts count:" + posts.size());
                        onResolved.apply(posts);

                    } else {
                        Log.w(TAG, "getAllPosts fail", task.getException());
                        onFail.apply(task.getException().toString());
                    }
                });
    }

    public void getPosts(
            String userId,
            Function<List<Post>, Object> onResolved,
            Function<String, Void> onFail
    ) {
        db.collection(Post.collectionName)
                .whereEqualTo("userid", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //todo refactor
                        ArrayList<Post> posts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("PostService: ", document.getId() + " => " + document.getData());
                            posts.add(
                                    document.toObject(Post.class).withId(document.getId())
                            );
                        }
                        onResolved.apply(posts);

                    } else {
                        Log.w("UserService: ", "Error getting documents.", task.getException());
                        onFail.apply(task.getException().toString());
                    }
                });

    }


}
