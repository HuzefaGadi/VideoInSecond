package com.vis.utilities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

/**
 * Created by huzefaasger on 04-11-2015.
 */
public class JavaScriptInterface {
    private Activity activity;
    ShareDialog shareDialog;
    public JavaScriptInterface(Activity activiy) {
        this.activity = activiy;
    }
    @JavascriptInterface
    public void shareOnFacebook(String videoAddress){
        shareDialog = new ShareDialog(activity);
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Video In Short")
                    .setContentUrl(Uri.parse(videoAddress))
                    .build();

            shareDialog.show(linkContent);
        }

    }
}
