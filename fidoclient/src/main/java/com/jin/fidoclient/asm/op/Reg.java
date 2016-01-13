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

package com.jin.fidoclient.asm.op;

import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.asm.db.UAFDBHelper;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;
import com.jin.fidoclient.client.RegistrationRequestProcessor;
import com.jin.fidoclient.crypto.BCrypt;
import com.jin.fidoclient.crypto.KeyCodec;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.logging.Logger;


public class Reg extends ASMOperator {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final ASMRequest request;
    private final int touchId;

    public Reg(int touchId, ASMRequest request) {
        if (!(request.args instanceof RegisterIn)) {
            throw new IllegalStateException("asm request must has a RegisterIn object");
        }
        this.touchId = touchId;
        this.request = request;
    }

    @Override
    public String handle() {
        logger.info("  [UAF][1]Reg  ");
        try {
            RegRecord regRecord = new RegRecord();
            regRecord.touchId = touchId;
            regRecord.keyId = getKeyId();
            RegisterIn registerIn = (RegisterIn) request.args;
            regRecord.username = registerIn.username;
            regRecord.appId = registerIn.appID;
            ASMResponse<RegisterOut> response = new ASMResponse<>();
            KeyPair keyPair = KeyCodec.getKeyPair();
            byte[] keyIdBytes = regRecord.keyId.getBytes();
            logger.info("  [UAF][2]Reg - KeyPair generated" + keyPair);

            RegistrationRequestProcessor p = new RegistrationRequestProcessor();
            RegisterOut registerOut = p.processRequest(registerIn, keyPair, keyIdBytes);
            logger.info("  [UAF][4]Reg - Reg Response Formed  ");
            logger.info(registerOut.assertion);
            logger.info("  [UAF][6]Reg - done  ");

            regRecord.userPrivateKey = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.URL_SAFE);
            regRecord.userPublicKey = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.URL_SAFE);

            UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.addRecord(db, regRecord);
            logger.info("  [UAF][7]Reg - keys stored  ");

            response.responseData = registerOut;
            response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            return gson.toJson(response);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getKeyId() {
        String keyId = "ebay-test-key-" + Base64.encodeToString(BCrypt.gensalt().getBytes(), Base64.NO_WRAP);
        keyId = Base64.encodeToString(keyId.getBytes(), Base64.URL_SAFE);

        return keyId;
    }
}
