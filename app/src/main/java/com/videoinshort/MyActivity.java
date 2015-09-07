package com.videoinshort;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
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

import com.videoinshort.Analytics.TrackerName;
import com.videoinshort.beans.Contact;
import com.videoinshort.beans.FbProfile;
import com.videoinshort.beans.NotificationMessage;
import com.videoinshort.utilities.Constants;
import com.videoinshort.utilities.Phonebook;
import com.videoinshort.utilities.Utility;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.videoinshort.utilities.WebServiceUtility;

public class MyActivity extends Activity {
	private ProgressBar progress;
	//private ProgressDialog prgDialog;
	protected WebView mainWebView;
	/*private final String REGID_URL = "http://m.buzzonn.com/BuzzonFBList.asmx";
	private final String REGID_SOAP_ACTION = "http://tempuri.org/insertRegId";
	private final String REGID_METHOD_NAME = "insertRegId";*/

	public final String REGID_CALL = "1";
	public final String CONTACT_CALL = "2";


	private Context mContext;	
	private String responseFromService;
	private WebView mWebviewPop;
	private FrameLayout mContainer;
	CookieManager cookieManager;
	private String url = "http://www.videoinshort.com";
	private String target_url_prefix = "m.videoinshort.com";
	private String target_url_prefix2 = "www.videoinshort.com";
	private String terms_and_condition = "http://m1.buzzonn.com/PrivcyPolicy.aspx";

	List <Contact> listOfContacts;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "1065930478790";


	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	private PendingIntent pendingIntent;
	RelativeLayout noInternetMessage;

	SwipeRefreshLayout swipeLayout;
	String regid;
	Button refreshButton;
	Utility utility;
	FbProfile fbProfile;

	@Override
	public void onBackPressed(){
		//	System.out.println("BACK PRESSED-->"+mainWebView.getUrl());

		if(mWebviewPop!=null)
		{
			mWebviewPop.setVisibility(View.GONE);
			mContainer.removeView(mWebviewPop);
			mWebviewPop=null;
		}
		else {
			super.onBackPressed();
			finish();
		}
	}


	@SuppressWarnings("deprecation")
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" }) @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_urimalo);

		String responseFromFb = getIntent().getStringExtra(Constants.FB_USER_INFO);
		if(responseFromFb!=null && !responseFromFb.isEmpty())
		{
			fbProfile = new Gson().fromJson(responseFromFb,FbProfile.class);
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
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			cookieManager.setAcceptThirdPartyCookies(mainWebView, true);
		}
		progress = (ProgressBar) findViewById(R.id.progressBar);
		progress.setMax(100);
		WebSettings webSettings = mainWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setSupportMultipleWindows(true);
		mainWebView.setWebViewClient(new MyCustomWebViewClient());
		mainWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		mainWebView.setWebChromeClient(new MyCustomChromeClient());
		mContext=this;
		if(utility.checkInternetConnectivity(mContext))
		{
			mContainer.setVisibility(View.VISIBLE);
			noInternetMessage.setVisibility(View.GONE);
			mainWebView.loadUrl(url);
		}
		else
		{
			mContainer.setVisibility(View.GONE);
			noInternetMessage.setVisibility(View.VISIBLE);
		}


		/*swipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub

				new Handler().postDelayed(new Runnable() {
					@Override public void run() {
						swipeLayout.setRefreshing(false);
					}
				}, 5000);
				if(checkInternetConnectivity(mContext))
				{
					mContainer.setVisibility(View.VISIBLE);
					noInternetMessage.setVisibility(View.GONE);
					mainWebView.loadUrl(url);

				}
				else
				{

					mContainer.setVisibility(View.GONE);
					noInternetMessage.setVisibility(View.VISIBLE);
				}



			}
		});

		swipeLayout.setColorSchemeResources(
                R.color.orange,
                R.color.green,
                R.color.blue);*/

		/*prgDialog= new ProgressDialog(MyActivity.this);
		prgDialog.setCanceledOnTouchOutside(false);
		prgDialog.setCancelable(false);
		prgDialog.setMessage("Loading..");*/
		/*progress.setProgress(100);*/
		//showDialog();


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
		if(prefs.getBoolean("ALARM_SET", false))
		{

		}
		else
		{
			Intent alarmIntent = new Intent(MyActivity.this, AlarmReceiver.class);
			pendingIntent = PendingIntent.getBroadcast(MyActivity.this, 0, alarmIntent, 0);

			//System.out.println("ALARM CALLED ON CREATE");
			AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			int interval = 1000 * 60 * 60 * 24 * 7;
			int startTime = 1000 * 60 * 60 * 24 * 5;
			/*int interval = 1000 * 60 * 10;
				int startTime = 1000 * 60 * 2;*/
			/* Set the alarm to start at 10:30 AM */
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis()+startTime);
			/* Repeating on every 20 minutes interval */
			manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
					interval, pendingIntent);
			prefs.edit().putBoolean("ALARM_SET", true).commit();
		}


		
		/* Retrieve a PendingIntent that will perform a broadcast */
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		processIntent(getIntent());
	}

	void processIntent(Intent intent)
	{
		String notification = intent.getStringExtra("NOTIFICATION");
		if(notification!=null && !notification.isEmpty())
		{
			AsyncCallWS task = new AsyncCallWS();
			task.execute(Constants.CLICK_INFO_TASK,notification);
		}
		onRefresh();
		
	}
	public void showDialog()
	{
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
		if(utility.checkInternetConnectivity(mContext))
		{
			mContainer.setVisibility(View.VISIBLE);
			noInternetMessage.setVisibility(View.GONE);
			mainWebView.loadUrl(url);

		}
		else
		{

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
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		SharedPreferences pref = getPreferences(this);
		if(pref.getBoolean("SHOWALARM", false) && !pref.getBoolean("ALREADYRATED", false))
		{
			rateUs("You are awesome! If you feel the same about Buzzonn, please take a moment to rate it.");
		}
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		// Check device for Play Services APK.
		checkPlayServices();
	}
	private void rateUs(String message ) {

		final SharedPreferences prefs = getPreferences(this);
		Tracker t = ((Analytics)getApplication()).getTracker(
				TrackerName.APP_TRACKER);
		t.enableAdvertisingIdCollection(true);
		// Build and send an Event.
		t.send(new HitBuilders.EventBuilder()
		.setCategory("Alert View")
		.setAction("Rate Us")
		.setLabel("Rate Us called")

		.build());

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
			}).show();

	}

	private void showUpdateMessage(String message ) {

		final SharedPreferences prefs = getPreferences(this);
		Tracker t = ((Analytics)getApplication()).getTracker(
				TrackerName.APP_TRACKER);
		t.enableAdvertisingIdCollection(true);
		// Build and send an Event.
		t.send(new HitBuilders.EventBuilder()
		.setCategory("Alert View")
		.setAction("Rate Us")
		.setLabel("Rate Us called")

		.build());

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		t.enableAdvertisingIdCollection(true);
		dialog.setTitle( "Update Available!!" )
		.setIcon(R.drawable.ic_launcher)
		.setMessage(message)
		.setNegativeButton("Later", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialoginterface, int i) 
			{
				dialoginterface.cancel();   

			}})
			.setPositiveButton("Now", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialoginterface, int i) 
				{   
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));

				}               
			}).show();

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
	 * @param regId registration ID
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
	 *         registration ID.
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

			if(url.equals(terms_and_condition))
			{
				return false;
			}
			if (host.equals(target_url_prefix) || host.equals(target_url_prefix2))
			{
				// This is my web site, so do not override; let my WebView load
				// the page
				if(mWebviewPop!=null)
				{
					mWebviewPop.setVisibility(View.GONE);
					mContainer.removeView(mWebviewPop);
					mWebviewPop=null;

				}

				return false;
			}

			if(host.equals("m.facebook.com") || host.equals("www.facebook.com"))
			{
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

			if(url.equals(Constants.QUIZ_FEED_URL))
			{
				SharedPreferences pref = getPreferences(mContext);

				if(fbProfile == null)
				{
					String responseFromFb = pref.getString(Constants.FB_USER_INFO,null);
					if(responseFromFb!=null)
					{
						fbProfile = new Gson().fromJson(responseFromFb,FbProfile.class);
					}
				}
				else
				{
					new WebServiceUtility(getApplicationContext(),fbProfile);
				}

				AsyncCallWS task = new AsyncCallWS();
				//Call execute
				String regId=getRegistrationId(mContext);
				String fbId = fbProfile.getFbUserId();
				String emailId = fbProfile.getEmail();
				if(regId!=null && !regId.isEmpty() && fbId!=null && !fbId.isEmpty()&& emailId!=null && !emailId.isEmpty() )
				{
					task.execute(Constants.USER_INFO_TASK,regId,emailId,fbId);
				}
				String cookies = CookieManager.getInstance().getCookie(url);
				if(cookies!=null)
				{
					String[] x = Pattern.compile(";").split(cookies);
					for(int i=0;i<x.length;i++)
					{
						/*if(x[i].contains("MobileInfo"))
						{
							AsyncCallWS task = new AsyncCallWS();
							//Call execute
							String regId=getRegistrationId(mContext);
							String mobileId = x[i].substring(x[i].indexOf("MobileId=")+9);
							if(regId!=null && !regId.isEmpty() && mobileId!=null && !mobileId.isEmpty() )
							{
								task.execute(mobileId,regId);
							}

						}*/

						/*if(x[i].contains("UserInfo"))
						{
							AsyncCallWS task = new AsyncCallWS();
							//Call execute
							String regId=getRegistrationId(mContext);
							String fbId = x[i].substring(x[i].indexOf("FBId=")+5 , x[i].indexOf("&email="));
							String emailId = x[i].substring(x[i].indexOf("&email=")+7);
							if(regId!=null && !regId.isEmpty() && fbId!=null && !fbId.isEmpty()&& emailId!=null && !emailId.isEmpty() )
							{
								task.execute(USER_INFO_TASK,regId,emailId,fbId);
							}
						}*/
						if(x[i].contains("VersionInfo"))
						{
							try
							{
								int newVersion = Integer.parseInt(x[i].substring(x[i].indexOf("version=")+8,x[i].indexOf("&message")));
								//String newVersion =  x[i].substring(x[i].indexOf("version=")+8,x[i].indexOf("&message"));
								String message = x[i].substring(x[i].indexOf("&message=")+9);

								System.out.println("Version "+newVersion+" Message  "+message);
								PackageInfo pInfo;
								try {
									pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
									int versionOfApp = pInfo.versionCode;


									if(versionOfApp < newVersion)
									{
										showUpdateMessage(message);
									} 

								} catch (NameNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							catch(Exception e) 
							{
								// Get tracker.
								Tracker t2 = ((Analytics)getApplication()).getTracker(
										TrackerName.APP_TRACKER);
								// Build and send an Event.
								t2.send(new HitBuilders.EventBuilder()
								.setCategory("Exception")
								.setAction("Exception")
								.setLabel(e.getMessage())
								.build());
							}


						}



					}
				}
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

	private class MyCustomChromeClient extends WebChromeClient
	{

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


	/*public void insertRegId(String userId,String regIdString) {
		//Create request
		SoapObject request = new SoapObject(NAMESPACE,REGID_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo uId = new PropertyInfo();
		//Set Name
		uId.setName("uId");
		//Set Value
		uId.setValue(userId);
		//Set dataType
		uId.setType(String.class);
		//Add the property to request object

		//Property which holds input parameters
		PropertyInfo regId = new PropertyInfo();
		//Set Name
		regId.setName("regId");
		//Set Value
		regId.setValue(regIdString);
		//Set dataType
		regId.setType(String.class);
		//Add the property to request object


		//Add the property to request object
		request.addProperty(uId);
		request.addProperty(regId);

		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);


		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(REGID_URL);

		try {
			//Invole web service
			androidHttpTransport.call(REGID_SOAP_ACTION, envelope);
			//Get the response
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			//Assign it to fahren static variable
			responseFromService = response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	public void insertRegId(String regIdString,String emailId,String fbId,String appVer) {
		//Create request
		SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.REGID_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo regId = new PropertyInfo();
		//Set Name
		regId.setName("RegID");
		//Set Value
		regId.setValue(regIdString);
		//Set dataType
		regId.setType(String.class);
		//Add the property to request object

		//Property which holds input parameters
		PropertyInfo emlId = new PropertyInfo();
		//Set Name
		emlId.setName("emailid");
		//Set Value
		emlId.setValue(emailId);
		//Set dataType
		emlId.setType(String.class);
		//Add the property to request object


		//Property which holds input parameters
		PropertyInfo facebookId = new PropertyInfo();
		//Set Name
		facebookId.setName("fbid");
		//Set Value
		facebookId.setValue(fbId);
		//Set dataType
		facebookId.setType(String.class);

		PropertyInfo appVersion = new PropertyInfo();
		//Set Name
		appVersion.setName("appVersion");
		//Set Value
		appVersion.setValue(appVer);
		//Set dataType
		appVersion.setType(String.class);


		//Add the property to request object
		request.addProperty(regId);
		request.addProperty(emlId);
		request.addProperty(facebookId);
		request.addProperty(appVersion);

		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);


		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.REGID_URL);

		try {
			//Invole web service
			androidHttpTransport.call(Constants.REGID_SOAP_ACTION, envelope);
			//Get the response
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			//Assign it to fahren static variable
			responseFromService = response.toString();
			System.out.println("Response "+responseFromService);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String sendContactDetails(String userNumber) {
		//Create request
		SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.CONTACT_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo regId = new PropertyInfo();
		//Set Name
		regId.setName("mynumID");
		//Set Value
		regId.setValue(userNumber);
		//Set dataType
		regId.setType(Integer.class);
		//Add the property to request object


		SoapObject lstUsers = new SoapObject(Constants.NAMESPACE, "lstusers");

		for(Contact contact:listOfContacts)
		{
			SoapObject clsAndroidUsers = new SoapObject(Constants.NAMESPACE, "clsAndroidUsers");
			clsAndroidUsers.addProperty("_friendname",contact.getName());
			clsAndroidUsers.addProperty("_friendnum",contact.getNumber());
			clsAndroidUsers.addProperty("_friendemail",contact.getEmail());
			/*Bitmap image = contact.getImage();
			if(image!=null)
			{
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compress(Bitmap.CompressFormat.PNG, 100, stream);
				clsAndroidUsers.addProperty("_friendprofilepic",Base64.encode(stream.toByteArray()));
			}
			else
			{
				clsAndroidUsers.addProperty("_friendprofilepic",null);
			}*/

			lstUsers.addSoapObject(clsAndroidUsers);
		}
		//Add the property to request object
		request.addProperty(regId);
		request.addSoapObject(lstUsers);



		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);


		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.CONTACT_URL);

		try {
			//Invole web service
			androidHttpTransport.call(Constants.CONTACT_SOAP_ACTION, envelope);
			//Get the response
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			//Assign it to fahren static variable
			responseFromService = response.toString();
			return responseFromService;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public void sendAcknowledgement(NotificationMessage
			notification)
	{

		//Create request
		SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACK_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo userId = new PropertyInfo();
		//Set Name
		userId.setName("fuid");
		//Set Value
		userId.setValue(notification.getUid());
		//Set dataType
		userId.setType(String.class);
		//Add the property to request object

		//Property which holds input parameters
		PropertyInfo commentId = new PropertyInfo();
		//Set Name
		commentId.setName("cid");
		//Set Value
		commentId.setValue(notification.getCid());
		//Set dataType
		commentId.setType(String.class);
		//Add the property to request object


		//Property which holds input parameters
		PropertyInfo replyId = new PropertyInfo();
		//Set Name
		replyId.setName("rid");
		//Set Value
		replyId.setValue(notification.getRid());
		//Set dataType
		replyId.setType(String.class);

		//Property which holds input parameters
		PropertyInfo clickStatus = new PropertyInfo();
		//Set Name
		clickStatus.setName("clickstatus");
		//Set Value
		clickStatus.setValue("1");
		//Set dataType
		clickStatus.setType(String.class);

		//Property which holds input parameters
		PropertyInfo recStatus = new PropertyInfo();
		//Set Name
		recStatus.setName("recstatus");
		//Set Value
		recStatus.setValue("1");
		//Set dataType
		recStatus.setType(String.class);




		//Add the property to request object
		request.addProperty(userId);
		request.addProperty(commentId);
		request.addProperty(replyId);
		request.addProperty(recStatus);
		request.addProperty(clickStatus);



		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);


		//Create HTTP call object

		HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.ACK_URL);

		try {
			//Invole web service
			androidHttpTransport.call(Constants.ACK_SOAP_ACTION, envelope);
			//Get the response
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			//Assign it to fahren static variable
			responseFromService = response.toString();
			System.out.println("Response from CLICK status"+responseFromService);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class AsyncCallWS extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			Log.i(Constants.TAG, "doInBackground");

			if(params[0].equals(Constants.USER_INFO_TASK))
			{
				SharedPreferences pref = getPreferences(mContext);
				Tracker t = ((Analytics)getApplication()).getTracker(
						TrackerName.APP_TRACKER);
				// Build and send an Event.
				t.send(new HitBuilders.EventBuilder()
				.setCategory("GCM")
				.setAction("Reg Id sent")
				.setLabel("Reg Id upload")
				.build());
				PackageInfo pInfo;
				String appVersion=null;
				try {
					pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
					appVersion = pInfo.versionName;
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				insertRegId(params[1],params[2],params[3],appVersion);
				listOfContacts = new Phonebook(MyActivity.this).readContacts();

				if(pref.getInt("CONTACTS", 0) < listOfContacts.size())
				{
					// Get tracker.
					Tracker t2 = ((Analytics)getApplication()).getTracker(
							TrackerName.APP_TRACKER);
					// Build and send an Event.
					t2.send(new HitBuilders.EventBuilder()
					.setCategory("Contacts")
					.setAction("Contacts sent")
					.setLabel("Contacts upload")
					.build());
					String response = sendContactDetails(params[0]);

					if(response!=null)
					{
						if(response.equals("1"))
						{
							pref.edit().putInt("CONTACTS", listOfContacts.size()).commit();
						}

					}
				}
			}
			else if(params[0].equals(Constants.CLICK_INFO_TASK))
			{
				Gson gson = new Gson();
				NotificationMessage notification = gson.fromJson(params[1], NotificationMessage.class);
				sendAcknowledgement(notification);
			}


			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i(Constants.TAG, "onPostExecute");

		}

		@Override
		protected void onPreExecute() {
			Log.i(Constants.TAG, "onPreExecute");

		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i(Constants.TAG, "onProgressUpdate");
		}



	}



}