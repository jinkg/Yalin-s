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

import com.google.gson.Gson;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.DeregisterIn;
import com.jin.fidoclient.constants.Constants;
import com.jin.fidoclient.msg.DeRegResponse;
import com.jin.fidoclient.msg.DeRegistrationRequest;
import com.jin.fidoclient.msg.DeRegisterAuthenticator;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.UAFClientActivity;
import com.jin.fidoclient.utils.StatLog;


public class Dereg extends ASMMessageHandler {
    private static final String TAG = Dereg.class.getSimpleName();

    private Gson gson = new Gson();

    private final DeRegistrationRequest deRegistrationRequest;

    public Dereg(UAFClientActivity activity, String message) {
        super(activity);
        updateState(Traffic.OpStat.PREPARE);
        this.deRegistrationRequest = getDeRegistrationRequest(message);
    }

    @Override
    public boolean startTraffic() {
        if (deRegistrationRequest == null) {
            return false;
        }
        switch (mCurrentState) {
            case PREPARE:
                String deRegMsg = deReg();
                ASMApi.doOperation(activity, REQUEST_ASM_OPERATION, deRegMsg);
                updateState(Traffic.OpStat.DEREG_PENDING);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean traffic(String asmResponseMsg) {
        switch (mCurrentState) {
            case DEREG_PENDING:
                String deRegMsg = gson.toJson(new DeRegResponse((short) 0));
                handleDeRegOut(deRegMsg);
                updateState(Traffic.OpStat.PREPARE);
                break;
            default:
                return false;
        }
        return true;
    }

    private String deReg() {
        DeregisterIn deregisterIn = new DeregisterIn(deRegistrationRequest.header.appID, deRegistrationRequest.authenticators[0].keyID);

        ASMRequest<DeregisterIn> asmRequest = new ASMRequest<>();
        asmRequest.requestType = Request.Deregister;
        asmRequest.args = deregisterIn;
        asmRequest.asmVersion = deRegistrationRequest.header.upv;
        String asmRequestMsg = gson.toJson(asmRequest);
        StatLog.printLog(TAG, "asm request: " + asmRequestMsg);
        return asmRequestMsg;
    }

    private void handleDeRegOut(String msg) {
        StatLog.printLog(TAG, "client deReg result:" + msg);
        Intent intent = UAFIntent.getUAFOperationResultIntent(activity.getComponentName().flattenToString(), new UAFMessage(msg).toJson());
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    private DeRegistrationRequest getDeRegistrationRequest(String uafMsg) {
        DeRegistrationRequest[] requests;
        try {
            requests = gson.fromJson(uafMsg, DeRegistrationRequest[].class);
        } catch (Exception e) {
            requests = null;
        }
        DeRegistrationRequest result;
        if (requests == null || requests.length == 0) {
            return null;
        }
        DeRegistrationRequest temp = null;
        for (DeRegistrationRequest request : requests) {
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

    private boolean checkRequest(DeRegistrationRequest request) {
        if (request == null) {
            return false;
        }
        if (!checkHeader(request.header)) {
            return false;
        }
        if (!checkAuthenticators(request.authenticators)) {
            return false;
        }
        return true;
    }

    private boolean checkAuthenticators(DeRegisterAuthenticator[] authenticators) {
        if (authenticators == null) {
            return false;
        }
        DeRegisterAuthenticator authenticator = authenticators[0];
        if (authenticator == null) {
            return false;
        }
        if (TextUtils.isEmpty(authenticator.aaid) || TextUtils.isEmpty(authenticator.keyID)) {
            return false;
        }
        if (authenticator.aaid.length() != 9 || authenticator.keyID.length() < Constants.KEY_ID_MIN_LEN || authenticator.keyID.length() > Constants.KEY_ID_MAX_LEN) {
            return false;
        }
        if (!authenticator.keyID.matches(Constants.BASE64_REGULAR)) {
            return false;
        }
        if (authenticator.keyID.contains("=")) {
            return false;
        }
        return true;
    }
}
