package boo.foo.org.mobvapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.leinardi.android.speeddial.SpeedDialView;

import boo.foo.org.mobvapp.models.User;
import boo.foo.org.mobvapp.services.UserService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity:";
    private UserService userService;

    @Override
    public void onStart() {
        super.onStart();
        User user = userService.getCurrentUser();

        //get all users with embedded posts

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSpeedDialMenu();
        userService = new UserService(this);
    }

    @Override
    public void onBackPressed() {
        return;
    }


    private void setSpeedDialMenu(){
        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.menu_speed_dial);
        speedDialView.setOnActionSelectedListener(speedDialActionItem -> {
            switch (speedDialActionItem.getId()) {
                case R.id.action_logout:
                    logout();
                    return true;
                case R.id.action_add_video:
                    //TODO
                    return true;
                case R.id.action_add_image:
                    //TODO
                    return true;
                default:
                    return false;
            }
        });
    }

    public void logout() {
        userService.logout();
        goToLoginScreen();
    }

    public void goToLoginScreen(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
