package com.example.sonata.hop_on.UserProfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.PasswordManagement.ChangePasswordActivity;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeEmailActivity extends AppCompatActivity {

    private static final String TAG = "ChangeEmailActivity";

    @InjectView(R.id.old_password)      EditText _currentpasswordText;
    @InjectView(R.id.input_email)       EditText _emailText;
    @InjectView(R.id.btn_submit)        Button   _submitButton;

    CountDownTimer timer;
    boolean isServerRespond = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        this.setTitle("Change Email");

        ButterKnife.inject(this);

        _submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmail();
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

    public void changeEmail() {
        Log.d(TAG, "Change Password");

        if (!validate()) {
            onChangeEmailFailed();
            return;
        }

        Preferences.showLoading(ChangeEmailActivity.this, "Change Email", "Processing...");

        isServerRespond = false;
        timer = new CountDownTimer(8000, 20) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                try
                {
                    if (isServerRespond == false)
                    {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(ChangeEmailActivity.this);
                        String message = "Server did not respond. Please check your internet connection." +
                                "Do you want to retry?";
                        builder2.setMessage(message);
                        builder2.setCancelable(true);

                        builder2.setPositiveButton(
                                "RETRY",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        changePasswordAction();
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
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        changePasswordAction();
    }

    void showMessage(String message) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(ChangeEmailActivity.this);
        builder2.setMessage(message);
        builder2.setCancelable(true);

        builder2.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert12 = builder2.create();
        alert12.show();
    }

    public void onChangeEmailSuccess() {
        Preferences.dismissLoading();

        _currentpasswordText.setText("");
        _emailText.setText("");

    }

    public void onChangeEmailFailed() {
        Preferences.dismissLoading();
    }

    public boolean validate() {

        boolean valid = true;

        String currentPassword = _currentpasswordText.getText().toString();
        String email = _emailText.getText().toString();

        if (currentPassword.isEmpty() || currentPassword.length() < 4 || currentPassword.length() > 10) {
            _currentpasswordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _currentpasswordText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        return valid;
    }

    void changePasswordAction() {

        timer.start();
        SharedPreferences pref = getSharedPreferences("ATK_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        String currentPassword = _currentpasswordText.getText().toString();
        String email = _emailText.getText().toString();

        JsonObject toUp = new JsonObject();
        toUp.addProperty("newEmail", email);
        toUp.addProperty("password", currentPassword);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);
        Call<ResponseBody> call = client.changeEmail(toUp);

        call.enqueue (new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    timer.cancel();
                    isServerRespond = true;

                    int messageCode = response.code();

                    if (messageCode == 200) // SUCCESS
                    {
                        onChangeEmailSuccess();
                        Toast.makeText(getBaseContext(), "Change email successfully!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        onChangeEmailFailed();
                        if (messageCode == 400) // BAD REQUEST HTTP
                        {

                        }
                        else if (messageCode == 500) // SERVER FAILED
                        {
                            Notification.showMessage(ChangeEmailActivity.this, 1);
                        }
                        else {

                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    timer.cancel();
                    onChangeEmailFailed();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                timer.cancel();
                onChangeEmailFailed();
            }
        });

    }
}
