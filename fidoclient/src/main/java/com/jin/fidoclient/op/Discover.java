package com.jin.fidoclient.op;

import android.app.Activity;
import android.content.Intent;

import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.asm.msg.obj.GetInfoOut;
import com.jin.fidoclient.msg.Authenticator;
import com.jin.fidoclient.msg.DiscoverData;
import com.jin.fidoclient.msg.Version;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.fragment.AuthenticatorListFragment;
import com.jin.fidoclient.utils.StatLog;

/**
 * Created by YaLin on 2016/1/21.
 */
public class Discover extends ASMMessageHandler {
    private static final String TAG = Discover.class.getSimpleName();

    public Discover(AuthenticatorListFragment fragment) {
        super(fragment);
        updateState(Traffic.OpStat.PREPARE);
    }

    @Override
    public boolean startTraffic() {

        switch (mCurrentState) {
            case PREPARE:
                String getInfoMessage = getInfoRequest(new Version(1, 0));
                ASMApi.doOperation(fragment, REQUEST_ASM_OPERATION, getInfoMessage, asmPackage);
                updateState(Traffic.OpStat.GET_INFO_PENDING);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean traffic(String asmResponseMsg) throws ASMException {
        StatLog.printLog(TAG, "asm response: " + asmResponseMsg);
        switch (mCurrentState) {
            case GET_INFO_PENDING:
                if (!handleGetInfo(asmResponseMsg)) {
                    return false;
                }
                updateState(Traffic.OpStat.PREPARE);
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handleGetInfo(String asmResponseMsg) {
        StatLog.printLog(TAG, "get info :" + asmResponseMsg);
        ASMResponse asmResponse = ASMResponse.fromJson(asmResponseMsg, GetInfoOut.class);
        if (asmResponse.statusCode != StatusCode.UAF_ASM_STATUS_OK) {
            return false;

        }
        GetInfoOut getInfoOut = (GetInfoOut) asmResponse.responseData;
        AuthenticatorInfo[] authenticatorInfos = getInfoOut.Authenticators;
        if (authenticatorInfos == null) {
            return false;
        }

        Authenticator[] authenticators = Authenticator.fromInfo(authenticatorInfos);
        if (authenticators == null) {
            return false;
        }
        DiscoverData discoverData = new DiscoverData();
        discoverData.supportedUAFVersions = authenticators[0].supportedUAFVersions;
        discoverData.availableAuthenticators = authenticators;
        discoverData.clientVendor = "yalin";
        discoverData.clientVersion = new Version(1, 0);
        String result = gson.toJson(discoverData);
        StatLog.printLog(TAG, "discover prepare result:" + result);
        Intent intent = UAFIntent.getDiscoverResultIntent(result, fragment.getActivity().getComponentName().flattenToString(), UAFClientError.NO_ERROR);
        fragment.getActivity().setResult(Activity.RESULT_OK, intent);
        fragment.getActivity().finish();
        return true;
    }
}
