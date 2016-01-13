package com.jin.fidoclient.asm.db;

/**
 * Created by YaLin on 2016/1/13.
 */
public class RegRecord {
    public int id;
    public int touchId;
    public String keyId;
    public String appId;
    public String username;
    public String userPrivateKey;
    public String userPublicKey;

    public RegRecord id(int id) {
        this.id = id;
        return this;
    }

    public RegRecord touchId(int touchId) {
        this.touchId = touchId;
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
}
