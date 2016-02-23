/*
 * Copyright 2015 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.constants.Constants;
import com.jin.fidoclient.msg.AuthenticationRequest;
import com.jin.fidoclient.msg.AuthenticationResponse;
import com.jin.fidoclient.msg.AuthenticatorSignAssertion;
import com.jin.fidoclient.msg.ChannelBinding;
import com.jin.fidoclient.msg.FinalChallengeParams;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.msg.Policy;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.AuthenticatorAdapter;
import com.jin.fidoclient.ui.UAFClientActivity;
import com.jin.fidoclient.utils.StatLog;
import com.jin.fidoclient.utils.Utils;

public class Auth extends ASMMessageHandler implements AuthenticatorAdapter.OnAuthenticatorClickCallback {
    private static final String TAG = Auth.class.getSimpleName();
    private final AuthenticationRequest authenticationRequest;
    private final ChannelBinding channelBinding;

    private String finalChallenge;

    public Auth(UAFClientActivity activity, String message, String channelBinding) {
        super(activity);
        try {
            authenticationRequest = getAuthRequest(message);
        } catch (Exception e) {
            throw new IllegalStateException("Authentication message error");
        }
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
        if (authenticationRequest == null || activity == null) {
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
                updateState(Traffic.OpStat.AUTH_PENDING);
                break;
            case AUTH_PENDING:
                handleAuthOut(asmResponseMsg);
                updateState(Traffic.OpStat.PREPARE);
                break;
            default:
                return false;
        }
        return true;

    }

    @Override
    public Policy getPolicy() {
        return authenticationRequest.policy;
    }

    private void handleAuthOut(String msg) throws ASMException {
        ASMResponse asmResponse = ASMResponse.fromJson(msg, AuthenticateOut.class);
        if (asmResponse.statusCode != StatusCode.UAF_ASM_STATUS_OK) {
            throw new ASMException(asmResponse.statusCode);
        }
        String response;
        if (asmResponse.responseData instanceof AuthenticateOut) {
            AuthenticateOut authenticateOut = (AuthenticateOut) asmResponse.responseData;
            AuthenticationResponse[] responses = new AuthenticationResponse[1];
            responses[0] = wrapResponse(authenticateOut);
            response = gson.toJson(responses);
        } else {
            throw new ASMException(StatusCode.UAF_ASM_STATUS_ERROR);
        }
        Intent intent = UAFIntent.getUAFOperationResultIntent(activity.getComponentName().flattenToString(), new UAFMessage(response).toJson());
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    private AuthenticationResponse wrapResponse(AuthenticateOut authenticateOut) {
        AuthenticationResponse response = new AuthenticationResponse();
        response.header = new OperationHeader();
        response.header.serverData = authenticationRequest.header.serverData;
        response.header.appID = authenticationRequest.header.appID;
        response.header.op = authenticationRequest.header.op;
        response.header.upv = authenticationRequest.header.upv;

        response.fcParams = finalChallenge;

        response.assertions = new AuthenticatorSignAssertion[1];
        response.assertions[0] = new AuthenticatorSignAssertion();
        response.assertions[0].assertion = authenticateOut.assertion;
        response.assertions[0].assertionScheme = authenticateOut.assertionScheme;
        return response;
    }

    public AuthenticationRequest getAuthRequest(String uafMsg) {
        AuthenticationRequest[] requests;
        try {
            requests = gson.fromJson(uafMsg, AuthenticationRequest[].class);
        } catch (Exception e) {
            requests = null;
        }
        AuthenticationRequest result;
        if (requests == null || requests.length == 0) {
            return null;
        }
        AuthenticationRequest temp = null;
        for (AuthenticationRequest request : requests) {
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

    private boolean checkRequest(AuthenticationRequest authenticationRequest) {
        if (authenticationRequest == null) {
            return false;
        }
        if (!checkChallenge(authenticationRequest.challenge)) {
            return false;
        }
        if (!checkHeader(authenticationRequest.header)) {
            return false;
        }
        if (!checkPolicy(authenticationRequest.policy)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean checkHeader(OperationHeader header) {
        if (TextUtils.isEmpty(header.serverData) || header.serverData.length() > Constants.SERVER_DATA_MAX_LEN) {
            return false;
        }
        return super.checkHeader(header);
    }

    @Override
    public void onAuthenticatorClick(AuthenticatorInfo info) {
        String facetId = Utils.getFacetId(activity.getApplication());
        FinalChallengeParams fcParams = new FinalChallengeParams();
        if (TextUtils.isEmpty(authenticationRequest.header.appID)) {
            fcParams.appID = facetId;
        } else {
            fcParams.appID = authenticationRequest.header.appID;
        }
        fcParams.challenge = authenticationRequest.challenge;
        fcParams.facetID = facetId;
        fcParams.channelBinding = channelBinding;

        finalChallenge = Base64.encodeToString(gson.toJson(fcParams).getBytes(), Base64.URL_SAFE);
        AuthenticateIn authenticateIn = new AuthenticateIn(authenticationRequest.header.appID, null, finalChallenge);

        ASMRequest<AuthenticateIn> asmRequest = new ASMRequest<>();
        asmRequest.requestType = Request.Authenticate;
        asmRequest.args = authenticateIn;
        asmRequest.asmVersion = authenticationRequest.header.upv;
        asmRequest.authenticatorIndex = info.authenticatorIndex;
        String asmRequestMsg = gson.toJson(asmRequest);
        StatLog.printLog(TAG, "asm request: " + asmRequestMsg);
        ASMApi.doOperation(activity, REQUEST_ASM_OPERATION, asmRequestMsg);
    }
}
