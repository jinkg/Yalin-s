package com.jin.fidoclient.op;


import com.google.gson.Gson;
import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.GetInfoOut;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.msg.MatchCriteria;
import com.jin.fidoclient.msg.Policy;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.msg.client.UAFIntentType;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.AuthenticatorAdapter;
import com.jin.fidoclient.ui.UAFClientActivity;
import com.jin.fidoclient.utils.StatLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by YaLin on 2016/1/11.
 */
public abstract class ASMMessageHandler {

    public static final String TAG = ASMMessageHandler.class.getSimpleName();
    public static final int REQUEST_ASM_OPERATION = 1;

    public static final String REG_TAG = "\"Reg\"";
    public static final String AUTH_TAG = "\"Auth\"";
    public static final String DEREG_TAG = "\"Dereg\"";

    protected final Gson gson = new Gson();

    protected Traffic.OpStat mCurrentState = Traffic.OpStat.PREPARE;

    protected final UAFClientActivity activity;

    public static ASMMessageHandler parseMessage(UAFClientActivity activity, String intentType, String uafMessage, String channelBinding) {
        if (UAFIntentType.UAF_OPERATION.name().equals(intentType)) {
            if (uafMessage.contains(REG_TAG)) {
                return new Reg(activity, uafMessage, channelBinding);
            } else if (uafMessage.contains(AUTH_TAG)) {
                return new Auth(activity, uafMessage, channelBinding);
            } else if (uafMessage.contains(DEREG_TAG)) {
                return new Dereg(activity, uafMessage);
            }
        } else if (UAFIntentType.CHECK_POLICY.name().equals(intentType)) {
            return new CheckPolicy(activity, uafMessage);
        } else if (UAFIntentType.DISCOVER.name().equals(intentType)) {
            return new Discover(activity);
        } else if (UAFIntentType.UAF_OPERATION_COMPLETION_STATUS.name().equals(intentType)) {
            return new Completion(activity);
        }
        return new ASMMessageHandler(activity) {
            @Override
            public boolean startTraffic() {
                return false;
            }

            @Override
            public boolean traffic(String asmResponseMsg) {
                return false;
            }
        };
    }

    public ASMMessageHandler(UAFClientActivity activity) {
        this.activity = activity;
    }

    public abstract boolean startTraffic();

    public abstract boolean traffic(String asmResponseMsg) throws ASMException;

    public Policy getPolicy() {
        return null;
    }

    protected void updateState(Traffic.OpStat newState) {
        StatLog.printLog(TAG, "update op state:" + mCurrentState.name() + "->" + newState.name());
        mCurrentState = newState;
    }

    protected String getInfoRequest(Version version) {
        StatLog.printLog(TAG, "asm request get info");
        ASMRequest<RegisterIn> asmRequest = new ASMRequest<>();
        asmRequest.requestType = Request.GetInfo;
        asmRequest.asmVersion = version;
        String asmRequestMsg = gson.toJson(asmRequest);
        StatLog.printLog(TAG, "asm request: " + asmRequestMsg);
        return asmRequestMsg;
    }

    protected boolean handleGetInfo(String msg, AuthenticatorAdapter.OnAuthenticatorClickCallback callback) {
        StatLog.printLog(TAG, "get info :" + msg);
        ASMResponse asmResponse = ASMResponse.fromJson(msg, GetInfoOut.class);
        if (asmResponse.statusCode != StatusCode.UAF_ASM_STATUS_OK) {
            return false;

        }
        GetInfoOut getInfoOut = (GetInfoOut) asmResponse.responseData;
        AuthenticatorInfo[] authenticatorInfos = getInfoOut.Authenticators;
        StatLog.printLog(TAG, "client reg parse policy: " + gson.toJson(authenticatorInfos));
        if (authenticatorInfos == null) {
            return false;
        }
        List<AuthenticatorInfo> parseResult = parsePolicy(getPolicy(), authenticatorInfos);
        activity.showAuthenticator(parseResult, callback);
        return true;
    }

    protected List<AuthenticatorInfo> parsePolicy(Policy policy, AuthenticatorInfo[] authenticatorInfos) {
        if (policy == null) {
            return Arrays.asList(authenticatorInfos);
        }
        List<AuthenticatorInfo> authenticatorInfoList = new ArrayList<>();
        for (AuthenticatorInfo info : authenticatorInfos) {
            for (MatchCriteria[] criterias : policy.accepted) {
                boolean setMatch = true;
                for (MatchCriteria criteria : criterias) {
                    if (!criteria.isMatch(info)) {
                        setMatch = false;
                        break;
                    }
                }
                if (setMatch) {
                    authenticatorInfoList.add(info);
                    break;
                }
            }
        }

        if (policy.disallowed != null) {
            for (AuthenticatorInfo info : authenticatorInfoList) {
                for (MatchCriteria matchCriteria : policy.disallowed) {
                    if (matchCriteria.isMatch(info)) {
                        authenticatorInfoList.remove(info);
                        break;
                    }
                }
            }
        }
        return authenticatorInfoList;
    }
}
