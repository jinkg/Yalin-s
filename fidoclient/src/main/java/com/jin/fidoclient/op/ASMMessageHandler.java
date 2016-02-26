package com.jin.fidoclient.op;


import android.text.TextUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.GetInfoOut;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.constants.Constants;
import com.jin.fidoclient.msg.MatchCriteria;
import com.jin.fidoclient.msg.OperationHeader;
import com.jin.fidoclient.msg.Policy;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.msg.client.UAFIntentType;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.AuthenticatorAdapter;
import com.jin.fidoclient.ui.fragment.AuthenticatorListFragment;
import com.jin.fidoclient.utils.StatLog;
import com.jin.fidoclient.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by YaLin on 2016/1/11.
 */
public abstract class ASMMessageHandler {

    public interface StateChangeListener {
        void onStateChange(Traffic.OpStat newState, Traffic.OpStat oldState);
    }

    public static final String TAG = ASMMessageHandler.class.getSimpleName();
    public static final int REQUEST_ASM_OPERATION = 1;

    public static final String REG_TAG = "\"Reg\"";
    public static final String AUTH_TAG = "\"Auth\"";
    public static final String DEREG_TAG = "\"Dereg\"";

    private static final String UPV_TAG = "\"upv\"";
    private static final String MAJOR_TAG = "major";
    private static final String MINOR_TAG = "minor";

    protected final Gson gson = new Gson();

    protected Traffic.OpStat mCurrentState = Traffic.OpStat.PREPARE;

    protected final AuthenticatorListFragment fragment;
    protected StateChangeListener stateChangeListener;

    protected String asmPackage;

    public static ASMMessageHandler parseMessage(AuthenticatorListFragment fragment, String intentType, String uafMessage, String channelBinding) {
        if (UAFIntentType.UAF_OPERATION.name().equals(intentType)) {
            boolean versionLegal = false;
            try {
                String upvSub = uafMessage.substring(uafMessage.indexOf(UPV_TAG));
                String upv = upvSub.substring(upvSub.indexOf("{"), upvSub.indexOf("}") + 1);
                JSONObject jsonObject = new JSONObject(upv);
                if (jsonObject.getInt(MAJOR_TAG) == 1 && jsonObject.getInt(MINOR_TAG) == 0) {
                    versionLegal = true;
                }
            } catch (Exception ignored) {
            }
            if (versionLegal) {
                if (uafMessage.contains(REG_TAG)) {
                    return new Reg(fragment, uafMessage, channelBinding);
                } else if (uafMessage.contains(AUTH_TAG)) {
                    return new Auth(fragment, uafMessage, channelBinding);
                } else if (uafMessage.contains(DEREG_TAG)) {
                    return new Dereg(fragment, uafMessage);
                }
            }
        } else if (UAFIntentType.CHECK_POLICY.name().equals(intentType)) {
            return new CheckPolicy(fragment, uafMessage);
        } else if (UAFIntentType.DISCOVER.name().equals(intentType)) {
            return new Discover(fragment);
        } else if (UAFIntentType.UAF_OPERATION_COMPLETION_STATUS.name().equals(intentType)) {
            return new Completion(fragment);
        }
        return new ASMMessageHandler(fragment) {
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

    public void setStateChangeListener(StateChangeListener listener) {
        stateChangeListener = listener;
    }

    public ASMMessageHandler(AuthenticatorListFragment fragment) {
        this.fragment = fragment;
    }

    public abstract boolean startTraffic();

    public abstract boolean traffic(String asmResponseMsg) throws ASMException;

    public String getCurrentOpDescription() {
        return null;
    }

    public void setAsmPackage(String asmPackage) {
        this.asmPackage = asmPackage;
    }

    public Policy getPolicy() {
        return null;
    }

    protected boolean checkHeader(OperationHeader header) {
        if (header == null) {
            return false;
        }
        if (header.appID == null || header.appID.length() > Constants.APP_ID_MAX_LEN) {
            return false;
        }
        if (header.appID.length() > 0 && !header.appID.contains(Constants.APP_ID_PREFIX) && !header.appID.equals(Utils.getFacetId(fragment.getActivity().getApplicationContext()))) {
            return false;
        }
        return true;
    }

    protected boolean checkChallenge(String challenge) {
        if (TextUtils.isEmpty(challenge)) {
            return false;
        }
        if (challenge.length() < Constants.CHALLENGE_MIN_LEN || challenge.length() > Constants.CHALLENGE_MAX_LEN) {
            return false;
        }
        if (!challenge.matches(Constants.BASE64_REGULAR)) {
            return false;
        }
        return true;
    }

    protected boolean checkPolicy(Policy policy) {
        if (policy == null || policy.accepted == null) {
            return false;
        }
        return true;
    }

    protected void updateState(Traffic.OpStat newState) {
        StatLog.printLog(TAG, "update op state:" + mCurrentState.name() + "->" + newState.name());
        if (stateChangeListener != null) {
            stateChangeListener.onStateChange(newState, mCurrentState);
        }
        mCurrentState = newState;
    }

    protected String getInfoRequest(Version version) {
        StatLog.printLog(TAG, "asm request get info");
        ASMRequest<RegisterIn> asmRequest = new ASMRequest<>();
        asmRequest.requestType = Request.GetInfo;
        asmRequest.asmVersion = version;
        Gson gson = new GsonBuilder().setExclusionStrategies(getExclusionStrategy())
                .create();
        String asmRequestMsg = gson.toJson(asmRequest);
        StatLog.printLog(TAG, "asm request: " + asmRequestMsg);
        return asmRequestMsg;
    }

    private ExclusionStrategy getExclusionStrategy() {
        return new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getName().equals(ASMRequest.authenticatorIndexName);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
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
        if (parseResult == null || parseResult.size() == 0) {
            return false;
        }
        fragment.showAuthenticator(parseResult, callback);
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
