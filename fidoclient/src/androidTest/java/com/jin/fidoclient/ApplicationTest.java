package com.jin.fidoclient;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Base64;

import com.jin.fidoclient.crypto.KeyCodec;

import org.spongycastle.jce.interfaces.ECPublicKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }


    public void testString() {
        String msg = "[{\"challenge\":\"JDJhJDEwJGpTdXZkN0hOS0pEZS91Tm11LksxMGU\",\"header\":{\"appID\":\"https://www.head2toes.org/fidouaf/v1/public/uaf/facets\",\"op\":\"Auth\",\"serverData\":\"MTdiOGJmMTM4MDNjN2ZjZTAwNzYxYjM3MWIxODIyNjE0MDUyOGMyYzJlODRiOGFkNzI2ODc0NjIzNTlhMjE1OS5NVFExTmpFNU5UUTBPVE0zTWcuU2tSS2FFcEVSWGRLUjNCVVpGaGFhMDR3YUU5VE1IQkZXbE01TVZSdE1URk1hM040VFVkVg\",\"upv\":{\"major\":1,\"minor\":0}},\"policy\":{\"accepted\":[[{\"aaid\":[\"EBA0#0001\"]}],[{\"aaid\":[\"4e4e#4005\"]}],[{\"aaid\":[\"EEEE#0001\"]}],[{\"aaid\":[\"0012#0002\"]}],[{\"aaid\":[\"0010#0001\"]}],[{\"aaid\":[\"4e4e#0001\"]}],[{\"aaid\":[\"5143#0001\"]}],[{\"aaid\":[\"0011#0701\"]}],[{\"aaid\":[\"0013#0001\"]}],[{\"aaid\":[\"0014#0000\"]}],[{\"aaid\":[\"0014#0001\"]}],[{\"aaid\":[\"53EC#C002\"]}],[{\"aaid\":[\"DAB8#8001\"]}],[{\"aaid\":[\"DAB8#0011\"]}],[{\"aaid\":[\"DAB8#8011\"]}],[{\"aaid\":[\"5143#0111\"]}],[{\"aaid\":[\"5143#0120\"]}],[{\"aaid\":[\"4746#F816\"]}],[{\"aaid\":[\"53EC#3801\"]}]]}}]";
        String UPV_TAG = "{\"major\": 1, \"minor\": 0}";
        String MAJOR_TAG = "\"major\": 1";
        String MINOR_TAG = "\"minor\": 0";
        assert (msg.contains(UPV_TAG));
        assert (msg.contains(MAJOR_TAG));
        assert (msg.contains(MINOR_TAG));
    }
}