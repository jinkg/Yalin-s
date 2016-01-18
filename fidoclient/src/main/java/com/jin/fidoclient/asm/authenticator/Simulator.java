package com.jin.fidoclient.asm.authenticator;

import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by YaLin on 2016/1/18.
 */
public abstract class Simulator {
    protected static final int UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_RAW = 0x01;
    protected static final int UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER = 0x02;
    protected static final int UAF_ALG_SIGN_RSASSA_PSS_SHA256_RAW = 0x03;
    protected static final int UAF_ALG_SIGN_RSASSA_PSS_SHA256_DER = 0x04;
    protected static final int UAF_ALG_SIGN_SECP256K1_ECDSA_SHA256_RAW = 0x05;
    protected static final int UAF_ALG_SIGN_SECP256K1_ECDSA_SHA256_DER = 0x06;

    protected static final int TAG_ATTESTATION_CERT = 0x2E05;
    protected static final int TAG_ATTESTATION_BASIC_FULL = 0x3E07;
    protected static final int TAG_ATTESTATION_BASIC_SURROGATE = 0x3E08;

    protected static final int USER_VERIFY_PRESENCE = 0x01;
    protected static final int USER_VERIFY_FINGERPRINT = 0x02;
    protected static final int USER_VERIFY_PASSCODE = 0x04;
    protected static final int USER_VERIFY_VOICEPRINT = 0x08;
    protected static final int USER_VERIFY_FACEPRINT = 0x10;
    protected static final int USER_VERIFY_LOCATION = 0x20;
    protected static final int USER_VERIFY_EYEPRINT = 0x40;
    protected static final int USER_VERIFY_PATTERN = 0x80;
    protected static final int USER_VERIFY_HANDPRINT = 0x100;
    protected static final int USER_VERIFY_NONE = 0x200;
    protected static final int USER_VERIFY_ALL = 0x400;

    protected static final int KEY_PROTECTION_SOFTWARE = 0x01;
    protected static final int KEY_PROTECTION_HARDWARE = 0x02;
    protected static final int KEY_PROTECTION_TEE = 0x04;
    protected static final int KEY_PROTECTION_SECURE_ELEMENT = 0x08;
    protected static final int KEY_PROTECTION_REMOTE_HANDLE = 0x10;

    protected static final int MATCHER_PROTECTION_SOFTWARE = 0x01;
    protected static final int MATCHER_PROTECTION_TEE = 0x02;
    protected static final int MATCHER_PROTECTION_ON_CHIP = 0x04;

    protected static final int ATTACHMENT_HINT_INTERNAL = 0x01;
    protected static final int ATTACHMENT_HINT_EXTERNAL = 0x02;
    protected static final int ATTACHMENT_HINT_WIRED = 0x04;
    protected static final int ATTACHMENT_HINT_WIRELESS = 0x08;
    protected static final int ATTACHMENT_HINT_NFC = 0x10;
    protected static final int ATTACHMENT_HINT_BLUETOOTH = 0x20;
    protected static final int ATTACHMENT_HINT_NETWORK = 0x40;
    protected static final int ATTACHMENT_HINT_READY = 0x80;
    protected static final int ATTACHMENT_HINT_WIFI_DIRECT = 0x100;

    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_ANY = 0x01;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_PRIVILEGED_SOFTWARE = 0x02;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_TEE = 0x04;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_HARDWARE = 0x08;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_REMOTE = 0x10;

    public Simulator() {

    }

    public static RegisterOut register(String biometricsId, RegisterIn registerIn, int authenticatorIndex) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        Simulator simulator = SimulatorA.getInstance();
        return simulator.register(biometricsId, registerIn);
    }

    public static AuthenticateOut authenticate(String biometricsId, AuthenticateIn authenticateIn, int authenticatorIndex) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        Simulator simulator = SimulatorA.getInstance();
        return simulator.authenticate(biometricsId, authenticateIn);
    }

    public abstract RegisterOut register(String biometricsId, RegisterIn registerIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException;

    public abstract AuthenticateOut authenticate(String biometricsId, AuthenticateIn authenticateIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException;

    protected abstract String getKeyId();

    protected abstract AuthenticatorInfo getInfo();

    public static AuthenticatorInfo[] discover() {
        AuthenticatorInfo[] authenticatorInfos = new AuthenticatorInfo[2];
        authenticatorInfos[0] = SimulatorA.getInstance().getInfo()
                .authenticatorIndex(1)
                .isUserEnrolled(false);
        authenticatorInfos[1] = SimulatorB.getInstance().getInfo()
                .authenticatorIndex(2)
                .isUserEnrolled(false);
        return authenticatorInfos;
    }
}
