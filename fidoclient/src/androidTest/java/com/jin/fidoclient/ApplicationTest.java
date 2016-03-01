package com.jin.fidoclient;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.jin.fidoclient.constants.Constants;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testBase64Encoded() {
        String base64String = "WWFMaW4tdGVzdDIta2V5LUpESmhKREV3SkhwcFlXUllja2xRU0dSbFZtZDBSM3BoVmsxT1kyVT0\n";
        String base64String1 = base64String.trim();
        String base64Url="zsfjhbCwYi_w-zHTiFvJj7cv-siLlds5DaqhxS9Wt9Y=";
        assertTrue(base64Url.matches(Constants.BASE64_URL_REGULAR));
        assertTrue(base64String1.matches(Constants.BASE64_REGULAR));
    }
}