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


import android.text.TextUtils;
import android.util.Base64;

import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.msg.AuthenticationRequest;
import com.jin.fidoclient.msg.AuthenticationResponse;
import com.jin.fidoclient.msg.AuthenticatorSignAssertion;
import com.jin.fidoclient.msg.ChannelBinding;
import com.jin.fidoclient.msg.FinalChallengeParams;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.ui.AuthenticatorAdapter;
import com.jin.fidoclient.ui.FIDOOperationActivity;
import com.jin.fidoclient.utils.StatLog;
import com.jin.fidoclient.utils.Utils;

import java.util.List;

public class Auth extends ASMMessageHandler implements AuthenticatorAdapter.OnAuthenticatorClickCallback {
    private static final String TAG = Auth.class.getSimpleName();
    private final AuthenticationRequest authenticationRequest;
    private final FIDOOperationActivity activity;
    private final ChannelBinding channelBinding;
    private final HandleResultCallback callback;

    private String finalChallenge;

    public Auth(FIDOOperationActivity activity, String message, String channelBinding, HandleResultCallback callback) {
        this.activity = activity;
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
        this.callback = callback;
    }

    @Override
    public void handle() {
        List<AuthenticatorInfo> authenticatorInfos = parsePolicy(authenticationRequest.policy);
        StatLog.printLog(TAG, "client auth parse policy: " + gson.toJson(authenticatorInfos));
        if (authenticatorInfos == null || authenticatorInfos.isEmpty()) {
            return;
        }
        activity.showAuthenticator(authenticatorInfos, this);
    }

    @Override
    public String parseAsmResponse(String asmResponseMsg) throws ASMException {
        ASMResponse asmResponse = ASMResponse.fromJson(asmResponseMsg, AuthenticateOut.class);
        if (asmResponse.statusCode != StatusCode.UAF_ASM_STATUS_OK) {
            throw new ASMException(asmResponse.statusCode);
        }
        if (asmResponse.responseData instanceof AuthenticateOut) {
            AuthenticateOut authenticateOut = (AuthenticateOut) asmResponse.responseData;
            AuthenticationResponse[] responses = new AuthenticationResponse[1];
            responses[0] = wrapResponse(authenticateOut);
            return gson.toJson(responses);
        } else {
            throw new ASMException(StatusCode.UAF_ASM_STATUS_ERROR);
        }
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
        return gson.fromJson(uafMsg, AuthenticationRequest[].class)[0];
    }

    @Override
    public void onAuthenticatorClick(AuthenticatorInfo info) {
        String facetId = Utils.getFacetId(activity.getApplication());
        if (TextUtils.isEmpty(authenticationRequest.header.appID)) {
            authenticationRequest.header.appID = facetId;
        }

        FinalChallengeParams fcParams = new FinalChallengeParams();
        fcParams.appID = authenticationRequest.header.appID;
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
        if (callback != null) {
            callback.onResult(gson.toJson(asmRequest));
        }
    }
}
