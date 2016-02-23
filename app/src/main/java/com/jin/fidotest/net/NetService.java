package com.jin.fidotest.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import com.jin.fidotest.app.MyApplication;

/**
 * Created by YaLin on 2015/9/15.
 */
public class NetService {
    static {
        init(MyApplication.getContext());
    }

    private static final String HTTP_SP = "http_url";
    public static final String HTTP_KEY = "http";

    public static String HTTP_URL;

    public static final String BASE_URL = "http://192.168.1.104:8080";

    private static final String START_REG_SUB = "/v1/public/regRequest/";
    private static final String FINISH_REG_SUB = "/v1/public/regResponse";
    private static final String START_AUTH_SUB = "/v1/public/authRequest/";
    private static final String FINISH_AUTH_SUB = "/v1/public/authResponse";
    private static final String DEREG_SUB = "/v1/public/deregRequest";

    public static String FINISH_REG_URL;
    public static String FINISH_AUTH_URL;
    public static String DEREG_URL;

    private static void init(Context context) {
        getHttpUrl(context);
        FINISH_REG_URL = HTTP_URL + FINISH_REG_SUB;
        FINISH_AUTH_URL = HTTP_URL + FINISH_AUTH_SUB;
        DEREG_URL = HTTP_URL + DEREG_SUB;
    }

    public static String getHttpUrl(Context context) {
        if (TextUtils.isEmpty(HTTP_URL)) {
            SharedPreferences sp = context.getSharedPreferences(
                    HTTP_SP, Context.MODE_PRIVATE);
            HTTP_URL = sp.getString(HTTP_KEY, BASE_URL);
        }
        return HTTP_URL;
    }

    public static void storeHttpUrl(Context context, String server) {
        SharedPreferences sp = context.getSharedPreferences(
                HTTP_SP, Context.MODE_PRIVATE);
        HTTP_URL = server;
        sp.edit().putString(HTTP_KEY, server)
                .apply();
        init(context);
    }

    public static SharedPreferences getNetSp(Context context) {
        return context.getSharedPreferences(
                HTTP_SP, Context.MODE_PRIVATE);
    }

    public static String getStartRegUrl(String username, String appId) {
        return BASE_URL + START_REG_SUB + username + "/" + appId;
    }

    public static String getStartAuthUrl(String appId) {
        return BASE_URL + START_AUTH_SUB + appId;
    }
}
