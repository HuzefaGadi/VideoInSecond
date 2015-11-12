package com.vis.utilities;

import android.app.Activity;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
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
    public void shareOnFacebook(String videoAddress) {
        shareDialog = new ShareDialog(activity);
        String youtubetag = "youtube.com/watch?v=";
        String id = videoAddress.substring(videoAddress.indexOf(youtubetag) + youtubetag.length(), videoAddress.length());
        if (ShareDialog.canShow(ShareLinkContent.class)) {

            ShareVideoContent videoContent = new ShareVideoContent.Builder()
                    .setContentUrl(Uri.parse(videoAddress))

                    .setContentTitle("test")
                    .setPreviewPhoto(new SharePhoto.Builder().setImageUrl(Uri.parse("http://img.youtube.com/vi/" + id + "/0.jpg")).build())
                    .build();
            /*ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Video in Short")
                    .setImageUrl(Uri.parse("http://img.youtube.com/vi/" + id + "/0.jpg"))
                    .setContentUrl(Uri.parse(videoAddress))
                    .build();*/

            shareDialog.show(videoContent);
        }

    }
}
