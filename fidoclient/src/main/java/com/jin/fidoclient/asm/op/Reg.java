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

import java.security.KeyPair;
import java.util.logging.Logger;


public class Reg extends ASMOperator {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final ASMRequest request;
    private final String touchId;

    public Reg(String touchId, ASMRequest request) {
        if (!(request.args instanceof RegisterIn)) {
            throw new IllegalStateException("asm request must has a RegisterIn object");
        }
        this.touchId = touchId;
        this.request = request;
    }

    @Override
    public String handle() {
        ASMResponse<RegisterOut> response = new ASMResponse<>();
        logger.info("  [UAF][1]Reg  ");
        try {
            UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            if (!dbHelper.registered(db, touchId)) {
                RegRecord regRecord = new RegRecord();
                regRecord.type = "touchId";
                regRecord.touchId = touchId;
                regRecord.keyId = getKeyId();
                RegisterIn registerIn = (RegisterIn) request.args;
                regRecord.username = registerIn.username;
                regRecord.appId = registerIn.appID;

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

                dbHelper.addRecord(db, regRecord);
                logger.info("  [UAF][7]Reg - keys stored  ");

                response.responseData = registerOut;
                response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            } else {
                response.statusCode = StatusCode.UAF_ASM_STATUS_ERROR;
            }
        } catch (Exception e) {
            response.statusCode = StatusCode.UAF_ASM_STATUS_ERROR;
        }
        return gson.toJson(response);
    }

    private String getKeyId() {
        String keyId = "yalin-test-key-" + Base64.encodeToString(BCrypt.gensalt().getBytes(), Base64.NO_WRAP);
        keyId = Base64.encodeToString(keyId.getBytes(), Base64.URL_SAFE);

        return keyId;
    }
}
