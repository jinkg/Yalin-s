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

package com.jin.fidoclient.client;

import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;

import java.security.KeyPair;

public class RegistrationRequestProcessor {
    public RegisterOut processRequest(RegisterIn registerIn, KeyPair keyPair, byte[] keyIdBytes, Simulator simulator) {
        RegisterOut registerOut = new RegisterOut();
        RegAssertionBuilder builder = new RegAssertionBuilder(keyPair, simulator);

        setAppId(registerIn, registerOut);

        setAssertions(registerOut, builder, registerIn.finalChallenge, keyIdBytes, simulator.getScheme());
        return registerOut;
    }

    private void setAssertions(RegisterOut registerOut, RegAssertionBuilder builder, String fcParams, byte[] keyIdBytes, String scheme) {
        try {
            registerOut.assertion = builder.getAssertions(fcParams, keyIdBytes);
            registerOut.assertionScheme = scheme;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAppId(RegisterIn registerIn,
                          RegisterOut registerOut) {

    }

}
