package com.jin.fidoclient.asm.op;


import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.GetInfoOut;

/**
 * Created by YaLin on 2016/1/18.
 */
public class GetInfo extends ASMOperator {
    @Override
    public String handle() {
        ASMResponse<GetInfoOut> response = new ASMResponse<>();

        GetInfoOut getInfoOut = new GetInfoOut();
        getInfoOut.Authenticators = getAvailableAuthenticator();
        response.responseData = getInfoOut;
        response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
        return gson.toJson(response);
    }

    private AuthenticatorInfo[] getAvailableAuthenticator() {
        return Simulator.discover();
    }
}
