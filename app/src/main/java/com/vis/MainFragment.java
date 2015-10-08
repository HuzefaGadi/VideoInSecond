package com.vis;

import android.app.ProgressDialog;
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
import com.vis.beans.FbProfile;
import com.vis.utilities.Constants;

import org.json.JSONObject;

import java.util.Arrays;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private CallbackManager callbackManager;


    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    SharedPreferences preferences;
    SharedPreferences.Editor edit;
    boolean fromSettings;
    LoginButton loginButton;
    ProgressDialog pd;


    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {

            showDialog();
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


                            if (response.getError() == null) {
                                Gson gson = new Gson();
                                FbProfile fbProfile = gson.fromJson(object.toString(), FbProfile.class);
                                fbProfile.setFbProfileLink("http://www.facebook.com/" + fbProfile.getFbUserId());
                                fbProfile.setProfileImagePath("http://graph.facebook.com/"+fbProfile.getFbUserId()+"/picture?type=large");
                                String fbUserInfo = gson.toJson(fbProfile);
                                edit.putString(Constants.FB_USER_INFO, fbUserInfo);
                                edit.commit();
                                Intent intent = new Intent(getActivity(), MyActivity.class);
                                intent.putExtra(Constants.FB_USER_INFO, fbUserInfo);
                                startActivity(intent);
                                getActivity().finish();

                            }
                            System.out.println("response---->>" + object);
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
            loginButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(FacebookException e) {
            loginButton.setVisibility(View.VISIBLE);
        }


    };

    private void hideDialog() {
        if (pd != null) {
            pd.cancel();
        }
    }

    public MainFragment() {

    }

    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (LoginSuccess) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement textEntered");
        }
    }*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fromSettings = getActivity().getIntent().getBooleanExtra(Constants.MENU_SETTINGS, false);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        preferences = getActivity().getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        edit = preferences.edit();
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
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

       if (!fromSettings) {
            updateWithToken(AccessToken.getCurrentAccessToken());
        }
        pd = new ProgressDialog(getActivity());


    }

    private void updateWithToken(AccessToken newToken) {

        if (getActivity() != null) {
            if (newToken != null) {
                Intent intent = new Intent(getActivity(), MyActivity.class);
                startActivity(intent);
                getActivity().finish();
            } else {


                Intent intent = new Intent("finish_activity");
                getActivity().sendBroadcast(intent);
            }

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

        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends,user_location,basic_info"));
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, callback);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            System.out.println("PROFILE-->" + profile.getLinkUri());

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void displayMessage(Profile profile) {
        if (profile != null) {
            Uri uri = profile.getProfilePictureUri(100, 100);
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

    private void showDialog() {

        pd.setMessage("Please wait while we log you in..");
        pd.setCancelable(false);
        pd.show();
    }
}