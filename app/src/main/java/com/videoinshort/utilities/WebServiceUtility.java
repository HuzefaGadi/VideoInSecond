package com.videoinshort.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.videoinshort.Analytics;
import com.videoinshort.MyActivity;
import com.videoinshort.beans.FbProfile;
import com.videoinshort.beans.Location;
import com.videoinshort.beans.NotificationMessage;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by huzefaasger on 07-09-2015.
 */
public class WebServiceUtility {

    Context mContext;
    SharedPreferences preferences;

    public WebServiceUtility(Context context, FbProfile fbProfile) {
        mContext = context;

        preferences = getPreferences(mContext);
        new AsyncCallWS().execute(fbProfile);
    }

    private class AsyncCallWS extends AsyncTask<FbProfile, Void, Void> {
        @Override
        protected Void doInBackground(FbProfile... params) {
            Log.i(Constants.TAG, "doInBackground");


            SharedPreferences pref = getPreferences(mContext);
            Tracker t = ((Analytics) mContext.getApplicationContext()).getTracker(
                    Analytics.TrackerName.APP_TRACKER);
            // Build and send an Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory("GCM")
                    .setAction("Reg Id sent")
                    .setLabel("Reg Id upload")
                    .build());
            PackageInfo pInfo;
            String appVersion = null;
            try {
                pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                appVersion = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            insertFacebookNewUserData(params[0]);


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

    private SharedPreferences getPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return mContext.getSharedPreferences(Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    public void insertFacebookNewUserData(FbProfile fbProfile) {

        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.NEW_USER_METHOD_NAME);
        PropertyInfo firstName = new PropertyInfo();
        firstName.setName("FirstName");
        firstName.setValue(fbProfile.getFirstName());
        firstName.setType(String.class);

        PropertyInfo lastName = new PropertyInfo();
        lastName.setName("LastName");
        lastName.setValue(fbProfile.getLastName());
        lastName.setType(String.class);

        PropertyInfo email = new PropertyInfo();
        email.setName("Email");
        email.setValue(fbProfile.getEmail());
        email.setType(String.class);

        PropertyInfo cityName = new PropertyInfo();
        cityName.setName("CityName");
        cityName.setType(String.class);


        PropertyInfo countryName = new PropertyInfo();
        countryName.setName("CountryName");
        countryName.setType(String.class);
        Location location = fbProfile.getLocation();
        if (location != null) {
            if (location.getCountry() == null && location.getCity() == null) {
                cityName.setValue(location.getName());
                countryName.setValue("");
            } else {
                cityName.setValue(location.getCity());
                countryName.setValue(location.getCountry());
            }
        } else {
            cityName.setValue("");
            countryName.setValue("");
        }


        PropertyInfo profileImagePath = new PropertyInfo();
        profileImagePath.setName("ProfileImagePath");
        profileImagePath.setValue(fbProfile.getProfileImagePath());
        profileImagePath.setType(String.class);

        PropertyInfo fbUserId = new PropertyInfo();
        fbUserId.setName("FBUserId");
        fbUserId.setValue(fbProfile.getFbUserId());
        fbUserId.setType(String.class);

        PropertyInfo dob = new PropertyInfo();
        dob.setName("dob");
        dob.setValue(fbProfile.getDateOfBirth());
        dob.setType(String.class);

        PropertyInfo gender = new PropertyInfo();
        gender.setName("Gender");
        gender.setValue(fbProfile.getGender());
        gender.setType(String.class);

        PropertyInfo facebookProfileLink = new PropertyInfo();
        facebookProfileLink.setName("FacebookProfileLink");
        facebookProfileLink.setValue(fbProfile.getFbProfileLink());
        facebookProfileLink.setType(String.class);


        String registrationId = preferences.getString(Constants.PROPERTY_REG_ID, "");
        PropertyInfo mobRegId = new PropertyInfo();
        mobRegId.setName("MobRegId");
        mobRegId.setValue(registrationId);
        mobRegId.setType(String.class);

        PropertyInfo mobNumber = new PropertyInfo();
        mobNumber.setName("MobNumber");
        mobNumber.setValue(fbProfile.getMobileNumber());
        mobNumber.setType(String.class);


        request.addProperty(firstName);
        request.addProperty(lastName);
        request.addProperty(email);
        request.addProperty(cityName);
        request.addProperty(countryName);
        request.addProperty(profileImagePath);
        request.addProperty(fbUserId);
        request.addProperty(dob);
        request.addProperty(gender);
        request.addProperty(facebookProfileLink);
        request.addProperty(mobRegId);
        request.addProperty(mobNumber);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.NEW_USER_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.NEW_USER_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response " + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
