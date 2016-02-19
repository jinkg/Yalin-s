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

import com.google.gson.Gson;
import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.DeregisterIn;
import com.jin.fidoclient.msg.DeregResponse;
import com.jin.fidoclient.msg.DeregistrationRequest;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.UAFClientActivity;
import com.jin.fidoclient.utils.StatLog;


public class Dereg extends ASMMessageHandler {
    private static final String TAG = Dereg.class.getSimpleName();

    private Gson gson = new Gson();

    private final DeregistrationRequest deregistrationRequest;

    public Dereg(UAFClientActivity activity, String message) {
        super(activity);
        updateState(Traffic.OpStat.PREPARE);
        this.deregistrationRequest = getDeregistrationRequest(message);
    }

    @Override
    public boolean startTraffic() {
        if (deregistrationRequest == null) {
            return false;
        }
        switch (mCurrentState) {
            case PREPARE:
                String deregMsg = dereg();
                ASMApi.doOperation(activity, REQUEST_ASM_OPERATION, deregMsg);
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
                String deregMsg = gson.toJson(new DeregResponse((short) 0));
                handleDeregOut(deregMsg);
                updateState(Traffic.OpStat.PREPARE);
                break;
            default:
                return false;
        }
        return true;
    }

    private String dereg() {
        DeregisterIn deregisterIn = new DeregisterIn(deregistrationRequest.header.appID, deregistrationRequest.authenticators[0].keyID);

        ASMRequest<DeregisterIn> asmRequest = new ASMRequest<>();
        asmRequest.requestType = Request.Deregister;
        asmRequest.args = deregisterIn;
        asmRequest.asmVersion = deregistrationRequest.header.upv;
        String asmRequestMsg = gson.toJson(asmRequest);
        StatLog.printLog(TAG, "asm request: " + asmRequestMsg);
        return asmRequestMsg;
    }

    private void handleDeregOut(String msg) {
        StatLog.printLog(TAG, "client dereg result:" + msg);
        Intent intent = UAFIntent.getUAFOperationResultIntent(activity.getComponentName().flattenToString(), new UAFMessage(msg).toJson());
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    private DeregistrationRequest getDeregistrationRequest(String uafMsg) {
        DeregistrationRequest[] deregistrationRequests = gson.fromJson(uafMsg, DeregistrationRequest[].class);
        DeregistrationRequest request = deregistrationRequests[0];
        if (!checkRequest(request)) {
            request = null;
        }
        return request;
    }

    private boolean checkRequest(DeregistrationRequest request) {
        if (request == null) {
            return false;
        }
        if (request.authenticators == null || request.authenticators.length == 0) {
            return false;
        }
        return true;
    }
}
