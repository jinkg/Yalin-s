package com.jin.fidoclient.asm.op;


import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.GetInfoOut;
import com.jin.fidoclient.utils.StatLog;

/**
 * Created by YaLin on 2016/1/18.
 */
public class ASMGetInfo extends ASMOperator {
    private static final String TAG = ASMGetInfo.class.getSimpleName();

    private final HandleResultCallback callback;

    public ASMGetInfo(HandleResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm getInfo");
        ASMResponse<GetInfoOut> response = new ASMResponse<>();

        GetInfoOut getInfoOut = new GetInfoOut();
        getInfoOut.Authenticators = getAvailableAuthenticator();
        response.responseData = getInfoOut;
        response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
        String getInfoResult = gson.toJson(response);
        StatLog.printLog(TAG, "get info result:" + getInfoResult);
        if (callback != null) {
            callback.onHandleResult(getInfoResult);
        }
    }

    private AuthenticatorInfo[] getAvailableAuthenticator() {
        return Simulator.discover();
    }
}
