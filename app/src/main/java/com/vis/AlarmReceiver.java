package com.vis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.vis.utilities.Constants;

public class AlarmReceiver extends BroadcastReceiver {

    SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            /* Setting the alarm here */
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24 * 7, pendingIntent);
            Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show();
        } else {
            //System.out.println("ALARM CALLED");
            prefs = getPreferences(context);
            prefs.edit().putBoolean(Constants.PREFERENCES_SHOW_ALARM, true).commit();
            //Toast.makeText(context, "ALARM CALLED", Toast.LENGTH_LONG).show();
        }
    }

    private SharedPreferences getPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }
}