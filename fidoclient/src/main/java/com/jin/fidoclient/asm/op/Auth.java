package com.jin.fidoclient.asm.op;

import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;

public class Auth extends ASMOperator {

    private final ASMRequest request;
    private final String biometricsId;

    public Auth(String biometricsId, ASMRequest request) {
        if (!(request.args instanceof AuthenticateIn)) {
            throw new IllegalStateException("asm request must has a AuthenticateIn object");
        }
        this.biometricsId = biometricsId;
        this.request = request;
    }

    @Override
    public String handle() {
        ASMResponse<AuthenticateOut> response = new ASMResponse<>();
        try {
            AuthenticateOut authenticateOut = Simulator.authenticate(biometricsId, (AuthenticateIn) request.args, request.authenticatorIndex);
            response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            response.responseData = authenticateOut;
        } catch (Exception e) {
            response.statusCode = StatusCode.UAF_ASM_STATUS_ACCESS_DENIED;
        }
        return gson.toJson(response);
    }
}
