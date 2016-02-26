package com.jin.fidoclient.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jin.fidoclient.R;
import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMIntent;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.ASMMessageHandler;
import com.jin.fidoclient.op.traffic.Traffic;
import com.jin.fidoclient.ui.AuthenticatorAdapter;
import com.jin.fidoclient.ui.UAFClientActivity;
import com.jin.fidoclient.utils.StatLog;

import java.util.List;

/**
 * Created by YaLin on 2016/2/26.
 */
public class AuthenticatorListFragment extends Fragment implements ASMMessageHandler.StateChangeListener {
    private static final String TAG = AuthenticatorListFragment.class.getSimpleName();

    private View mCoordinator;
    private TextView tvOpType;
    private TextView tvInfo;
    private RecyclerView rvAuthenticators;

    private String mIntentType;
    private String mMessage;
    private String mChannelBinding;

    private ASMMessageHandler asmMessageHandler;

    public static AuthenticatorListFragment getInstance(String intentType, String message, String channelBinding) {
        AuthenticatorListFragment fragment = new AuthenticatorListFragment();
        fragment.mIntentType = intentType;
        fragment.mMessage = message;
        fragment.mChannelBinding = channelBinding;
        return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authenticator_list, container, false);
        findView(view);
        initData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doConfirm();
    }

    private void findView(View view) {
        mCoordinator = view.findViewById(R.id.root_coordinator);
        rvAuthenticators = (RecyclerView) view.findViewById(R.id.rv_authenticators);
        tvOpType = (TextView) view.findViewById(R.id.tv_op_type);
        tvInfo = (TextView) view.findViewById(R.id.tv_prompt);

        rvAuthenticators.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    }

    private void initData() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ASMMessageHandler.REQUEST_ASM_OPERATION) {
                String resultStr = data.getExtras().getString(ASMIntent.MESSAGE_KEY);
                try {
                    if (!asmMessageHandler.traffic(resultStr)) {
                        finishWithError(UAFClientError.PROTOCOL_ERROR);
                    }
                } catch (ASMException e) {
                    Snackbar.make(mCoordinator, ASMException.class.getSimpleName() + ":" + e.statusCode, Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    public void showAuthenticator(List<AuthenticatorInfo> infos, AuthenticatorAdapter.OnAuthenticatorClickCallback callback) {
        AuthenticatorAdapter adapter = new AuthenticatorAdapter(getActivity(), infos, callback);
        rvAuthenticators.setAdapter(adapter);
    }

    private void doConfirm() {
        if (TextUtils.isEmpty(mIntentType)) {
            showError(R.string.client_op_type_error);
            return;
        }
        processMessage(mMessage);
    }

    private void processMessage(String inUafOperationMsg) {
        StatLog.printLog(TAG, "process message:" + inUafOperationMsg);
        String inMsg = extract(inUafOperationMsg);
        StatLog.printLog(TAG, "extract message is:" + inMsg);
        asmMessageHandler = ASMMessageHandler.parseMessage(this, mIntentType, inMsg, mChannelBinding);
        asmMessageHandler.setStateChangeListener(this);
        asmMessageHandler.setAsmPackage(UAFClientActivity.getAsmPack(getActivity().getApplicationContext()));
        if (!asmMessageHandler.startTraffic()) {
            finishWithError(UAFClientError.PROTOCOL_ERROR);
        }
    }

    private String extract(String inMsg) {
        if (TextUtils.isEmpty(inMsg)) {
            return null;
        }
        UAFMessage uafMessage = new UAFMessage();
        uafMessage.loadFromJson(inMsg);
        return uafMessage.uafProtocolMessage;
    }

    private void showError(int errorId) {
        tvInfo.setText(errorId);
    }

    private void setFailedIntent(short errorCode) {
        Intent intent = UAFIntent.getUAFOperationErrorIntent(getActivity().getComponentName().flattenToString(), errorCode);
        getActivity().setResult(Activity.RESULT_CANCELED, intent);
    }

    private void finishWithError(short errorCode) {
        setFailedIntent(errorCode);
        getActivity().finish();
    }

    @Override
    public void onStateChange(Traffic.OpStat newState, Traffic.OpStat oldState) {

    }
}
