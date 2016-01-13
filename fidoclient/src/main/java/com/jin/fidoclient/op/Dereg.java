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
import com.jin.fidoclient.client.RegAssertionBuilder;
import com.jin.fidoclient.msg.DeregisterAuthenticator;
import com.jin.fidoclient.msg.DeregistrationRequest;
import com.jin.fidoclient.msg.Operation;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.utils.Preferences;

import java.util.logging.Logger;

public class Dereg extends ClientOperator {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Gson gson = new Gson();

    private String message;

    public Dereg(String message) {
        this.message = message;
    }

    @Override
    public void handle() {
        logger.info("  [UAF][1]Dereg  ");
        try {
            DeregistrationRequest reg = new DeregistrationRequest();
            reg.header = new OperationHeader();
            reg.header.upv = new Version(1, 0);
            reg.header.op = Operation.Dereg;
            reg.header.appID = Preferences.getSettingsParam("appID");
            reg.authenticators = new DeregisterAuthenticator[1];
            DeregisterAuthenticator deregAuth = new DeregisterAuthenticator();
            deregAuth.aaid = RegAssertionBuilder.AAID;
            String tmp = Preferences.getSettingsParam("keyId");
            byte[] bytes = tmp.getBytes();
            deregAuth.keyID = tmp;
//				Base64.encodeToString(bytes, Base64.NO_WRAP);
            reg.authenticators[0] = deregAuth;

            logger.info("  [UAF][2]Dereg - Reg Response Formed  ");
            Preferences.setSettingsParam("pub", "");
            Preferences.setSettingsParam("priv", "");
            Preferences.setSettingsParam("username", "");
            Preferences.setSettingsParam("keyId", "");
            logger.info("  [UAF][5]Dereg - keys stored  ");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String assemble(String result) {
        return null;
    }
}
