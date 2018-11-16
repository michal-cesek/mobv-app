package boo.foo.org.mobvapp.models;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Model {
    @Exclude
    public String id;

    public <T extends Model> T withId(@NonNull final String id) {
        this.id = id;
        return (T) this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}