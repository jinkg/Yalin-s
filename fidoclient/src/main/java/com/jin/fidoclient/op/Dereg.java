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


import com.google.gson.Gson;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.DeregisterIn;
import com.jin.fidoclient.msg.DeregResponse;
import com.jin.fidoclient.msg.DeregistrationRequest;
import com.jin.fidoclient.utils.StatLog;


public class Dereg extends ASMMessageHandler {
    private static final String TAG = Dereg.class.getSimpleName();

    private Gson gson = new Gson();

    private final DeregistrationRequest deregistrationRequest;
    private final HandleResultCallback callback;

    public Dereg(String message, HandleResultCallback callback) {
        this.deregistrationRequest = getDeregistrationRequest(message);
        this.callback = callback;
    }

    @Override
    public void handle() {
        try {
            DeregisterIn deregisterIn = new DeregisterIn(deregistrationRequest.header.appID, deregistrationRequest.authenticators[0].keyID);

            ASMRequest<DeregisterIn> asmRequest = new ASMRequest<>();
            asmRequest.requestType = Request.Deregister;
            asmRequest.args = deregisterIn;
            asmRequest.asmVersion = deregistrationRequest.header.upv;
            if (callback != null) {
                callback.onResult(gson.toJson(asmRequest));
            }
        } catch (Exception e) {
            StatLog.printLog(TAG, "generate dereg asm request error. error is:" + e.getMessage());
        }
    }

    @Override
    public String parseAsmResponse(String asmResponseMsg) {
        return gson.toJson(new DeregResponse((short) 0));
    }

    private DeregistrationRequest getDeregistrationRequest(String uafMsg) {
        DeregistrationRequest[] deregistrationRequests = gson.fromJson(uafMsg, DeregistrationRequest[].class);
        return deregistrationRequests[0];
    }
}
