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

    public void testCert() {
        try {
            byte[] pub = KeyCodec.getKeyAsRawBytes((ECPublicKey) KeyCodec.getPubKey(Base64.decode(AttestCert.pubCert, Base64.URL_SAFE)));
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
                    new ByteArrayInputStream(pub));
            String certBase64 = Base64.encodeToString(cert.getEncoded(), Base64.URL_SAFE);
            System.out.println(certBase64);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}