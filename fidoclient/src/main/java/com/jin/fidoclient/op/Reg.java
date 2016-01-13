package com.jin.fidoclient.op;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Base64;

import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;
import com.jin.fidoclient.msg.AuthenticatorRegistrationAssertion;
import com.jin.fidoclient.msg.ChannelBinding;
import com.jin.fidoclient.msg.FinalChallengeParams;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.msg.RegistrationRequest;
import com.jin.fidoclient.msg.RegistrationResponse;
import com.jin.fidoclient.utils.Utils;


public class Reg extends ClientOperator {
    private final RegistrationRequest registrationRequest;
    private final Activity activity;
    private final ChannelBinding channelBinding;

    private String finalChallenge;

    public Reg(Activity activity, String message, String channelBinding) {
        if (activity == null || TextUtils.isEmpty(message)) {
            throw new IllegalArgumentException();
        }
        this.activity = activity;
        try {
            this.registrationRequest = getRegistrationRequest(message);
        } catch (Exception e) {
            throw new IllegalStateException("register message error");
        }
        ChannelBinding cb;
        try {
            cb = gson.fromJson(channelBinding, ChannelBinding.class);
        } catch (Exception e) {
            cb = null;
        }
        this.channelBinding = cb;
    }

    @Override
    public void handle() {
        String facetId = Utils.getFacetId(activity);
        if (TextUtils.isEmpty(registrationRequest.header.appID)) {
            registrationRequest.header.appID = facetId;
        }
        FinalChallengeParams fcParams = new FinalChallengeParams();
        fcParams.appID = registrationRequest.header.appID;
        fcParams.challenge = registrationRequest.challenge;
        fcParams.facetID = facetId;
        fcParams.channelBinding = channelBinding;

        finalChallenge = Base64.encodeToString(gson.toJson(fcParams).getBytes(), Base64.URL_SAFE);
        RegisterIn registerIn = new RegisterIn(registrationRequest.header.appID, registrationRequest.username, finalChallenge, 0);

        ASMRequest<RegisterIn> asmRequest = new ASMRequest<>();
        asmRequest.requestType = Request.Register;
        asmRequest.args = registerIn;
        asmRequest.asmVersion = registrationRequest.header.upv;
        ASMApi.doOperaion(activity, REQUEST_ASM_OPERATION, gson.toJson(asmRequest));
    }

    @Override
    public String assemble(String result) {
        ASMResponse asmResponse = ASMResponse.fromJson(result, RegisterOut.class);
        if (asmResponse.responseData instanceof RegisterOut) {
            RegisterOut registerOut = (RegisterOut) asmResponse.responseData;
            RegistrationResponse[] responses = new RegistrationResponse[1];
            responses[0] = wrapResponse(registerOut);
            return gson.toJson(responses);
        } else {
            throw new IllegalStateException("reg assemble data must be a RegisterOut object");
        }
    }

    private RegistrationResponse wrapResponse(RegisterOut registerOut) {
        RegistrationResponse response = new RegistrationResponse();
        response.header = new OperationHeader();
        response.header.serverData = registrationRequest.header.serverData;
        response.header.appID = registrationRequest.header.appID;
        response.header.op = registrationRequest.header.op;
        response.header.upv = registrationRequest.header.upv;

        response.fcParams = finalChallenge;

        response.assertions = new AuthenticatorRegistrationAssertion[1];
        response.assertions[0] = new AuthenticatorRegistrationAssertion();
        response.assertions[0].assertion = registerOut.assertion;
        response.assertions[0].assertionScheme = registerOut.assertionScheme;
        return response;
    }

    private RegistrationRequest getRegistrationRequest(String uafMsg) {
        RegistrationRequest[] requests = gson.fromJson(uafMsg, RegistrationRequest[].class);
        return requests[0];
    }
}
