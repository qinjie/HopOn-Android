package com.example.sonata.hop_on.SignUp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sonata.hop_on.LogIn.LogInActivity;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    @InjectView(R.id.input_username)    EditText _usernameText;
    @InjectView(R.id.input_email)       EditText _emailText;
    @InjectView(R.id.input_password)    EditText _passwordText;
    @InjectView(R.id.input_confirmpass) EditText _confirmedPasswordText;
    @InjectView(R.id.btn_continue)      Button   _continueButton;
    @InjectView(R.id.link_login)        TextView _loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.setTitle("Register");

        ButterKnife.inject(this);

        _continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _continueButton.setEnabled(false);

        String username = _usernameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        SignUpClass user = new SignUpClass(username, password, email);
        signupAction(user);
    }

    public void onSignupSuccess() {
        Preferences.dismissLoading();
        setResult(RESULT_OK, null);

        Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
        startActivity(intent);
    }

    public void onSignupFailed() {
        Preferences.dismissLoading();
        _continueButton.setEnabled(true);

    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String confirmedPassword = _confirmedPasswordText.getText().toString();


        if (username.isEmpty() || username.length() < 4 || username.length() > 255) {
            _usernameText.setError("enter a valid username");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (confirmedPassword.compareTo(password) != 0) {
            _confirmedPasswordText.setError("These passwords don't match. Try again?");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void signupAction(SignUpClass user) {

        Preferences.showLoading(SignUpActivity.this, "Sign Up", "Creating Account...");

        StringClient client = ServiceGenerator.createService(StringClient.class);

        Call<ResponseBody> call = client.signup(user);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    int messageCode = response.code();
                    if (messageCode == 200) // SUCCESS
                    {
                        onSignupSuccess();
                    }
                    else
                    {
                        onSignupFailed();
                        if (messageCode == 400) // BAD REQUEST HTTP
                        {

                        }
                        else if (messageCode == 500) // SERVER FAILED
                        {
                            Notification.showMessage(SignUpActivity.this, 1);
                        }
                        else {

                        }
                    }

                }
                catch(Exception e){
                    onSignupFailed();
                    e.printStackTrace();
                    Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onSignupFailed();
            }
        });
    }
}
