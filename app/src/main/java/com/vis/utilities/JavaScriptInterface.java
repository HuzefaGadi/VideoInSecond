package com.vis.utilities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by huzefaasger on 04-11-2015.
 */
public class JavaScriptInterface {
    private Activity activity;

    public JavaScriptInterface(Activity activiy) {
        this.activity = activiy;
    }
    @JavascriptInterface
    public void shareOnFacebook(String videoAddress){
        /*Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(videoAddress), "video/3gpp");
        activity.startActivity(intent);*/

        Toast.makeText(activity,videoAddress,Toast.LENGTH_LONG).show();
    }
}
