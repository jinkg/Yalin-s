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


import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.crypto.Asn1;
import com.jin.fidoclient.crypto.BCrypt;
import com.jin.fidoclient.crypto.KeyCodec;
import com.jin.fidoclient.crypto.NamedCurve;
import com.jin.fidoclient.crypto.SHA;
import com.jin.fidoclient.tlv.TagsEnum;

import org.spongycastle.jce.interfaces.ECPublicKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

public class AuthAssertionBuilder {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public String getAssertions(RegRecord regRecord, String fcParams) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value;
        int length;

        byteOut.write(encodeInt(TagsEnum.TAG_UAFV1_AUTH_ASSERTION.id));
        value = getAuthAssertion(regRecord, fcParams);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        String ret = Base64.encodeToString(byteOut.toByteArray(), Base64.NO_PADDING);
        logger.info(" : assertion : " + ret);
        return ret;
    }

    private byte[] getAuthAssertion(RegRecord regRecord, String fcParams) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value;
        int length;

        byteOut.write(encodeInt(TagsEnum.TAG_UAFV1_SIGNED_DATA.id));
        value = getSignedData(regRecord, fcParams);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byte[] signedDataValue = byteOut.toByteArray();

        byteOut.write(encodeInt(TagsEnum.TAG_SIGNATURE.id));
        value = getSignature(regRecord, signedDataValue);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        return byteOut.toByteArray();
    }

    private byte[] getSignedData(RegRecord regRecord, String fcParams) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value;
        int length;

        byteOut.write(encodeInt(TagsEnum.TAG_AAID.id));
        value = getAAID();
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_ASSERTION_INFO.id));
        //2 bytes - vendor; 1 byte Authentication Mode; 2 bytes Sig Alg
        value = new byte[]{0x00, 0x00, 0x01, 0x01, 0x00};
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_AUTHENTICATOR_NONCE.id));
        value = SHA.sha256(BCrypt.gensalt()).getBytes();
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_FINAL_CHALLENGE.id));
        value = getFC(fcParams);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_TRANSACTION_CONTENT_HASH.id));
        length = 0;
        byteOut.write(encodeInt(length));

        byteOut.write(encodeInt(TagsEnum.TAG_KEYID.id));
        value = getKeyId(regRecord);
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        byteOut.write(encodeInt(TagsEnum.TAG_COUNTERS.id));
        value = getCounters();
        length = value.length;
        byteOut.write(encodeInt(length));
        byteOut.write(value);

        return byteOut.toByteArray();
    }

    private byte[] getFC(String fcParams) throws NoSuchAlgorithmException {
        return SHA.sha(fcParams.getBytes(), "SHA-256");
    }

    private byte[] getCounters() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byteOut.write(encodeInt(0));
        byteOut.write(encodeInt(1));
        return byteOut.toByteArray();
    }

    private byte[] getSignature(RegRecord regRecord, byte[] dataForSigning) throws Exception {

        PublicKey pub = KeyCodec.getPubKey(Base64.decode(regRecord.userPublicKey, Base64.URL_SAFE));
        PrivateKey pri = KeyCodec.getPrivKey(Base64.decode(regRecord.userPrivateKey, Base64.URL_SAFE));

        BigInteger[] signatureGen = NamedCurve.signAndFromatToRS(pri,
                SHA.sha(dataForSigning, "SHA-256"));

        boolean verify = NamedCurve.verify(
                KeyCodec.getKeyAsRawBytes((ECPublicKey) pub),
                SHA.sha(dataForSigning, "SHA-256"),
                Asn1.decodeToBigIntegerArray(Asn1.getEncoded(signatureGen)));
        if (!verify) {
            throw new RuntimeException("Signature match fail");
        }

        return Asn1.toRawSignatureBytes(signatureGen);
    }

    private byte[] getKeyId(RegRecord regRecord) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        String keyId = regRecord.keyId;
        byte[] value = keyId.getBytes();
        byteOut.write(value);
        return byteOut.toByteArray();
    }

    private byte[] getAAID() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] value = "EBA0#0001".getBytes();
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

