package com.jin.fidoclient.asm.op;

import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;


public class Reg extends ASMOperator {
    private final ASMRequest request;
    private final String biometricsId;

    public Reg(String biometricsId, ASMRequest request) {
        if (!(request.args instanceof RegisterIn)) {
            throw new IllegalStateException("asm request must has a RegisterIn object");
        }
        this.biometricsId = biometricsId;
        this.request = request;
    }

    @Override
    public String handle() {
        ASMResponse<RegisterOut> response = new ASMResponse<>();
        try {
            RegisterOut registerOut = Simulator.register(biometricsId, (RegisterIn) request.args, request.authenticatorIndex);
            if (registerOut != null) {
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
}
