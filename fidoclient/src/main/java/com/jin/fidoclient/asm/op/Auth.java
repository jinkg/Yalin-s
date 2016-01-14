package com.jin.fidoclient.asm.op;

import android.database.sqlite.SQLiteDatabase;

import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.asm.db.UAFDBHelper;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;
import com.jin.fidoclient.client.AuthenticationRequestProcessor;

import java.util.logging.Logger;

public class Auth extends ASMOperator {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private final ASMRequest request;
    private final String touchId;

    public Auth(String touchId, ASMRequest request) {
        if (!(request.args instanceof AuthenticateIn)) {
            throw new IllegalStateException("asm request must has a AuthenticateIn object");
        }
        this.touchId = touchId;
        this.request = request;
    }

    @Override
    public String handle() {
        logger.info("  [UAF][1]Auth  ");
        ASMResponse<AuthenticateOut> response = new ASMResponse<>();
        try {
            UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            RegRecord regRecord = dbHelper.getUserRecord(db, touchId);
            if (regRecord == null) {
                throw new IllegalStateException("you not have reg uaf");
            }

            logger.info("  [UAF][2]Auth - pri key retrieved");
            AuthenticationRequestProcessor p = new AuthenticationRequestProcessor();
            AuthenticateOut authenticateOut = p.processRequest(regRecord, (AuthenticateIn) request.args);
            logger.info("  [UAF][4]Auth - Auth Response Formed  ");
            logger.info(authenticateOut.assertion);
            logger.info("  [UAF][6]Auth - done  ");
            response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            response.responseData = authenticateOut;

        } catch (Exception e) {
            response.statusCode = StatusCode.UAF_ASM_STATUS_ACCESS_DENIED;
        }
        return gson.toJson(response);
    }
}
