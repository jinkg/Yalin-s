package com.jin.fidoclient.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.jin.fidoclient.msg.AsmInfo;
import com.jin.fidoclient.msg.DeRegisterAuthenticator;
import com.jin.fidoclient.msg.DeRegistrationRequest;
import com.jin.fidoclient.msg.RegRecord;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.ui.UAFClientActivity;
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
        if (activity == null) {
            throw new IllegalArgumentException();
        }
        Intent intent = UAFIntent.getDiscoverIntent();
        activity.startActivityForResult(intent, requestCode);
    }

    public static void doCheckPolicy(Activity activity, int requestCode, String responseMessage) {
        if (activity == null || TextUtils.isEmpty(responseMessage)) {
            throw new IllegalArgumentException();
        }
        Intent intent = UAFIntent.getCheckPolicyIntent(new UAFMessage(responseMessage).toJson(), activity.getApplication().getPackageName());
        activity.startActivityForResult(intent, requestCode);
    }

    public static void doOperation(Activity activity, int requestCode, String responseMessage, String channelBinding) {
        if (activity == null || TextUtils.isEmpty(responseMessage)) {
            throw new IllegalArgumentException();
        }
        Intent intent = UAFIntent.getUAFOperationIntent(new UAFMessage(responseMessage).toJson(), null, channelBinding);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void doOperation(Fragment fragment, int requestCode, String responseMessage, String channelBinding) {
        if (fragment == null || TextUtils.isEmpty(responseMessage)) {
            throw new IllegalArgumentException();
        }
        Intent intent = UAFIntent.getUAFOperationIntent(new UAFMessage(responseMessage).toJson(), null, channelBinding);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static List<RegRecord> getRegRecords(String username) {
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException();
        }
        return null;
    }

    public static String getFacetId() {
        return Utils.getFacetId(getContext());
    }

    public static DeRegistrationRequest[] getDeRegistrationRequests(RegRecord regRecord) {
        if (regRecord == null) {
            throw new IllegalArgumentException("regRecord must not be null!");
        }
        DeRegistrationRequest[] deRegistrationRequests = new DeRegistrationRequest[1];
        deRegistrationRequests[0] = new DeRegistrationRequest();
        deRegistrationRequests[0].authenticators = new DeRegisterAuthenticator[1];
        deRegistrationRequests[0].authenticators[0] = new DeRegisterAuthenticator();
        deRegistrationRequests[0].authenticators[0].aaid = regRecord.aaid;
        deRegistrationRequests[0].authenticators[0].keyID = regRecord.keyId;

        return deRegistrationRequests;
    }

    public static AsmInfo getDefaultAsmInfo() {
        return UAFClientActivity.getAsmInfo(getContext());
    }

    public static void clearDefaultAsm() {
        UAFClientActivity.setAsmInfo(getContext(), null);
    }
}
