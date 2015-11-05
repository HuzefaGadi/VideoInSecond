package com.vis.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vis.Analytics;
import com.vis.R;
import com.vis.beans.Contact;
import com.vis.beans.FbProfile;
import com.vis.beans.Location;
import com.vis.beans.NotificationMessage;
import com.vis.beans.Registration;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;

/**
 * Created by huzefaasger on 07-09-2015.
 */
public class WebServiceUtility {

    Context mContext;
    SharedPreferences preferences;
    List<Contact> listOfContacts;

    public WebServiceUtility(Context context, int action, Object object) {
        mContext = context;
        preferences = getPreferences(mContext);
        new AsyncCallWS(action).execute(object);
    }

    private class AsyncCallWS extends AsyncTask<Object, Void, String> {

        int action;

        public AsyncCallWS(int action) {
            this.action = action;
        }

        @Override
        protected String doInBackground(Object... params) {
            Log.i(Constants.TAG, "doInBackground");
            SharedPreferences pref = getPreferences(mContext);

            if (action == Constants.SEND_FACEBOOK_DATA) {
                Tracker t = ((Analytics) mContext.getApplicationContext()).getTracker(
                        Analytics.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("USERDATA")
                        .setAction("User data sent")
                        .setLabel("User data Upload")
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

                insertFacebookNewUserData((FbProfile) params[0]);
            } else if (action == Constants.SEND_APP_ACTIVE_DATA) {
                Tracker t = ((Analytics) mContext.getApplicationContext()).getTracker(
                        Analytics.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("App Active")
                        .setAction("App Opened")
                        .setLabel("App Opened By User")
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

                insertAppActive((String) params[0]);
            } else if (action == Constants.CLICK_INFO_TASK) {
                String json = (String) params[0];
                Gson gson = new Gson();
                try {
                    NotificationMessage notification = gson.fromJson(json, NotificationMessage.class);
                    sendAcknowledgementForClickStatus(notification);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (action == Constants.RECIEVE_INFO_TASK) {
                String json = (String) params[0];
                Gson gson = new Gson();
                try {
                    NotificationMessage notification = gson.fromJson(json, NotificationMessage.class);
                    sendAcknowledgementForRecieveStatus(notification);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (action == Constants.USER_INFO_TASK) {


                Tracker t = ((Analytics) mContext).getTracker(
                        Analytics.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("GCM")
                        .setAction("Reg Id sent")
                        .setLabel("Reg Id upload")
                        .build());
                PackageInfo pInfo;

                Registration registration = (Registration) params[0];
                insertRegId(registration);
                    /*listOfContacts = new Phonebook(mContext).readContacts();

                    if (pref.getInt("CONTACTS", 0) < listOfContacts.size()) {
                        // Get tracker.
                        Tracker t2 = ((Analytics) mContext).getTracker(
                                Analytics.TrackerName.APP_TRACKER);
                        // Build and send an Event.
                        t2.send(new HitBuilders.EventBuilder()
                                .setCategory("Contacts")
                                .setAction("Contacts sent")
                                .setLabel("Contacts upload")
                                .build());
                        String response = sendContactDetails((String) params[0]);

                        if (response != null) {
                            if (response.equals("1")) {
                                pref.edit().putInt("CONTACTS", listOfContacts.size()).commit();
                            }

                        }
                    }*/

            } else if (action == Constants.UPDATE_APP) {
                return getAppVersion((String) params[0]);
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(Constants.TAG, "onPostExecute");

            if (action == Constants.UPDATE_APP) {
                if (result != null) {
                    if (result.equals("1")) {
                        showUpdateMessage("Update your App!!");
                    }
                }
            }

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
            System.out.println("Response for New Facebook User" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertAppActive(String regId) {

        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACTIVE_METHOD_NAME);

        PropertyInfo mobileRegistrationId = new PropertyInfo();
        mobileRegistrationId.setName("mobileRegistrationId");
        mobileRegistrationId.setValue(regId);
        mobileRegistrationId.setType(String.class);

        request.addProperty(mobileRegistrationId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.ACTIVE_URL);


        try {
            //Invole web service
            androidHttpTransport.call(Constants.ACTIVE_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response For Insert App Active" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   /* public void sendAcknowledgementForClickStatus(NotificationMessage
                                                          notification) {

        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACK_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(notification.getUid());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo notificationId = new PropertyInfo();
        //Set Name
        notificationId.setName("NotificationId");
        //Set Value
        notificationId.setValue(notification.getNotificationId());
        //Set dataType
        notificationId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo clickStatus = new PropertyInfo();
        //Set Name
        clickStatus.setName("clickStatus");
        //Set Value
        clickStatus.setValue("1");
        //Set dataType
        clickStatus.setType(String.class);

        //Property which holds input parameters
        PropertyInfo recStatus = new PropertyInfo();
        //Set Name
        recStatus.setName("receiveStatus");
        //Set Value
        recStatus.setValue("1");
        //Set dataType
        recStatus.setType(String.class);


        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(notificationId);
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
            String responseFromService = response.toString();
            System.out.println("Response from CLICK status" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/

    public void sendAcknowledgementForClickStatus(NotificationMessage
                                                          notification) {

        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACK_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(notification.getUid());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo notificationId = new PropertyInfo();
        //Set Name
        notificationId.setName("NotificationId");
        //Set Value
        notificationId.setValue(notification.getNotificationId());
        //Set dataType
        notificationId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo clickStatus = new PropertyInfo();
        //Set Name
        clickStatus.setName("clickStatus");
        //Set Value
        clickStatus.setValue("1");
        //Set dataType
        clickStatus.setType(String.class);

        //Property which holds input parameters
        PropertyInfo recStatus = new PropertyInfo();
        //Set Name
        recStatus.setName("receiveStatus");
        //Set Value
        recStatus.setValue("1");
        //Set dataType
        recStatus.setType(String.class);


        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(notificationId);
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
            String responseFromService = response.toString();
            System.out.println("Response from CLICK status" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendAcknowledgementForRecieveStatus(NotificationMessage
                                                            notification) {

        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACK_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(notification.getUid());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo notificationId = new PropertyInfo();
        //Set Name
        notificationId.setName("NotificationId");
        //Set Value
        notificationId.setValue(notification.getNotificationId());
        //Set dataType
        notificationId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo clickStatus = new PropertyInfo();
        //Set Name
        clickStatus.setName("clickStatus");
        //Set Value
        clickStatus.setValue("0");
        //Set dataType
        clickStatus.setType(String.class);

        //Property which holds input parameters
        PropertyInfo recStatus = new PropertyInfo();
        //Set Name
        recStatus.setName("receiveStatus");
        //Set Value
        recStatus.setValue("1");
        //Set dataType
        recStatus.setType(String.class);


        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(notificationId);
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
            String responseFromService = response.toString();
            System.out.println("Response from Recieve " + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void insertRegId(Registration registration) {
        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.REGID_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo regId = new PropertyInfo();
        //Set Name
        regId.setName("RegID");
        //Set Value
        regId.setValue(registration.getRegId());
        //Set dataType
        regId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo emlId = new PropertyInfo();
        //Set Name
        emlId.setName("emailid");
        //Set Value
        emlId.setValue(registration.getEmailId());
        //Set dataType
        emlId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo facebookId = new PropertyInfo();
        //Set Name
        facebookId.setName("fbid");
        //Set Value
        facebookId.setValue(registration.getFbId());
        //Set dataType
        facebookId.setType(String.class);

        PropertyInfo appVersion = new PropertyInfo();
        //Set Name
        appVersion.setName("appVersion");
        //Set Value
        appVersion.setValue(registration.getAppVersion());
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
            String responseFromService = response.toString();
            System.out.println("Response for regId" + responseFromService);
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

        for (Contact contact : listOfContacts) {
            SoapObject clsAndroidUsers = new SoapObject(Constants.NAMESPACE, "clsAndroidUsers");
            clsAndroidUsers.addProperty("_friendname", contact.getName());
            clsAndroidUsers.addProperty("_friendnum", contact.getNumber());
            clsAndroidUsers.addProperty("_friendemail", contact.getEmail());
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
            String responseFromService = response.toString();
            return responseFromService;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getAppVersion(String appVer) {

        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.VERSION_METHOD_NAME);

        PropertyInfo appVersion = new PropertyInfo();
        appVersion.setName("appVersion");
        appVersion.setValue(appVer);
        appVersion.setType(String.class);

        request.addProperty(appVersion);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.VERSION_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.VERSION_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();

            System.out.println("Response For Insert App Active" + responseFromService);
            return responseFromService;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showUpdateMessage(String message) {

        final SharedPreferences prefs = getPreferences(mContext);
        Tracker t = ((Analytics) mContext).getTracker(
                Analytics.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Alert View")
                .setAction("Rate Us")
                .setLabel("Rate Us called")

                .build());

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        t.enableAdvertisingIdCollection(true);
        dialog.setTitle("Update Available!!")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();

                    }
                })
                .setPositiveButton("Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getApplicationContext().getPackageName())));

                    }
                }).show();

    }
}
