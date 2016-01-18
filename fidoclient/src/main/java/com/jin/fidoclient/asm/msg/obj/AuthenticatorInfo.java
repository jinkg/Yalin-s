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

import com.jin.fidoclient.msg.Version;

public class AuthenticatorInfo {
    public int authenticatorIndex;
    public Version[] asmVersions;
    public boolean isUserEnrolled;
    public boolean hasSettings;
    public String aaid;
    public String assertionScheme;
    public int authenticationAlgorithm;
    public int[] attestationTypes;
    public long userVerification;
    public int keyProtection;
    public int matcherProtection;
    public long attachmentHint;
    public boolean isSecondFactorOnly;
    public boolean isRoamingAuthenticator;
    public String[] supportedExtensionIDs;
    public int tcDisplay;

    public AuthenticatorInfo authenticatorIndex(int authenticatorIndex) {
        this.authenticatorIndex = authenticatorIndex;
        return this;
    }

    public AuthenticatorInfo asmVersions(Version[] asmVersions) {
        this.asmVersions = asmVersions;
        return this;
    }

    public AuthenticatorInfo isUserEnrolled(boolean isUserEnrolled) {
        this.isUserEnrolled = isUserEnrolled;
        return this;
    }

    public AuthenticatorInfo hasSettings(boolean hasSettings) {
        this.hasSettings = hasSettings;
        return this;
    }

    public AuthenticatorInfo aaid(String aaid) {
        this.aaid = aaid;
        return this;
    }

    public AuthenticatorInfo assertionScheme(String assertionScheme) {
        this.assertionScheme = assertionScheme;
        return this;
    }

    public AuthenticatorInfo authenticationAlgorithm(int authenticationAlgorithm) {
        this.authenticationAlgorithm = authenticationAlgorithm;
        return this;
    }

    public AuthenticatorInfo attestationTypes(int[] attestationTypes) {
        this.attestationTypes = attestationTypes;
        return this;
    }

    public AuthenticatorInfo userVerification(int userVerification) {
        this.userVerification = userVerification;
        return this;
    }

    public AuthenticatorInfo keyProtection(int keyProtection) {
        this.keyProtection = keyProtection;
        return this;
    }

    public AuthenticatorInfo matcherProtection(int matcherProtection) {
        this.matcherProtection = matcherProtection;
        return this;
    }

    public AuthenticatorInfo attachmentHint(long attachmentHint) {
        this.attachmentHint = attachmentHint;
        return this;
    }

    public AuthenticatorInfo isSecondFactorOnly(boolean isSecondFactorOnly) {
        this.isSecondFactorOnly = isSecondFactorOnly;
        return this;
    }

    public AuthenticatorInfo isRoamingAuthenticator(boolean isRoamingAuthenticator) {
        this.isRoamingAuthenticator = isRoamingAuthenticator;
        return this;
    }

    public AuthenticatorInfo supportedExtensionIDs(String[] supportedExtensionIDs) {
        this.supportedExtensionIDs = supportedExtensionIDs;
        return this;
    }

    public AuthenticatorInfo tcDisplay(int tcDisplay) {
        this.tcDisplay = tcDisplay;
        return this;
    }


}
