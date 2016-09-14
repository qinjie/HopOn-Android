package com.example.sonata.hop_on.UserProfile;

import org.json.JSONObject;

/**
 * Created by Sonata on 9/14/2016.
 */

public class UserProfile {
    String userName;
    String userEmail;
    String userMobilePhone;

    public UserProfile(JSONObject data) {
        try
        {
            userName = data.getString("fullname");
            userEmail = data.getString("email");
            userMobilePhone = data.getString("mobile");

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserMobilePhone() { return userMobilePhone; }
}
