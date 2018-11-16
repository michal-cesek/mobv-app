package boo.foo.org.mobvapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;



import boo.foo.org.mobvapp.models.Post;
import boo.foo.org.mobvapp.models.User;
import boo.foo.org.mobvapp.services.UserService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MY TAG";
    private UserService userService;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userService = new UserService();

        String userName = "peter";
        login(userName);

    }

    private void login(String username) {
        userService.login(username,
                (u) -> {
                    Log.w(TAG, "Great success");
                    return null;
                }, (message) -> {
                    Log.w(TAG, "Fail");
                    return null;
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
