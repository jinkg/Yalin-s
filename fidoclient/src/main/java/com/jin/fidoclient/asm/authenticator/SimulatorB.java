package com.jin.fidoclient.asm.authenticator;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.jin.fidoclient.R;
import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.asm.db.UAFDBHelper;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;
import com.jin.fidoclient.client.AuthenticationRequestProcessor;
import com.jin.fidoclient.client.RegistrationRequestProcessor;
import com.jin.fidoclient.crypto.BCrypt;
import com.jin.fidoclient.crypto.KeyCodec;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.utils.StatLog;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by YaLin on 2016/1/18.
 */
public class SimulatorB extends Simulator {
    private static final String CERT_BASE64 = "MIIBsDCCAVOgAwIBAgIEWkp1CzAMBggqhkjOPQQDAgUAMEwxCjAIBgNVBAYTATUxCjAIBgNVBAgTATQxCjAIBgNVBAcTATMxCjAIBgNVBAoTATIxCjAIBgNVBAsTATExDjAMBgNVBAMTBXlhbGluMB4XDTE2MDIxNjA4MjEwN1oXDTE2MDUxNjA4MjEwN1owTDEKMAgGA1UEBhMBNTEKMAgGA1UECBMBNDEKMAgGA1UEBxMBMzEKMAgGA1UEChMBMjEKMAgGA1UECxMBMTEOMAwGA1UEAxMFeWFsaW4wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQMCxHxENhZnSHpI_9TcQZtEFMuM6U6mS5DkcCWk_adbB7u79XNGctb571bngUjdtxYIQh34xezFu039rovkUXuoyEwHzAdBgNVHQ4EFgQUk1cphLxqkmvDiDe36r6gfpmnRSEwDAYIKoZIzj0EAwIFAANJADBGAiEAmmxOTbpNdtG_zycJkBmMzxfKIcx4UcYfQy2xmSiUt2cCIQCgGFy5P4w1dHKESXtaC3bCgRREY4MY4Ky7YkH7P9B0cw==";
    private static final String PUBLIC_KEY_BASE64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEDAsR8RDYWZ0h6SP_U3EGbRBTLjOlOpkuQ5HAlpP2nWwe7u_VzRnLW-e9W54FI3bcWCEId-MXsxbtN_a6L5FF7g==";
    private static final String PRIVATE_KEY_BASE64 = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCAyB-ylNKt5MH7w8Fh85v5F7IyUKcik8d7-taJ1PNEQXQ==";

    private static final String TAG = SimulatorB.class.getSimpleName();
    //    public static final String AAID = "EBA0#0001";
    public static final String AAID = "EEEE#0001";
    private static final String SIMULATOR_TYPE = "face";
    private static final String ASSERTION_SCHEME = "UAFV1TLV";

    private static SimulatorB sInstance;

    private SimulatorB() {
    }

    public static SimulatorB getInstance() {
        if (sInstance == null) {
            sInstance = new SimulatorB();
        }
        return sInstance;
    }

    @Override
    public RegisterOut register(@NonNull String biometricsId, @NonNull RegisterIn registerIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        if (TextUtils.isEmpty(biometricsId)) {
            throw new IllegalArgumentException();
        }
        StatLog.printLog(TAG, "simulatorB register biometricsId: " + biometricsId + " registerIn: " + new Gson().toJson(registerIn));
        UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (!dbHelper.registered(db, biometricsId)) {
            RegRecord regRecord = new RegRecord();
            regRecord.type = SIMULATOR_TYPE;
            regRecord.biometricsId = biometricsId;
            regRecord.aaid = AAID;
            regRecord.keyId = getKeyId();
            regRecord.username = registerIn.username;
            regRecord.appId = registerIn.appID;

            KeyPair keyPair = KeyCodec.getKeyPair();
            byte[] keyIdBytes = regRecord.keyId.getBytes();

            RegistrationRequestProcessor p = new RegistrationRequestProcessor();
            RegisterOut registerOut = p.processRequest(registerIn, keyPair, keyIdBytes, this);

            regRecord.userPrivateKey = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.URL_SAFE);
            regRecord.userPublicKey = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.URL_SAFE);

            dbHelper.addRecord(db, regRecord);
            StatLog.printLog(TAG, "simulatorB register success registerOut: " + new Gson().toJson(registerOut));
            return registerOut;
        }

        return null;
    }

    @Override
    public AuthenticateOut authenticate(@NonNull String biometricsId, @NonNull AuthenticateIn authenticateIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        StatLog.printLog(TAG, "simulatorB authenticate biometricsId: " + biometricsId + " authenticateIn: " + new Gson().toJson(authenticateIn));
        UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        RegRecord regRecord = dbHelper.getUserRecord(db, biometricsId);
        if (regRecord == null) {
            throw new IllegalStateException("you not have reg uaf");
        }

        AuthenticationRequestProcessor p = new AuthenticationRequestProcessor();
        AuthenticateOut authenticateOut = p.processRequest(regRecord, authenticateIn, ASSERTION_SCHEME);
        StatLog.printLog(TAG, "simulatorB authenticate success authenticateOut: " + new Gson().toJson(authenticateOut));
        return authenticateOut;
    }

    @Override
    public String getAAID() {
        return AAID;
    }

    @Override
    public String getScheme() {
        return ASSERTION_SCHEME;
    }

    @Override
    public String getKeyId() {
        String keyId = "YaLin-test2-key-" + Base64.encodeToString(BCrypt.gensalt().getBytes(), Base64.NO_WRAP);
        keyId = Base64.encodeToString(keyId.getBytes(), Base64.NO_PADDING);

        return keyId;
    }

    @Override
    protected String getPrivateKey() {
        return PRIVATE_KEY_BASE64;
    }

    @Override
    public String getPublicKey() {
        return PUBLIC_KEY_BASE64;
    }

    @Override
    public String getCert() {
        return CERT_BASE64;
    }

    @Override
    protected AuthenticatorInfo getInfo() {
        AuthenticatorInfo authenticatorInfo = new AuthenticatorInfo();

        Version[] versions = new Version[1];
        versions[0] = new Version(1, 0);
        String[] extendIds = new String[1];
        extendIds[0] = "abc123";

        int[] attestationTypes = new int[2];
        attestationTypes[0] = TAG_ATTESTATION_BASIC_SURROGATE;
        attestationTypes[1] = TAG_ATTESTATION_BASIC_FULL;
        authenticatorInfo.hasSettings(false)
                .aaid(AAID)
                .asmVersions(versions)
                .supportedExtensionIDs(extendIds)
                .assertionScheme(ASSERTION_SCHEME)
                .authenticationAlgorithm(UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER)
                .attestationTypes(attestationTypes)
                .userVerification(USER_VERIFY_FACEPRINT)
                .keyProtection(KEY_PROTECTION_SOFTWARE)
                .matcherProtection(MATCHER_PROTECTION_SOFTWARE)
                .attachmentHint(ATTACHMENT_HINT_INTERNAL)
                .isSecondFactorOnly(false)
                .isRoamingAuthenticator(false)
                .tcDisplay(TRANSACTION_CONFIRMATION_DISPLAY_ANY);
        authenticatorInfo.title = SIMULATOR_TYPE;

        return authenticatorInfo;
    }

    @Override
    public void showBiometricsAuth(@NonNull Activity activity, final BiometricsAuthResultCallback callback) {
        final String[] ids = activity.getResources().getStringArray(R.array.touch_ids);
        new AlertDialog.Builder(activity)
                .setItems(ids,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (callback != null) {
                                    callback.onAuthSuccess(SimulatorB.this, ids[which]);
                                }
                            }
                        }).show();
    }
}
