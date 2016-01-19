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
public class GetInfo extends ASMOperator {
    private static final String TAG = GetInfo.class.getSimpleName();

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm getInfo");
        ASMResponse<GetInfoOut> response = new ASMResponse<>();

        GetInfoOut getInfoOut = new GetInfoOut();
        getInfoOut.Authenticators = getAvailableAuthenticator();
        response.responseData = getInfoOut;
        response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
        gson.toJson(response);
    }

    private AuthenticatorInfo[] getAvailableAuthenticator() {
        return Simulator.discover();
    }
}
