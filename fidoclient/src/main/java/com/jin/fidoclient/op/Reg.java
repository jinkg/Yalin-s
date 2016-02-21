package com.jin.fidoclient.op;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;

import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;
import com.jin.fidoclient.constants.Constants;
import com.jin.fidoclient.msg.AuthenticatorRegistrationAssertion;
import com.jin.fidoclient.msg.ChannelBinding;
import com.jin.fidoclient.msg.FinalChallengeParams;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.msg.Policy;
import com.jin.fidoclient.msg.RegistrationRequest;
import com.jin.fidoclient.msg.RegistrationResponse;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.AuthenticatorAdapter;
import com.jin.fidoclient.ui.UAFClientActivity;
import com.jin.fidoclient.utils.StatLog;
import com.jin.fidoclient.utils.Utils;


public class Reg extends ASMMessageHandler implements AuthenticatorAdapter.OnAuthenticatorClickCallback {
    private static final String TAG = Reg.class.getSimpleName();
    private final RegistrationRequest registrationRequest;
    private final ChannelBinding channelBinding;

    private String finalChallenge;

    public Reg(UAFClientActivity activity, String message, String channelBinding) {
        super(activity);
        if (TextUtils.isEmpty(message)) {
            throw new IllegalArgumentException();
        }
        this.registrationRequest = getRegistrationRequest(message);
        ChannelBinding cb;
        try {
            cb = gson.fromJson(channelBinding, ChannelBinding.class);
        } catch (Exception e) {
            cb = null;
        }
        this.channelBinding = cb;
        updateState(Traffic.OpStat.PREPARE);
    }

    @Override
    public boolean startTraffic() {
        if (registrationRequest == null || activity == null) {
            return false;
        }
        switch (mCurrentState) {
            case PREPARE:
                String getInfoMessage = getInfoRequest(new Version(1, 0));
                ASMApi.doOperation(activity, REQUEST_ASM_OPERATION, getInfoMessage);
                updateState(Traffic.OpStat.GET_INFO_PENDING);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean traffic(String asmResponseMsg) throws ASMException {
        StatLog.printLog(TAG, "asm response: " + asmResponseMsg);
        switch (mCurrentState) {
            case GET_INFO_PENDING:
                if (!handleGetInfo(asmResponseMsg, this)) {
                    return false;
                }
                updateState(Traffic.OpStat.REG_PENDING);
                break;
            case REG_PENDING:
                handleRegOut(asmResponseMsg);
                updateState(Traffic.OpStat.PREPARE);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public Policy getPolicy() {
        return registrationRequest.policy;
    }

    private void handleRegOut(String msg) throws ASMException {
        ASMResponse asmResponse = ASMResponse.fromJson(msg, RegisterOut.class);
        if (asmResponse.statusCode != StatusCode.UAF_ASM_STATUS_OK) {
            throw new ASMException(asmResponse.statusCode);
        }
        String response;
        if (asmResponse.responseData instanceof RegisterOut) {
            RegisterOut registerOut = (RegisterOut) asmResponse.responseData;
            RegistrationResponse[] responses = new RegistrationResponse[1];
            responses[0] = wrapResponse(registerOut);
            response = gson.toJson(responses);
        } else {
            throw new ASMException(StatusCode.UAF_ASM_STATUS_ERROR);
        }
        Intent intent = UAFIntent.getUAFOperationResultIntent(activity.getComponentName().flattenToString(), new UAFMessage(response).toJson());
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
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
        RegistrationRequest[] requests;
        try {
            requests = gson.fromJson(uafMsg, RegistrationRequest[].class);
        } catch (Exception e) {
            requests = null;
        }
        RegistrationRequest result;
        if (requests == null || requests.length == 0) {
            return null;
        }
        RegistrationRequest temp = null;
        for (RegistrationRequest request : requests) {
            if (request.header == null || request.header.upv == null) {
                continue;
            }
            if (request.header.upv.equals(Version.getCurrentSupport())) {
                if (temp == null) {
                    temp = request;
                } else {
                    temp = null;
                    break;
                }
            }
        }
        result = temp;
        if (!checkRequest(result)) {
            result = null;
        }
        return result;
    }

    @Override
    protected boolean checkHeader(OperationHeader header) {
        if (TextUtils.isEmpty(header.serverData) || header.serverData.length() > Constants.SERVER_DATA_MAX_LEN) {
            return false;
        }
        return super.checkHeader(header);
    }


    private boolean checkRequest(RegistrationRequest registrationRequest) {
        if (registrationRequest == null) {
            return false;
        }
        if (!checkChallenge(registrationRequest.challenge)) {
            return false;
        }
        if (!checkHeader(registrationRequest.header)) {
            return false;
        }
        if (TextUtils.isEmpty(registrationRequest.username)) {
            return false;
        }
        if (registrationRequest.username.length() > Constants.USERNAME_MAX_LEN) {
            return false;
        }
        if (!checkPolicy(registrationRequest.policy)) {
            return false;
        }
        return true;
    }

    @Override
    public void onAuthenticatorClick(AuthenticatorInfo info) {
        String facetId = Utils.getFacetId(activity.getApplication());

        FinalChallengeParams fcParams = new FinalChallengeParams();
        if (TextUtils.isEmpty(registrationRequest.header.appID)) {
            fcParams.appID = facetId;
        } else {
            fcParams.appID = registrationRequest.header.appID;
        }
        fcParams.challenge = registrationRequest.challenge;
        fcParams.facetID = facetId;
        fcParams.channelBinding = channelBinding;

        finalChallenge = Base64.encodeToString(gson.toJson(fcParams).getBytes(), Base64.URL_SAFE);
        RegisterIn registerIn = new RegisterIn(registrationRequest.header.appID, registrationRequest.username, finalChallenge, info.attestationTypes[0]);

        ASMRequest<RegisterIn> asmRequest = new ASMRequest<>();
        asmRequest.requestType = Request.Register;
        asmRequest.args = registerIn;
        asmRequest.asmVersion = registrationRequest.header.upv;
        asmRequest.authenticatorIndex = info.authenticatorIndex;
        String asmRequestMsg = gson.toJson(asmRequest);
        StatLog.printLog(TAG, "asm request: " + asmRequestMsg);
        ASMApi.doOperation(activity, REQUEST_ASM_OPERATION, asmRequestMsg);
    }
}
