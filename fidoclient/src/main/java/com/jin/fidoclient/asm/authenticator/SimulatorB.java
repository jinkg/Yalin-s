package com.jin.fidoclient.asm.authenticator;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
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
import com.jin.fidoclient.utils.StatLog;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by YaLin on 2016/1/18.
 */
public class SimulatorB extends Simulator {
    private static final String TAG = SimulatorB.class.getSimpleName();
    public static final String AAID = "0015#0001";
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
    public RegisterOut register(String biometricsId, RegisterIn registerIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        if (TextUtils.isEmpty(biometricsId) || registerIn == null) {
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
            RegisterOut registerOut = p.processRequest(registerIn, keyPair, keyIdBytes, AAID);

            regRecord.userPrivateKey = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.URL_SAFE);
            regRecord.userPublicKey = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.URL_SAFE);

            dbHelper.addRecord(db, regRecord);
            StatLog.printLog(TAG, "simulatorB register success registerOut: " + new Gson().toJson(registerOut));
            return registerOut;
        }

        return null;
    }

    @Override
    public AuthenticateOut authenticate(String biometricsId, AuthenticateIn authenticateIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
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
    protected String getKeyId() {
        String keyId = "yalin-test2-key-" + Base64.encodeToString(BCrypt.gensalt().getBytes(), Base64.NO_WRAP);
        keyId = Base64.encodeToString(keyId.getBytes(), Base64.URL_SAFE);

        return keyId;
    }

    @Override
    protected AuthenticatorInfo getInfo() {
        AuthenticatorInfo authenticatorInfo = new AuthenticatorInfo();

        int[] attestationTypes = new int[2];
        attestationTypes[0] = TAG_ATTESTATION_BASIC_SURROGATE;
        attestationTypes[1] = TAG_ATTESTATION_BASIC_FULL;
        authenticatorInfo.hasSettings(false)
                .aaid(AAID)
                .assertionScheme(ASSERTION_SCHEME)
                .authenticationAlgorithm(UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER)
                .attestationTypes(attestationTypes)
                .userVerification(USER_VERIFY_FACEPRINT)
                .keyProtection(KEY_PROTECTION_SOFTWARE)
                .matcherProtection(MATCHER_PROTECTION_SOFTWARE)
                .attachmentHint(ATTACHMENT_HINT_INTERNAL)
                .isSecondFactorOnly(false)
                .isRoamingAuthenticator(false)
                .tcDisplay(TRANSACTION_CONFIRMATION_DISPLAY_PRIVILEGED_SOFTWARE);
        authenticatorInfo.title = SIMULATOR_TYPE;

        return authenticatorInfo;
    }

    @Override
    public void showBiometricsAuth(Activity activity, final BiometricsAuthResultCallback callback) {
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
