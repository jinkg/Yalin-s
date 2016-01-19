package com.jin.fidoclient.op;

import android.text.TextUtils;
import android.util.Base64;

import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;
import com.jin.fidoclient.msg.AuthenticatorRegistrationAssertion;
import com.jin.fidoclient.msg.ChannelBinding;
import com.jin.fidoclient.msg.FinalChallengeParams;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.msg.RegistrationRequest;
import com.jin.fidoclient.msg.RegistrationResponse;
import com.jin.fidoclient.ui.AuthenticatorAdapter;
import com.jin.fidoclient.ui.FIDOOperationActivity;
import com.jin.fidoclient.utils.StatLog;
import com.jin.fidoclient.utils.Utils;

import java.util.List;


public class Reg extends ASMMessageHandler implements AuthenticatorAdapter.OnAuthenticatorClickCallback {
    private static final String TAG = Reg.class.getSimpleName();
    private final RegistrationRequest registrationRequest;
    private final FIDOOperationActivity activity;
    private final ChannelBinding channelBinding;
    private final HandleResultCallback callback;

    private String finalChallenge;


    public Reg(FIDOOperationActivity activity, String message, String channelBinding, HandleResultCallback callback) {
        if (TextUtils.isEmpty(message)) {
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
        this.callback = callback;
    }

    @Override
    public void handle() {
        List<AuthenticatorInfo> authenticatorInfos = parsePolicy(registrationRequest.policy);
        StatLog.printLog(TAG, "client reg parse policy: " + gson.toJson(authenticatorInfos));
        if (authenticatorInfos == null || authenticatorInfos.isEmpty()) {
            return;
        }
        activity.showAuthenticator(authenticatorInfos, this);


    }

    @Override
    public String parseAsmResponse(String asmResponseMsg) throws ASMException {
        ASMResponse asmResponse = ASMResponse.fromJson(asmResponseMsg, RegisterOut.class);
        if (asmResponse.statusCode != StatusCode.UAF_ASM_STATUS_OK) {
            throw new ASMException(asmResponse.statusCode);
        }
        if (asmResponse.responseData instanceof RegisterOut) {
            RegisterOut registerOut = (RegisterOut) asmResponse.responseData;
            RegistrationResponse[] responses = new RegistrationResponse[1];
            responses[0] = wrapResponse(registerOut);
            return gson.toJson(responses);
        } else {
            throw new ASMException(StatusCode.UAF_ASM_STATUS_ERROR);
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

    @Override
    public void onAuthenticatorClick(AuthenticatorInfo info) {
        String facetId = Utils.getFacetId(activity.getApplication());
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
        asmRequest.authenticatorIndex = info.authenticatorIndex;
        if (callback != null) {
            callback.onResult(gson.toJson(asmRequest));
        }
    }
}
