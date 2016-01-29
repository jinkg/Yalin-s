package com.jin.fidoclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jin.fidoclient.R;
import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMIntent;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.ASMMessageHandler;
import com.jin.fidoclient.utils.StatLog;

import java.util.List;


/**
 * Created by YaLin on 2015/10/21.
 */
public class UAFClientActivity extends AppCompatActivity {
    private static final String TAG = UAFClientActivity.class.getSimpleName();

    private View coordinator;
    private TextView tvInfo;
    private RecyclerView rvAuthenticators;

    private String intentType;
    private String message;
    private String channelBinding;

    private ASMMessageHandler asmMessageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fido_client);
        Bundle extras = this.getIntent().getExtras();
        intentType = extras.getString(UAFIntent.UAF_INTENT_TYPE_KEY);
        message = extras.getString(UAFIntent.MESSAGE_KEY);
        channelBinding = extras.getString(UAFIntent.CHANNEL_BINDINGS_KEY);
        StatLog.printLog(TAG, "onCreate intentType:" + intentType + " message:" + message + " channelBinding:" + channelBinding);

        findView();
        initData();

        doConfirm();
    }

    void findView() {
        rvAuthenticators = (RecyclerView) findViewById(R.id.rv_authenticators);
        coordinator = findViewById(R.id.root_coordinator);
        rvAuthenticators.setLayoutManager(new GridLayoutManager(this, 2));
        tvInfo = (TextView) findViewById(R.id.tv_prompt);
    }

    void initData() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ASMMessageHandler.REQUEST_ASM_OPERATION) {
                String resultStr = data.getExtras().getString(ASMIntent.MESSAGE_KEY);
                try {
                    asmMessageHandler.traffic(resultStr);
                } catch (ASMException e) {
                    Snackbar.make(coordinator, ASMException.class.getSimpleName() + ":" + e.statusCode, Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private void doConfirm() {
        if (TextUtils.isEmpty(intentType)) {
            showError(R.string.client_op_type_error);
            return;
        }
        processMessage(message);
    }

    private void processMessage(String inUafOperationMsg) {
        StatLog.printLog(TAG, "process message:" + inUafOperationMsg);
        String inMsg = extract(inUafOperationMsg);
        StatLog.printLog(TAG, "extract message is:" + inMsg);
        asmMessageHandler = ASMMessageHandler.parseMessage(this, intentType, inMsg, channelBinding);
        if (!asmMessageHandler.startTraffic()) {
            showError(R.string.handle_error);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = UAFIntent.getUAFOperationCancelIntent(UAFClientError.USER_CANCELLED);
        setResult(RESULT_CANCELED, intent);
    }

    public void showAuthenticator(List<AuthenticatorInfo> infos, AuthenticatorAdapter.OnAuthenticatorClickCallback callback) {
        AuthenticatorAdapter adapter = new AuthenticatorAdapter(this, infos, callback);
        rvAuthenticators.setAdapter(adapter);
    }
}
