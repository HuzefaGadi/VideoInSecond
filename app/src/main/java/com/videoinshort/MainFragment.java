package com.videoinshort;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.videoinshort.beans.FbProfile;
import com.videoinshort.utilities.Constants;

import org.json.JSONObject;

import java.util.Arrays;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private CallbackManager callbackManager;
    private TextView textView;

    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    SharedPreferences preferences;
    SharedPreferences.Editor edit;

    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            Profile profile = Profile.getCurrentProfile();
            displayMessage(profile);

            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                           Profile profile = Profile.getCurrentProfile();


                            if(response.getError() == null)
                            {
                                Gson gson = new Gson();
                                FbProfile fbProfile = gson.fromJson(object.toString(),FbProfile.class);
                                fbProfile.setFbProfileLink("http://www.facebook.com/" + fbProfile.getFbUserId());
                                fbProfile.setProfileImagePath("");
                                String fbUserInfo = gson.toJson(fbProfile);
                                edit.putString(Constants.FB_USER_INFO, fbUserInfo);
                                edit.commit();
                                Intent intent = new Intent(getActivity(),MyActivity.class);
                                intent.putExtra(Constants.FB_USER_INFO, fbUserInfo);
                                startActivity(intent);
                            }
                            System.out.println("response---->>"+object);
                            Log.v("LoginActivity", response.toString());
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday,location,first_name,last_name");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {

        }
    };

    public MainFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        preferences = getActivity().getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        edit = preferences.edit();
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker= new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                    updateWithToken(newToken);
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                displayMessage(newProfile);
            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        updateWithToken(AccessToken.getCurrentAccessToken());
    }

    private void updateWithToken(AccessToken newToken) {

        if(newToken!=null)
        {
            Intent intent = new Intent(getActivity(),MyActivity.class);

            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        textView = (TextView) view.findViewById(R.id.textView);

        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends,user_location,basic_info"));
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, callback);

        Profile profile = Profile.getCurrentProfile();
        if(profile!=null)
        {
            System.out.println("PROFILE-->"+profile.getLinkUri());

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void displayMessage(Profile profile){
        if(profile != null){

            textView.setText(profile.getName());
            Uri uri = profile.getProfilePictureUri(100,100);
            String url = uri.toString();
            System.out.println("FBURL-->" + url);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}