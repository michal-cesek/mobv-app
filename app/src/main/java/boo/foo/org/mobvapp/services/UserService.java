package boo.foo.org.mobvapp.services;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.function.Function;

import boo.foo.org.mobvapp.models.User;

public class UserService {

    private FirebaseFirestore db;


    public UserService() {
        db = FirebaseFirestore.getInstance();
    }

    //todo add some kind of cache mechanism
    public void login(
            String userName,
            Function<User, Void> onResolved,
            Function<String, Void> onFail
    ) {

        db.collection(User.collectionName)
//                .whereEqualTo("name", userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //todo refactor
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("UserService: ", document.getId() + " => " + document.getData());

                            User user = document.toObject(User.class)
                                    .withId(document.getId());

                            onResolved.apply(user);
                            break;
                        }
                        onResolved.apply(null);

                    } else {
                        Log.w("UserService: ", "Error getting documents.", task.getException());
                        onFail.apply(task.getException().toString());
                    }
                });

    }

}
