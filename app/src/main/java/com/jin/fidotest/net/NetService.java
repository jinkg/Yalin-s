package com.jin.fidotest.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;


import com.jin.fidotest.MyApplication;

import java.util.Map;

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

    public static final String BASE_URL = "http://192.168.10.243:8080";

    public static final String START_REG_SUB = "/v1/public/regRequest/";
    public static final String FINISH_REG_SUB = "/v1/public/regResponse";
    public static final String START_AUTH_SUB = "/v1/public/authRequest/";
    public static final String FINISH_AUTH_SUB = "/v1/public/authResponse";

    public static String FINISH_REG_URL;
    public static String FINISH_AUTH_URL;

    private static void init(Context context) {
        getHttpUrl(context);
        FINISH_REG_URL = HTTP_URL + FINISH_REG_SUB;
        FINISH_AUTH_URL = HTTP_URL + FINISH_AUTH_SUB;
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
        SharedPreferences sp = context.getSharedPreferences(
                HTTP_SP, Context.MODE_PRIVATE);
        return sp;
    }

    public static String getStartRegUrl(String username, String appId) {
        return BASE_URL + START_REG_SUB + username + "/" + appId;
    }

    public static String getStartAuthUrl(String appId) {
        return BASE_URL + START_AUTH_SUB + appId;
    }

    public static Map<String, String> getHostAuthHeader(String token) {
        Map<String, String> params = new ArrayMap<>();
        params.put("Authorization", "bearer " + token);
        return params;
    }

    public static Map<String, String> register(String username, String password) {
        ArrayMap params = new ArrayMap();
        params.put("username", username);
        params.put("password", password);
        return params;
    }
}
