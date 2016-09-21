package com.example.sonata.hop_on.LogIn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sonata.hop_on.BeaconService.BeaconScanningService;
import com.example.sonata.hop_on.BicycleBooking.CurrentBookingActivity;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.NavigationDrawer.NavigationDrawerActivity;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.PasswordManagement.ResetPasswordActivity;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.example.sonata.hop_on.SignUp.SignUpActivity;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    private static final int REQUEST_SIGNUP = 0;
    private static final int REQUEST_FORGOT_PASSWORD = 1;

    @InjectView(R.id.input_username)  EditText _usernameText;
    @InjectView(R.id.input_password)  EditText _passwordText;
    @InjectView(R.id.btn_login)       Button   _loginButton;
    @InjectView(R.id.link_forgotPass) TextView _forgotPassLink;
    @InjectView(R.id.link_signup)     TextView _signupLink;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        ButterKnife.inject(this);

        GlobalVariable.activity = LogInActivity.this;

        GlobalVariable.checkConnected(LogInActivity.this);

        this.setTitle("Log In");

        if (GlobalVariable.obtainedAuCode(this)) {
            getCurrentBookingInformation();
        }

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        _forgotPassLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Forgot Password activity
                Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivityForResult(intent, REQUEST_FORGOT_PASSWORD);
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    private void getCurrentBookingInformation()
    {
        Preferences.showLoading(this, "Update", "Sending request to server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        Call<ResponseBody> call = client.getCurrentBookingInformation();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();
                    if (messageCode == 200)
                    {
                        GlobalVariable.setBookingStatusInSP(LogInActivity.this, GlobalVariable.BOOKED);
                        Intent intent = new Intent(LogInActivity.this, CurrentBookingActivity.class);
                        startActivity(intent);
                    }
                    else if (messageCode == 400)
                    {
                        GlobalVariable.setBookingStatusInSP(LogInActivity.this, GlobalVariable.FREE);
                        Intent intent = new Intent(LogInActivity.this, NavigationDrawerActivity.class);
                        startActivity(intent);
                    } else if (messageCode == 500)
                    {
                        Notification.showMessage(LogInActivity.this, 1);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void login() {

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final String username = _usernameText.getText().toString();
        final String password = _passwordText.getText().toString();

        timer = new CountDownTimer(30000, 20) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                try
                {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(LogInActivity.this);
                    String message = "Server did not respond. Please check your internet connection." +
                            "Do you want to retry?";
                    builder2.setMessage(message);
                    builder2.setCancelable(true);

                    builder2.setPositiveButton(
                            "RETRY",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    loginAction(username, password, LogInActivity.this);
                                    dialog.cancel();
                                }
                            });

                    builder2.setNegativeButton(
                            "CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Preferences.dismissLoading();
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert12 = builder2.create();
                    alert12.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };


        loginAction(username, password, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    public void onBackPressed() {
        // disable going back to the NavigationActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.isEmpty() || username.length() < 4 || username.length() > 255) {
            _usernameText.setError("enter a valid username address");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 || password.length() > 255) {
            _passwordText.setError("at least 6 characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void loginAction(String username, String password, final Activity activity) {

        Preferences.showLoading(this, "Log In", "Authenticating...");

        timer.start();

        StringClient client = ServiceGenerator.createService(StringClient.class);
        LogInClass up = new LogInClass(username, password);
        Call<ResponseBody> call = client.login(up);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Preferences.dismissLoading();
                    JSONObject data = new JSONObject(URLDecoder.decode(response.body().string(), "UTF-8"));
                    int messageCode = response.code();
                    if (messageCode == 200) // SUCCESS
                    {
                        onLoginSuccess();
                        String authorizationCode = data.getString("token");
                        GlobalVariable.setAuCodeInSP(LogInActivity.this, authorizationCode);

                        getCurrentBookingInformation();
                    }
                    else
                    {
                        onLoginFailed();
                        if (messageCode == 400) // BAD REQUEST HTTP
                        {
                            int errorCode = data.getInt("code");
                            if (errorCode == 0 || errorCode == 1)
                            {
                                Notification.showMessage(LogInActivity.this, 0);
                            }
                            else if (errorCode == 3)
                            {
                                Notification.showMessage(LogInActivity.this, 4);
                            }
                            else if (errorCode == 6)
                            {
                                Notification.showMessage(LogInActivity.this, 5);
                            }
                        }
                        else if (messageCode == 500) // SERVER FAILED
                        {
                            Notification.showMessage(LogInActivity.this, 1);
                        }
                        else {

                        }
                    }
                } catch (Exception e) {
                    onLoginFailed();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onLoginFailed();
            }
        });

    }
}
