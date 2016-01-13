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

import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;

public class AuthenticationRequestProcessor {

    public AuthenticateOut processRequest(RegRecord regRecord, AuthenticateIn authenticateIn) {
        AuthenticateOut authenticateOut = new AuthenticateOut();
        AuthAssertionBuilder builder = new AuthAssertionBuilder();

        setAssertions(regRecord, authenticateOut, builder, authenticateIn.finalChallenge);
        return authenticateOut;
    }

    private void setAssertions(RegRecord regRecord, AuthenticateOut authenticateOut, AuthAssertionBuilder builder, String fcParams) {
        try {
            authenticateOut.assertion = builder.getAssertions(regRecord, fcParams);
            authenticateOut.assertionScheme = "UAFV1TLV";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
