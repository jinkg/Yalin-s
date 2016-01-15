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

import com.google.gson.Gson;
import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.DeregisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.client.RegAssertionBuilder;
import com.jin.fidoclient.msg.DeregResponse;
import com.jin.fidoclient.msg.DeregisterAuthenticator;
import com.jin.fidoclient.msg.DeregistrationRequest;
import com.jin.fidoclient.msg.Operation;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.msg.RegistrationRequest;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.utils.Preferences;

import java.util.logging.Logger;

public class Dereg extends ClientOperator {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Gson gson = new Gson();

    private DeregistrationRequest deregistrationRequest;
    private Activity activity;

    public Dereg(Activity activity, String message) {
        this.activity = activity;
        this.deregistrationRequest = getDeregistrationRequest(message);
    }

    @Override
    public void handle() {
        logger.info("  [UAF][1]Dereg  ");
        try {
            DeregisterIn deregisterIn = new DeregisterIn(deregistrationRequest.header.appID, deregistrationRequest.authenticators[0].keyID);

            ASMRequest<DeregisterIn> asmRequest = new ASMRequest<>();
            asmRequest.requestType = Request.Deregister;
            asmRequest.args = deregisterIn;
            asmRequest.asmVersion = deregistrationRequest.header.upv;
            ASMApi.doOperation(activity, REQUEST_ASM_OPERATION, gson.toJson(asmRequest));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String assemble(String result) {
        return gson.toJson(new DeregResponse((short) 0));
    }

    private DeregistrationRequest getDeregistrationRequest(String uafMsg) {
        DeregistrationRequest[] deregistrationRequests = gson.fromJson(uafMsg, DeregistrationRequest[].class);
        return deregistrationRequests[0];
    }
}
