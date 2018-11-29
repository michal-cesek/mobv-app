package boo.foo.org.mobvapp.services;


import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import boo.foo.org.mobvapp.models.Post;

public class PostsService {

    private FirebaseFirestore db;

    public PostsService() {
        db = FirebaseFirestore.getInstance();
    }

    public void getPosts(
            String userId,
            Function<List<Post>, Object> onResolved,
            Function<String, Void> onFail
    ) {
        db.collection(Post.collectionName)
                .whereEqualTo("userid", userId)
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

//
//    private void addPost( Post post) {
//
//        db.collection(Post.collectionName)
//                .add(post)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        post.withId(documentReference.getId());
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });
//    }


}
