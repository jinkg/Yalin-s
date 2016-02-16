/*
 * Copyright 2015 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jin.fidoclient.client;

import android.util.Base64;

import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.crypto.KeyCodec;
import com.jin.fidoclient.crypto.SHA;
import com.jin.fidoclient.tlv.TagsEnum;

import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public class RegAssertionBuilder {
    private KeyPair keyPair = null;
    private Simulator simulator;

    public RegAssertionBuilder(KeyPair keyPair, Simulator simulator) {
        if (keyPair == null || simulator == null) {
            throw new IllegalArgumentException();
        }
        this.keyPair = keyPair;
        this.simulator = simulator;
    }

    public String getAssertions(String fcParams, byte[] keyId) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value;
        int length;

        byteOut.write(encodeInt(TagsEnum.TAG_UAFV1_REG_ASSERTION.id));
        value = getRegAssertion(fcParams, keyId);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        return Base64.encodeToString(byteOut.toByteArray(), Base64.URL_SAFE);
    }

    private byte[] getRegAssertion(String fcParams, byte[] keyId) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value;
        int length;

        byteOut.write(encodeInt(TagsEnum.TAG_UAFV1_KRD.id));
        value = getSignedData(fcParams, keyId);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byte[] signedDataValue = byteOut.toByteArray();

        byteOut.write(encodeInt(TagsEnum.TAG_ATTESTATION_BASIC_FULL.id));
        value = getAttestationBasicFull(signedDataValue);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        return byteOut.toByteArray();
    }

    private byte[] getAttestationBasicFull(byte[] signedDataValue) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value;
        int length;
        byteOut.write(encodeInt(TagsEnum.TAG_SIGNATURE.id));
        value = getSignature(signedDataValue);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_ATTESTATION_CERT.id));
        value = Base64.decode(simulator.getCert(), Base64.URL_SAFE);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);
        return byteOut.toByteArray();
    }

    private byte[] getSignedData(String fcParams, byte[] keyId) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value;
        int length;

        byteOut.write(encodeInt(TagsEnum.TAG_AAID.id));
        value = getAAID();
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_ASSERTION_INFO.id));
        //2 bytes - vendor; 1 byte Authentication Mode; 2 bytes Sig Alg; 2 bytes Pub Key Alg
        value = new byte[]{0x00, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01};
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_FINAL_CHALLENGE.id));
        value = getFC(fcParams);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_KEYID.id));
        value = keyId;
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_COUNTERS.id));
        value = getCounters();
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_PUB_KEY.id));
        value = getPubKeyRawBytes();
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        return byteOut.toByteArray();
    }

    private byte[] getFC(String fcParams) throws NoSuchAlgorithmException {
        return SHA.sha(fcParams.getBytes(), "SHA-256");
    }

    private byte[] getPubKeyRawBytes() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        return KeyCodec.getKeyAsRawBytes((BCECPublicKey) keyPair.getPublic());
    }

    private byte[] getSignature(byte[] dataForSigning) throws Exception {
        return simulator.sign(dataForSigning);
    }

    private byte[] getCounters() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byteOut.write(encodeInt(0));
        byteOut.write(encodeInt(1));
        byteOut.write(encodeInt(0));
        byteOut.write(encodeInt(1));
        return byteOut.toByteArray();
    }

    private byte[] getAAID() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value = simulator.getAAID().getBytes();
        byteOut.write(value);
        return byteOut.toByteArray();
    }

    private byte[] encodeInt(int id) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (id & 0x00ff);
        bytes[1] = (byte) ((id & 0xff00) >> 8);
        return bytes;
    }

}
