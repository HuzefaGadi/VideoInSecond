package com.vis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.vis.Analytics.TrackerName;
import com.vis.beans.FbProfile;
import com.vis.beans.Registration;
import com.vis.utilities.Constants;
import com.vis.utilities.Utility;
import com.vis.utilities.WebServiceUtility;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class MyActivity extends Activity {
    private ProgressBar progress;
    protected WebView mainWebView;
    private Context mContext;
    private String responseFromService;
    private WebView mWebviewPop;
    private FrameLayout mContainer;
    CookieManager cookieManager;
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "995587742942";


    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    private PendingIntent pendingIntent;
    RelativeLayout noInternetMessage;


    String regid;
    Button refreshButton;
    Utility utility;
    FbProfile fbProfile;
    BroadcastReceiver broadcast_reciever;

    @Override
    public void onBackPressed() {
        //	System.out.println("BACK PRESSED-->"+mainWebView.getUrl());

        if (mWebviewPop != null) {
            mWebviewPop.setVisibility(View.GONE);
            mContainer.removeView(mWebviewPop);
            mWebviewPop = null;
        } else {
            if (mainWebView.canGoBack()) {
                mainWebView.goBack();
            } else {
                super.onBackPressed();
                finish();
            }

        }
    }


    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String responseFromFb = getIntent().getStringExtra(Constants.FB_USER_INFO);
        if (responseFromFb != null && !responseFromFb.isEmpty()) {
            fbProfile = new Gson().fromJson(responseFromFb, FbProfile.class);
        }
        utility = new Utility();

        //swipeLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        noInternetMessage = (RelativeLayout) findViewById(R.id.no_internet_message);
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);
        mainWebView = (WebView) findViewById(R.id.webview);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                onRefresh();
            }
        });


        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mainWebView, true);
        }
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setMax(100);
        WebSettings webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setUserAgentString(
                webSettings.getUserAgentString()
                        + " "
                        + "VideoInShortApp");
        mainWebView.setWebViewClient(new MyCustomWebViewClient());
        mainWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mainWebView.setWebChromeClient(new MyCustomChromeClient());
        mContext = this;
        if (utility.checkInternetConnectivity(mContext)) {
            mContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            mainWebView.loadUrl(Constants.url);
        } else {
            mContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(mContext);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(Constants.TAG, "No valid Google Play Services APK found.");
        }


        Tracker t = ((Analytics) getApplication()).getTracker(TrackerName.APP_TRACKER);
        t.setScreenName("MainActivity");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.AppViewBuilder().build());

        SharedPreferences prefs = getPreferences(mContext);
        if (prefs.getBoolean("ALARM_SET", false)) {

        } else {
            Intent alarmIntent = new Intent(MyActivity.this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(MyActivity.this, 0, alarmIntent, 0);

            //System.out.println("ALARM CALLED ON CREATE");
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            int interval = 1000 * 60 * 60 * 24 * 7;
            int startTime = 1000 * 60 * 60 * 24 * 5;
            /*int interval = 1000 * 60 * 1;
				int startTime = 1000 * 60 * 1;*/
			/* Set the alarm to start at 10:30 AM */
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() + startTime);
			/* Repeating on every 20 minutes interval */
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    interval, pendingIntent);
            prefs.edit().putBoolean("ALARM_SET", true).commit();
        }


        broadcast_reciever = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_activity")) {
                    finish();
                    // DO WHATEVER YOU WANT.
                }
            }
        };

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processIntent(getIntent());
    }

    void processIntent(Intent intent) {
        String notification = intent.getStringExtra("NOTIFICATION");
        if (notification != null && !notification.isEmpty()) {
            new WebServiceUtility(this, Constants.CLICK_INFO_TASK, notification);
        }
        onRefresh();

    }

    public void showDialog() {
        //prgDialog.show();
    }

    public void onRefresh() {
        // TODO Auto-generated method stub
		/*
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
				swipeLayout.setRefreshing(false);
			}
		}, 5000);*/
        if (utility.checkInternetConnectivity(mContext)) {
            mContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            mainWebView.loadUrl(Constants.url);

        } else {

            mContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, FacebookActivity.class);
            intent.putExtra(Constants.MENU_SETTINGS, true);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        SharedPreferences pref = getPreferences(this);
        if (pref.getBoolean(Constants.PREFERENCES_SHOW_ALARM, false) && !pref.getBoolean(Constants.PREFERENCES_ALREADY_RATED, false)) {
            rateUs("You are awesome! If you feel the same about VideoInShort, please take a moment to rate it.");
        }
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        unregisterReceiver(broadcast_reciever);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mainWebView != null) {
            mainWebView.onPause();
            mainWebView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        if (mainWebView != null) {
            mainWebView.onResume();
            mainWebView.resumeTimers();
        }
    }

    private void rateUs(String message) {

        final SharedPreferences prefs = getPreferences(this);
        Tracker t = ((Analytics) getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Alert View")
                .setAction("Rate Us")
                .setLabel("Rate Us called")

                .build());


        AlertDialog dialog = new AlertDialog.Builder(this).create();
        t.enableAdvertisingIdCollection(true);
        dialog.setTitle("Rate Us!!");
        dialog.setMessage(message);
        dialog.setIcon(R.mipmap.ic_launcher);
		/*AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		t.enableAdvertisingIdCollection(true);
		dialog.setTitle( "Rate Us!!" )
		.setIcon(R.drawable.ic_launcher)
		.setMessage(message)
		.setNegativeButton("Later", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialoginterface, int i) 
			{
				dialoginterface.cancel();   
				prefs.edit().putBoolean("SHOWALARM", false).commit();
			}})
			.setPositiveButton("Now", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialoginterface, int i) 
				{   
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
					prefs.edit().putBoolean("SHOWALARM", false).commit();
					prefs.edit().putBoolean("ALREADYRATED", true).commit();
				}               
			}).show();*/


        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Now", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
                prefs.edit().putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();
                prefs.edit().putBoolean(Constants.PREFERENCES_ALREADY_RATED, true).commit();

            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Later", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
                prefs.edit().putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();

            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Never", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
                prefs.edit().putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();
                prefs.edit().putBoolean(Constants.PREFERENCES_ALREADY_RATED, true).commit();

            }
        });

        dialog.show();

    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(Constants.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(Constants.TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROPERTY_REG_ID, regId);
        editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getPreferences(context);
        String registrationId = prefs.getString(Constants.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(Constants.TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(Constants.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mContext, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }


    private class MyCustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();

            if (url.equals(Constants.terms_and_condition)) {
                return false;
            }
            if (host.equals(Constants.target_url_prefix) || host.equals(Constants.target_url_prefix2)) {
                // This is my web site, so do not override; let my WebView load
                // the page
                if (mWebviewPop != null) {
                    mWebviewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebviewPop);
                    mWebviewPop = null;

                }

                return false;
            }

            if (host.equals("m.facebook.com") || host.equals("www.facebook.com")) {
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch
            // another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            Log.d("onReceivedSslError", "onReceivedSslError");
            //super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub

            try {


                if (url.startsWith(Constants.QUIZ_FEED_URL)) {
                    SharedPreferences pref = getPreferences(mContext);

                    if (fbProfile == null) {
                        String responseFromFb = pref.getString(Constants.FB_USER_INFO, null);
                        if (responseFromFb != null) {
                            fbProfile = new Gson().fromJson(responseFromFb, FbProfile.class);
                            new WebServiceUtility(getApplicationContext(), Constants.SEND_FACEBOOK_DATA, fbProfile);
                        }
                    } else {
                        new WebServiceUtility(getApplicationContext(), Constants.SEND_FACEBOOK_DATA, fbProfile);
                    }


                    //Call execute
                    String regId = getRegistrationId(mContext);
                    String fbId = fbProfile.getFbUserId();
                    String emailId = fbProfile.getEmail();
                    if (regId != null && !regId.isEmpty() && fbId != null && !fbId.isEmpty() && emailId != null && !emailId.isEmpty()) {
                        Registration registration = new Registration();
                        registration.setEmailId(emailId);
                        registration.setFbId(fbId);
                        registration.setRegId(regId);
                        registration.setAppVersion(getAppVersionName(getApplicationContext()));
                        new WebServiceUtility(getApplicationContext(), Constants.USER_INFO_TASK, registration);
                    }


                    new WebServiceUtility(getApplicationContext(), Constants.SEND_APP_ACTIVE_DATA, regId);
                    new WebServiceUtility(getApplicationContext(), Constants.UPDATE_APP, getAppVersionName(getApplicationContext()));


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            progress.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            progress.setVisibility(View.VISIBLE);
            //showDialog();
            super.onPageStarted(view, url, favicon);
        }

    }

    private class MyCustomChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            MyActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(mContext);
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setSavePassword(false);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");
        }


    }

    public void setValue(int progress) {
        this.progress.setProgress(progress);
    }


}