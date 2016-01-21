package com.jin.fidoclient.msg;

/**
 * Created by YaLin on 2016/1/21.
 */
public class DiscoverData {
    public Version[] supportedUAFVersions;
    public String clientVendor;
    public Version clientVersion;
    public Authenticator[] availableAuthenticators;
}
