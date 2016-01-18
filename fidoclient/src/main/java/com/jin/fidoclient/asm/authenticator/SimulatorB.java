package com.jin.fidoclient.asm.authenticator;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.asm.db.UAFDBHelper;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;
import com.jin.fidoclient.client.RegistrationRequestProcessor;
import com.jin.fidoclient.crypto.BCrypt;
import com.jin.fidoclient.crypto.KeyCodec;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by YaLin on 2016/1/18.
 */
public class SimulatorB extends Simulator {
    public static final String AAID = "EBA0#0002";
    private static final String SIMULATOR_TYPE = "face";

    private static SimulatorB sInstance;

    private SimulatorB(){}

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

        UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (!dbHelper.registered(db, biometricsId)) {
            RegRecord regRecord = new RegRecord();
            regRecord.type = SIMULATOR_TYPE;
            regRecord.biometricsId = biometricsId;
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

            return registerOut;
        }

        return null;
    }

    @Override
    public AuthenticateOut authenticate(String biometricsId, AuthenticateIn authenticateIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        return null;
    }

    @Override
    protected String getKeyId() {
        String keyId = "yalin-test1-key-" + Base64.encodeToString(BCrypt.gensalt().getBytes(), Base64.NO_WRAP);
        keyId = Base64.encodeToString(keyId.getBytes(), Base64.URL_SAFE);

        return keyId;
    }

    @Override
    protected AuthenticatorInfo getInfo() {
        AuthenticatorInfo authenticatorInfo = new AuthenticatorInfo();

        int[] attestationTypes = new int[2];
        attestationTypes[0] = TAG_ATTESTATION_CERT;
        attestationTypes[1] = TAG_ATTESTATION_BASIC_FULL;
        authenticatorInfo.hasSettings(false)
                .aaid(AAID)
                .assertionScheme("UAFV1TLV")
                .authenticationAlgorithm(UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER)
                .attestationTypes(attestationTypes)
                .userVerification(USER_VERIFY_FACEPRINT)
                .keyProtection(KEY_PROTECTION_SOFTWARE)
                .matcherProtection(MATCHER_PROTECTION_SOFTWARE)
                .attachmentHint(ATTACHMENT_HINT_INTERNAL)
                .isSecondFactorOnly(false)
                .isRoamingAuthenticator(false)
                .tcDisplay(TRANSACTION_CONFIRMATION_DISPLAY_PRIVILEGED_SOFTWARE);

        return authenticatorInfo;
    }

}
