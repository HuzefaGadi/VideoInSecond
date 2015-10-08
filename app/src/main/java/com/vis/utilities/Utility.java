package com.vis.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {
	
	public boolean checkInternetConnectivity(Context context) {
		boolean isConnected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (networkInfo != null && networkInfo.isConnected())
				isConnected = true;
			if (mobileNetworkInfo != null && mobileNetworkInfo.isConnected())
				isConnected = true;
		}
		return isConnected;
	}

}
