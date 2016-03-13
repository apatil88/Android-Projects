package com.amrutpatil.makeanote;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

/**
 * Description: Class that performs actions with Dropbox. Example: creating a session, authentication
 */
public class DropboxActions {

    //Method to handle authentication and re-authentication with Dropbox
    public static void loadAuth(AndroidAuthSession authSession, Context context){
        SharedPreferences prefs = context.getSharedPreferences(AppConstant.ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(AppConstant.ACCESS_KEY_NAME, null);
        String secret = prefs.getString(AppConstant.ACCESS_SECRET_NAME, null);

        //If authentication is not complete
        if(key == null || secret == null || key.length()== 0 || secret.length() == 0){
            return;
        }
        if(key.equals("oauth2:")){
            authSession.setOAuth2AccessToken(secret);
        } else{
            authSession.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    //Method to store oauth tokens
    public static void storeAuth(AndroidAuthSession authSession, Context context){
        String oAuth2AccessToken = authSession.getOAuth2AccessToken();
        if(oAuth2AccessToken != null){
            //Adding key manually as key is not part of oauth2 access token
            saveAuth(context, "oauth2:", oAuth2AccessToken);
            return;
        }
        AccessTokenPair oAuth1AccessToken = authSession.getAccessTokenPair();
        if(oAuth1AccessToken != null){
            saveAuth(context, oAuth1AccessToken.key, oAuth1AccessToken.secret);
        }
    }

    //Method to save tokens in Shared Preferences
    private static void saveAuth(Context context, String accessKey, String accessSecret){
        SharedPreferences prefs = context.getSharedPreferences(AppConstant.ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(AppConstant.ACCESS_KEY_NAME, accessKey);
        edit.putString(AppConstant.ACCESS_SECRET_NAME, accessSecret);
        edit.apply();
    }

    //Method to clear the keys
    public static void clearKeys(Context context, String accessKey, String accessSecret){
        SharedPreferences prefs = context.getSharedPreferences(AppConstant.ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }

    //Method to build a session
    public static AndroidAuthSession buildSession(Context context){
        AppKeyPair appKeyPair = new AppKeyPair(AppConstant.APP_KEY, AppConstant.APP_SECRET);
        AndroidAuthSession androidAuthSession = new AndroidAuthSession(appKeyPair);
        loadAuth(androidAuthSession, context);
        return androidAuthSession;
    }
}
