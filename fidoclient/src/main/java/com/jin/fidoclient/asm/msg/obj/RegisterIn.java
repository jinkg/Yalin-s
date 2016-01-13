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

package com.jin.fidoclient.asm.msg.obj;

public class RegisterIn {
    public String appID;
    public String username;
    public String finalChallenge;
    public int attestationType;

    public RegisterIn(String appID, String username, String finalChallenge, int attestationType) {
        this.appID = appID;
        this.username = username;
        this.finalChallenge = finalChallenge;
        this.attestationType = attestationType;
    }
}
