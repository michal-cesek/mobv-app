package boo.foo.org.mobvapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import boo.foo.org.mobvapp.models.User;
import boo.foo.org.mobvapp.services.UserService;


public class LoginActivity extends AppCompatActivity {

    public String TAG = "LoginActivity:";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private ImageView ivLogo;
    private RelativeLayout rlLogo;

    private View mLoginFormView;
    private View mRegisternFormView;

    private Button bLoginShow;
    private Button bRegisterShow;


    // REGISTER
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordRepeat;

    private UserService userService;


    @Override
    public void onStart() {
        super.onStart();
        User user = userService.getCurrentUser();

        if(user != null){
            goToHomeScreen();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView tvVersionName = findViewById(R.id.tv_app_version);
        tvVersionName.setText(BuildConfig.VERSION_NAME);

        userService = new UserService(this);

        mLoginFormView = findViewById(R.id.login_form);
        mRegisternFormView = findViewById(R.id.register_form);

        ivLogo = findViewById(R.id.iv_logo);
        rlLogo = findViewById(R.id.rl_logo);

        bLoginShow = findViewById(R.id.b_login_show);
        bLoginShow.setOnClickListener(v -> {
            mRegisternFormView.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.VISIBLE);
        });

        bRegisterShow = findViewById(R.id.b_register_show);
        bRegisterShow.setOnClickListener(v -> {
            mLoginFormView.setVisibility(View.GONE);
            mRegisternFormView.setVisibility(View.VISIBLE);
        });

        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button bLogin = findViewById(R.id.b_login);
        bLogin.setOnClickListener(view -> attemptLogin());

        Button bRegister = findViewById(R.id.b_register);
        bRegister.setOnClickListener(view -> attemptRegister());

        bRegisterShow = findViewById(R.id.b_register_show);
        bRegisterShow.setOnClickListener(v -> {
            mLoginFormView.setVisibility(View.GONE);
            mRegisternFormView.setVisibility(View.VISIBLE);
        });

        mProgressView = findViewById(R.id.login_progress);

        etUsername = findViewById(R.id.tv_register_username);
        etEmail = findViewById(R.id.tv_register_email);
        etPassword = findViewById(R.id.tv_register_password);
        etPasswordRepeat = findViewById(R.id.tv_register_password_repeat);
    }


    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showLoginProgress(true);

            userService.login(
                    email,
                    password,
                    u -> {
                        mProgressView.setVisibility(View.GONE);
                        goToHomeScreen();
                        return null;
                    },
                    err -> {
                        showLoginProgress(false);
                        return null;
                    }
            );
        }
    }


    private void attemptRegister() {
        etUsername.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etPasswordRepeat.setError(null);

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String passwordRepeat = etPasswordRepeat.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            etUsername.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            cancel = true;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_field_required));
            focusView = etEmail;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            cancel = true;
        }
        if (TextUtils.isEmpty(passwordRepeat)) {
            etPasswordRepeat.setError(getString(R.string.error_field_required));
            focusView = etPasswordRepeat;
            cancel = true;
        }
        //

        if (!isValidUsername(username)) {
            etUsername.setError(getString(R.string.error_invalid_username));
            focusView = etUsername;
            cancel = true;
        }

        if (!isEmailValid(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            focusView = etEmail;
            cancel = true;
        }

        if (!isPasswordValid(password)) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
            cancel = true;
        }
        if (!passwordsMatch(password, passwordRepeat)) {
            etPasswordRepeat.setError(getString(R.string.error_password_dont_match));
            focusView = etPasswordRepeat;
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();
        } else {
            showRegisterProgress(true);

            userService.register(
                    username,
                    email,
                    password,
                    u -> {
                        mProgressView.setVisibility(View.GONE);
                        goToHomeScreen();
                        return null;
                    },
                    err -> {
                        showRegisterProgress(false);
                        return null;
                    }
            );
        }
    }



    private void showLoginProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showRegisterProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mRegisternFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void goToHomeScreen(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean passwordsMatch(String p1, String p2) {
        return p1.equals(p2);
    }

    private boolean isValidUsername(String username) {
        return username.length() > 3;
    }


}

