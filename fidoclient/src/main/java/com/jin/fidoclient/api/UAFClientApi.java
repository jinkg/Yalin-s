package com.jin.fidoclient.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.jin.fidoclient.msg.client.UAFMessage;

/**
 * Created by YaLin on 2016/1/11.
 */
public class UAFClientApi {

    private static Context sContext;

    public static void init(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        if (sContext == null) {
            throw new IllegalStateException("you must init UAFClientApi in application onCreate!");
        }
        return sContext;
    }

    public static void doDiscover(Activity activity, int requestCode) {
        Intent intent = UAFIntent.getDiscoverIntent();
        activity.startActivityForResult(intent, requestCode);
    }

    public static void doOperation(Activity activity, int requestCode, String responseMessage, String channelBinding) {
        Intent intent = UAFIntent.getUAFOperationIntent(new UAFMessage(responseMessage).toJson(), null, channelBinding);
        activity.startActivityForResult(intent, requestCode);
    }

}
