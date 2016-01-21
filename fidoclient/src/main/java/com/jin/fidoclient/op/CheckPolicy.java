package com.jin.fidoclient.op;



import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.msg.Policy;
import com.jin.fidoclient.msg.RegistrationRequest;
import com.jin.fidoclient.ui.UAFClientActivity;

/**
 * Created by YaLin on 2016/1/18.
 */
public class CheckPolicy extends ASMMessageHandler {
    private Policy policy;

    public CheckPolicy(UAFClientActivity activity, String message) {
        super(activity);
        try {
            RegistrationRequest registrationRequest = getRegistrationRequest(message);
            policy = registrationRequest.policy;
        } catch (Exception e) {
            throw new IllegalStateException("register message error");
        }
    }

    @Override
    public boolean startTraffic() {
        ASMRequest asmRequest = new ASMRequest();
        asmRequest.requestType = Request.GetInfo;
        gson.toJson(asmRequest);
        return true;
    }

    @Override
    public boolean traffic(String asmResponseMsg) throws ASMException {
        return false;
    }

    private RegistrationRequest getRegistrationRequest(String uafMsg) {
        RegistrationRequest[] requests = gson.fromJson(uafMsg, RegistrationRequest[].class);
        return requests[0];
    }
}
