package com.jin.fidoclient.op;


import android.content.Context;

import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.msg.Policy;
import com.jin.fidoclient.msg.RegistrationRequest;

/**
 * Created by YaLin on 2016/1/18.
 */
public class CheckPolicy extends ASMMessageHandler {
    private Context context;
    private Policy policy;

    public CheckPolicy(Context context, String message) {
        this.context = context;
        try {
            RegistrationRequest registrationRequest = getRegistrationRequest(message);
            policy = registrationRequest.policy;
        } catch (Exception e) {
            throw new IllegalStateException("register message error");
        }
    }

    @Override
    public void handle() {
        ASMRequest asmRequest = new ASMRequest();
        asmRequest.requestType = Request.GetInfo;
        gson.toJson(asmRequest);
    }

    @Override
    public String parseAsmResponse(String asmResponseMsg) throws ASMException {
        return null;
    }

    private RegistrationRequest getRegistrationRequest(String uafMsg) {
        RegistrationRequest[] requests = gson.fromJson(uafMsg, RegistrationRequest[].class);
        return requests[0];
    }
}
