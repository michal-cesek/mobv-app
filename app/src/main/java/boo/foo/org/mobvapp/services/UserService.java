package boo.foo.org.mobvapp.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import boo.foo.org.mobvapp.models.User;

public class UserService {
    private final String TAG = "UserService:";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Activity activity;

    public UserService(Activity a) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        activity = a;
    }

    public User getCurrentUser() {
        FirebaseUser currentFirebaseUser = auth.getCurrentUser();
        Log.d(TAG, "getCurrentUser firebaseUser:  " + currentFirebaseUser);

        if (currentFirebaseUser != null) {
            String uid = currentFirebaseUser.getUid();
            User user = getCurrentUserData();
            Log.d(TAG, "getCurrentUser userRecord  " + user);
            return user;
        }

        return null;
    }


    public void increaseUserPostsNumber() {

        getUseRecordById(getCurrentUser().getId(), objs -> {
            User user = (User) objs.get(0);
            user.setNumberOfPosts(user.getNumberOfPosts() + 1);
            setCurrentUserData(user);

            QueryDocumentSnapshot docuemnt = (QueryDocumentSnapshot) objs.get(1);
            db.collection(User.collectionName)
                    .document(docuemnt.getId())
                    .set(user);
            return null;
        }, err -> {
            return null;
        });

    }

    public void login(
            String email,
            String password,
            Function<User, Void> onResolved,
            Function<String, Void> onFail
    ) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase signInWithEmail:success");
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        getUseRecordById(firebaseUser.getUid(), objs -> {
                            User user = (User) objs.get(0);
                            setCurrentUserData(user);
                            onResolved.apply(user);
                            return null;
                        }, s -> {
//                            https://github.com/flutter/flutter/issues/15907
                            // async firebaseUser.delete()
                            onFail.apply(s);
                            return null;
                        });
                    } else {
                        Log.w(TAG, "Firebase signInWithEmail:failure", task.getException());
                        onFail.apply(task.getException().toString());
                    }
                });
    }


    public void register(
            String name,
            String email,
            String password,
            Function<User, Void> onResolved,
            Function<String, Void> onFail
    ) {
        //register user with firestore auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        Log.d(TAG, "createUserWithEmail:success UID: " + firebaseUser.getUid());

                        //create user firestore DB record
                        User user = new User();
                        user.setId(firebaseUser.getUid());
                        user.setUsername(name);
                        user.setNumberOfPosts(0);
                        //todo
                        Timestamp now = new Timestamp(new java.util.Date());
                        user.setDate(now);

                        createUserRecord(user, (_v) -> {
                            setCurrentUserData(user);
                            onResolved.apply(user);
                            return null;
                        }, s -> {
//                            https://github.com/flutter/flutter/issues/15907
                            // async firebaseUser.delete()
                            onFail.apply(s);
                            return null;
                        });

                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        onFail.apply(task.getException().toString());
                    }
                });
    }

    public void logout() {
        auth.signOut();
        //set empty user - user is not logged in
        setCurrentUserData(new User());
    }


    private User getCurrentUserData() {
        SharedPreferences prefs = this.activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        //TODO defValue
        String defValue = gson.toJson(new User());
        String json = prefs.getString(TAG, defValue);
        User user = gson.fromJson(json, User.class);

        Log.w(TAG, "getCurrentUserData  " + user.getId());

        if (user.getId() != null) {
            return user;
        }

        return null;
    }

    private void setCurrentUserData(User user) {
        SharedPreferences prefs = this.activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(TAG, json);
        editor.commit();
        Log.w(TAG, "setCurrentUserData  " + json);

    }


    private void createUserRecord(
            User user,
            Function<Void, Void> onResolved,
            Function<String, Void> onFail
    ) {
        db.collection(User.collectionName)
                .add(user)
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "createUserRecord succesfull ID: " + doc.getId());
                    onResolved.apply(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "createUserRecord fail", e);
                    onFail.apply(e.getMessage());
                });
    }

    private void getUseRecordById(
            String userId,
            Function<List<Object>, Void> onResolved,
            Function<String, Void> onFail
    ) {

        db.collection(User.collectionName)
                .whereEqualTo("id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //todo refactor
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "getUseRecordById " + userId + " " + document.getData());
                            User user = document.toObject(User.class);
                            List<Object> objs = Arrays.asList(user, document);
                            onResolved.apply(objs);
                            return;
                        }
                        onFail.apply("No user found");
                    } else {
                        Log.w(TAG, "getUseRecordById ", task.getException());
                        onFail.apply(task.getException().toString());
                    }
                });

    }


}
