package com.jin.fidoclient.asm.authenticator;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
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
import com.jin.fidoclient.asm.ui.FingerprintAuthenticationDialogFragment;
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
public class SimulatorA extends Simulator implements FingerprintAuthenticationDialogFragment.FingerprintAuthenticationResultCallback {
    private static final String CERT_BASE64 = "MIIBrjCCAVOgAwIBAgIEPpN8yjAMBggqhkjOPQQDAgUAMEwxCjAIBgNVBAYTATUxCjAIBgNVBAgTATQxCjAIBgNVBAcTATMxCjAIBgNVBAoTATIxCjAIBgNVBAsTATExDjAMBgNVBAMTBXlhbGluMB4XDTE2MDIxNjA5MTIyOFoXDTE2MDUxNjA5MTIyOFowTDEKMAgGA1UEBhMBNTEKMAgGA1UECBMBNDEKMAgGA1UEBxMBMzEKMAgGA1UEChMBMjEKMAgGA1UECxMBMTEOMAwGA1UEAxMFeWFsaW4wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAS8JQ39Hf7XFP3AuWxZRJWU2MpSQkmEU6Ve8sstr9_fKhMGySHpm69p_bi40kWGKTJ7P-23VSYN0VudpasrwaFtoyEwHzAdBgNVHQ4EFgQUmac9DuNAOcOTg9PbO1ljHPLSouowDAYIKoZIzj0EAwIFAANHADBEAiB1hDzzICTSWiRhl3M2ibC3K11S3ocyViQDp8tHJYpeOwIgBCUnNDGEDrsXsRUMTd3jb5lR7S6sI2U_R4EL5IJUyG4=";
    private static final String PUBLIC_KEY_BASE64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEvCUN_R3-1xT9wLlsWUSVlNjKUkJJhFOlXvLLLa_f3yoTBskh6Zuvaf24uNJFhikyez_tt1UmDdFbnaWrK8GhbQ==";
    private static final String PRIVATE_KEY_BASE64 = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBYqVlbx5v6mMxePQcjf9De9VRnzFxirWhGvpwSKau1Ag==";

    private static final String TAG = SimulatorA.class.getSimpleName();

    //    public static final String AAID = "EBA0#0001";
    public static final String AAID = "4e4e#4005";
    private static final String SIMULATOR_TYPE = "fingerprint";
    private static final String ASSERTION_SCHEME = "UAFV1TLV";

    private static SimulatorA sInstance;
    private BiometricsAuthResultCallback mCallback;

    private SimulatorA() {
    }

    public static SimulatorA getInstance() {
        if (sInstance == null) {
            sInstance = new SimulatorA();
        }
        return sInstance;
    }

    @Override
    public RegisterOut register(@NonNull String biometricsId, @NonNull RegisterIn registerIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        if (TextUtils.isEmpty(biometricsId)) {
            throw new IllegalArgumentException();
        }
        StatLog.printLog(TAG, "simulatorA register biometricsId: " + biometricsId + " registerIn: " + new Gson().toJson(registerIn));

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
            StatLog.printLog(TAG, "simulatorA register success registerOut: " + new Gson().toJson(registerOut));
            return registerOut;
        }

        return null;
    }

    @Override
    public AuthenticateOut authenticate(@NonNull String biometricsId, @NonNull AuthenticateIn authenticateIn) {
        StatLog.printLog(TAG, "simulatorA authenticate biometricsId: " + biometricsId + " authenticateIn: " + new Gson().toJson(authenticateIn));
        UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        RegRecord regRecord = dbHelper.getUserRecord(db, biometricsId);
        if (regRecord == null) {
            throw new IllegalStateException("you not have reg uaf");
        }

        AuthenticationRequestProcessor p = new AuthenticationRequestProcessor();
        AuthenticateOut authenticateOut = p.processRequest(regRecord, authenticateIn, ASSERTION_SCHEME);
        StatLog.printLog(TAG, "simulatorA authenticate success authenticateOut: " + new Gson().toJson(authenticateOut));
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
        String keyId = "yalin-test1-key-" + Base64.encodeToString(BCrypt.gensalt().getBytes(), Base64.NO_WRAP);
        keyId = Base64.encodeToString(keyId.getBytes(), Base64.URL_SAFE);

        return keyId;
    }

    @Override
    public String getPrivateKey() {
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
        extendIds[0] = "abc";

        int[] attestationTypes = new int[2];
        attestationTypes[0] = TAG_ATTESTATION_BASIC_SURROGATE;
        attestationTypes[1] = TAG_ATTESTATION_BASIC_FULL;
        authenticatorInfo.hasSettings(false)
                .aaid(AAID)
                .assertionScheme(ASSERTION_SCHEME)
                .asmVersions(versions)
                .supportedExtensionIDs(extendIds)
                .authenticationAlgorithm(UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_RAW)
                .attestationTypes(attestationTypes)
                .userVerification(USER_VERIFY_FINGERPRINT)
                .keyProtection(KEY_PROTECTION_SOFTWARE)
                .matcherProtection(MATCHER_PROTECTION_SOFTWARE)
                .attachmentHint(ATTACHMENT_HINT_INTERNAL)
                .isSecondFactorOnly(false)
                .isRoamingAuthenticator(false)
                .tcDisplay(TRANSACTION_CONFIRMATION_DISPLAY_ANY);

        authenticatorInfo.title = SIMULATOR_TYPE;
        authenticatorInfo.iconRes = R.drawable.ic_fp_40px;

        return authenticatorInfo;
    }


    @Override
    public void showBiometricsAuth(@NonNull Activity activity, BiometricsAuthResultCallback callback) {
        mCallback = callback;
        FingerprintAuthenticationDialogFragment fragment = new FingerprintAuthenticationDialogFragment(this);
        fragment.setStage(
                FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
        fragment.show(activity.getFragmentManager(), activity.getClass().getSimpleName());
    }

    @Override
    public void onAuthenticate(String fingerId) {
        if (mCallback != null) {
            mCallback.onAuthSuccess(this, fingerId);
        }
    }

    @Override
    public void authenticateFailed() {
        if (mCallback != null) {
            mCallback.onAuthFailed(this);
        }
    }
}
