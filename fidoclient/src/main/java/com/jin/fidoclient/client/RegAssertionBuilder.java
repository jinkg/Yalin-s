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

import android.text.TextUtils;
import android.util.Base64;

import com.jin.fidoclient.crypto.Asn1;
import com.jin.fidoclient.crypto.KeyCodec;
import com.jin.fidoclient.crypto.NamedCurve;
import com.jin.fidoclient.crypto.SHA;
import com.jin.fidoclient.tlv.Tags;
import com.jin.fidoclient.tlv.TagsEnum;
import com.jin.fidoclient.tlv.TlvAssertionParser;

import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.interfaces.ECPublicKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;


public class RegAssertionBuilder {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private KeyPair keyPair = null;
    private TlvAssertionParser parser = new TlvAssertionParser();
    private String aaid;

    public RegAssertionBuilder(KeyPair keyPair, String aaid) {
        if (keyPair == null || TextUtils.isEmpty(aaid)) {
            throw new IllegalArgumentException();
        }
        this.keyPair = keyPair;
        this.aaid = aaid;
    }

    public String getAssertions(String fcParams, byte[] keyId) throws Exception {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte[] value;
        int length;

        byteout.write(encodeInt(TagsEnum.TAG_UAFV1_REG_ASSERTION.id));
        value = getRegAssertion(fcParams, keyId);
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        String ret = Base64.encodeToString(byteout.toByteArray(), Base64.URL_SAFE);
        logger.info(" : assertion : " + ret);
        Tags tags = parser.parse(ret);
        String AAID = new String(tags.getTags().get(
                TagsEnum.TAG_AAID.id).value);
        String KeyID = new String(tags.getTags()
                .get(TagsEnum.TAG_KEYID.id).value);
        logger.info(" : KeyID : " + KeyID);
        return ret;
    }

    private byte[] getRegAssertion(String fcParams, byte[] keyId) throws Exception {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte[] value = null;
        int length = 0;

        byteout.write(encodeInt(TagsEnum.TAG_UAFV1_KRD.id));
        value = getSignedData(fcParams, keyId);
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        byte[] signedDataValue = byteout.toByteArray();

        byteout.write(encodeInt(TagsEnum.TAG_ATTESTATION_BASIC_FULL.id));
        value = getAttestationBasicFull(signedDataValue);
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        return byteout.toByteArray();
    }

    private byte[] getAttestationBasicFull(byte[] signedDataValue) throws Exception {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte[] value = null;
        int length = 0;
        byteout.write(encodeInt(TagsEnum.TAG_SIGNATURE.id));
        value = getSignature(signedDataValue);
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        byteout.write(encodeInt(TagsEnum.TAG_ATTESTATION_CERT.id));
        value = Base64.decode(AttestCert.base64DERCert, Base64.URL_SAFE);
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);
        return byteout.toByteArray();
    }

    private byte[] getSignedData(String fcParams, byte[] keyId) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte[] value = null;
        int length = 0;

        byteout.write(encodeInt(TagsEnum.TAG_AAID.id));
        value = getAAID();
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        byteout.write(encodeInt(TagsEnum.TAG_ASSERTION_INFO.id));
        //2 bytes - vendor; 1 byte Authentication Mode; 2 bytes Sig Alg; 2 bytes Pub Key Alg
        value = new byte[]{0x00, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01};
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        byteout.write(encodeInt(TagsEnum.TAG_FINAL_CHALLENGE.id));
        value = getFC(fcParams);
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        byteout.write(encodeInt(TagsEnum.TAG_KEYID.id));
        value = keyId;
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        byteout.write(encodeInt(TagsEnum.TAG_COUNTERS.id));
        value = getCounters();
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        byteout.write(encodeInt(TagsEnum.TAG_PUB_KEY.id));
        value = getPubKeyId();
        length = value.length;
        byteout.write(encodeInt(length));
        byteout.write(value);

        return byteout.toByteArray();
    }

    private byte[] getFC(String fcParams) throws NoSuchAlgorithmException {
        return SHA.sha(fcParams.getBytes(), "SHA-256");
    }

    private byte[] getPubKeyId() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        return KeyCodec.getKeyAsRawBytes((BCECPublicKey) this.keyPair.getPublic());
    }

    private byte[] getSignature(byte[] dataForSigning) throws Exception {

//		PublicKey pub = KeyCodec.getPubKey(
//				Base64.encode(this.keyPair.getPublic().getEncoded(), Base64.URL_SAFE))
//				;
//		PrivateKey priv = KeyCodec.getPrivKey(Base64
//				.encode(this.keyPair.getPrivate().getEncoded(),Base64.URL_SAFE));
//		PublicKey pub = this.keyPair.getPublic();
        PrivateKey priv =
                KeyCodec.getPrivKey(Base64.decode(AttestCert.priv, Base64.URL_SAFE));
        //this.keyPair.getPrivate();

        logger.info(" : dataForSigning : "
                + Base64.encodeToString(dataForSigning, Base64.URL_SAFE));

        BigInteger[] signatureGen = NamedCurve.signAndFromatToRS(priv,
                SHA.sha(dataForSigning, "SHA-256"));

        boolean verify = NamedCurve.verify(
                KeyCodec.getKeyAsRawBytes((ECPublicKey) KeyCodec.getPubKey(Base64.decode(AttestCert.pubCert, Base64.URL_SAFE))),
                //KeyCodec.getKeyAsRawBytes((ECPublicKey)this.keyPair.getPublic()),
                SHA.sha(dataForSigning, "SHA-256"),
                Asn1.decodeToBigIntegerArray(Asn1.getEncoded(signatureGen)));
        if (!verify) {
            throw new RuntimeException("Signatire match fail");
        }
        byte[] ret = Asn1.toRawSignatureBytes(signatureGen);
        logger.info(" : signature : " + Base64.encodeToString(ret, Base64.URL_SAFE));

        return ret;
    }

    private byte[] getCounters() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byteout.write(encodeInt(0));
        byteout.write(encodeInt(1));
        byteout.write(encodeInt(0));
        byteout.write(encodeInt(1));
        return byteout.toByteArray();
    }

    private byte[] getAAID() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte[] value = aaid.getBytes();
        byteout.write(value);
        return byteout.toByteArray();
    }

    private byte[] encodeInt(int id) {

        byte[] bytes = new byte[2];
        bytes[0] = (byte) (id & 0x00ff);
        bytes[1] = (byte) ((id & 0xff00) >> 8);
        return bytes;
    }

}
