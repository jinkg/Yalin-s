package com.jin.fidoclient.msg;

import com.google.gson.Gson;

/**
 * Created by YaLin on 2016/1/13.
 */
public class RegRecord {
    public static final String KEY_ID = "_id";
    public static final String KEY_AUTH_TYPE = "type";
    public static final String KEY_BIOMETRICS_ID = "biometrics_id";
    public static final String KEY_AAID = "aa_id";
    public static final String KEY_KEY_ID = "key_id";
    public static final String KEY_APP_ID = "app_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USER_PRIVATE_KEY = "private_key";
    public static final String KEY_USER_PUBLIC_KEY = "public_key";

    public int id;
    public String type;
    public String biometricsId;
    public String aaid;
    public String keyId;
    public String appId;
    public String username;
    public String userPrivateKey;
    public String userPublicKey;

    public RegRecord id(int id) {
        this.id = id;
        return this;
    }

    public RegRecord type(String type) {
        this.type = type;
        return this;
    }

    public RegRecord biometricsId(String biometricsId) {
        this.biometricsId = biometricsId;
        return this;
    }

    public RegRecord aaid(String aaid) {
        this.aaid = aaid;
        return this;
    }

    public RegRecord keyId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    public RegRecord appId(String appId) {
        this.appId = appId;
        return this;
    }

    public RegRecord username(String username) {
        this.username = username;
        return this;
    }

    public RegRecord userPrivateKey(String userPrivateKey) {
        this.userPrivateKey = userPrivateKey;
        return this;
    }

    public RegRecord userPublicKey(String userPublicKey) {
        this.userPublicKey = userPublicKey;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
