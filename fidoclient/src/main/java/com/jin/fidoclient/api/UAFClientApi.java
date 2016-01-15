package com.jin.fidoclient.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;

import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.asm.db.UAFDBHelper;
import com.jin.fidoclient.msg.DeregisterAuthenticator;
import com.jin.fidoclient.msg.DeregistrationRequest;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.utils.Utils;

import java.util.List;

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

    public static void doOperation(Fragment fragment, int requestCode, String responseMessage, String channelBinding) {
        Intent intent = UAFIntent.getUAFOperationIntent(new UAFMessage(responseMessage).toJson(), null, channelBinding);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static List<RegRecord> getRegRecords(String username) {
        UAFDBHelper helper = UAFDBHelper.getInstance(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        return helper.getUserRecords(db, username);
    }

    public static String getFacetId() {
        return Utils.getFacetId(getContext());
    }

    public static DeregistrationRequest[] getDeregistrationRequests(RegRecord regRecord) {
        if (regRecord == null) {
            throw new IllegalArgumentException("regRecord must not be null!");
        }
        DeregistrationRequest[] deregistrationRequests = new DeregistrationRequest[1];
        deregistrationRequests[0] = new DeregistrationRequest();
        deregistrationRequests[0].authenticators = new DeregisterAuthenticator[1];
        deregistrationRequests[0].authenticators[0] = new DeregisterAuthenticator();
        deregistrationRequests[0].authenticators[0].aaid = ASMApi.getAAID();
        deregistrationRequests[0].authenticators[0].keyID = regRecord.keyId;

        return deregistrationRequests;
    }
}
