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

package com.jin.fidoclient.msg.client;

public enum UAFIntentType {
    DISCOVER("DISCOVER"),
    DISCOVER_RESULT("DISCOVER_RESULT"),

    //Perform a no-op check if a message could be processed.
    CHECK_POLICY("CHECK_POLICY"),

    //Check Policy results.
    CHECK_POLICY_RESULT("CHECK_POLICY_RESULT"),

    //Process a Registration, Authentication, Transaction Confirmation or Deregistration message.
    UAF_OPERATION("UAF_OPERATION"),

    //UAF Operation results.
    UAF_OPERATION_RESULT("UAF_OPERATION_RESULT"),

    //Inform the FIDO UAF Client of the completion status of a Registration, Authentication,
    // Transaction Confirmation or Deregistration message.
    UAF_OPERATION_COMPLETION_STATUS("UAF_OPERATION_COMPLETION_STATUS");

    public final String type;

    UAFIntentType(String type) {
        this.type = type;
    }
}
