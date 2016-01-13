package com.jin.fidotest.data;

/**
 * Created by YaLin on 2016/1/12.
 */
public class AuthenticatorRecord {
    private static final String DLM = "#";
    public String AAID;
    public String KeyID;
    public String deviceId;
    public String username;
    public String status;

    public String toString() {
        return AAID + DLM + KeyID;
    }
}
