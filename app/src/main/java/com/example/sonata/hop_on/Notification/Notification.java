package com.example.sonata.hop_on.Notification;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Sonata on 8/2/2016.
 */
public class Notification {

    public static String [] messageList = {
            "Username or password invalid. Please try again!", // 0
            "Sever failed! Please try again later!", // 1
            "Invalid email address. Please try again!", // 2
            "Change password successfully!", // 3
            "Please verify confirmation email before log in.", // 4
            "Invalid account! Please try another one.", // 5
            "Old password incorrect! Please try again.", // 6
            "Invalid password! Please try again.", // 7
            "Invalid request! Please try again!", // 8
            "Incorrect password! Please try again!", // 9
    };

    public static void showMessage(final Activity activity, final int mesCode) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                activity);

                        builder.setMessage(messageList[mesCode]);
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

            }
        });

    }
}
