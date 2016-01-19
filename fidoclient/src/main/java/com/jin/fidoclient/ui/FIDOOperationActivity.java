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
import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.api.ASMIntent;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;
import com.jin.fidoclient.msg.client.UAFIntentType;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.ASMMessageHandler;

import java.util.List;


/**
 * Created by YaLin on 2015/10/21.
 */
public class FIDOOperationActivity extends AppCompatActivity implements ASMMessageHandler.HandleResultCallback {
    private static final String TAG = FIDOOperationActivity.class.getSimpleName();
    public static final int REQUEST_ASM_OPERATION = 1;

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
            if (requestCode == REQUEST_ASM_OPERATION) {
                String resultStr = data.getExtras().getString(ASMIntent.MESSAGE_KEY);
                String response;
                try {
                    response = asmMessageHandler.parseAsmResponse(resultStr);
                    Intent intent = null;
                    if (UAFIntentType.UAF_OPERATION.name().equals(intentType)) {
                        intent = UAFIntent.getUAFOperationResultIntent(getComponentName().flattenToString(), UAFClientError.NO_ERROR, new UAFMessage(response).toJson());
                    } else if (UAFIntentType.CHECK_POLICY.name().equals(intentType)) {
                        intent = UAFIntent.getCheckPolicyResultIntent(getComponentName().flattenToString(), UAFClientError.NO_ERROR);
                    }
                    if (intent != null) {
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch (ASMException e) {
                    Snackbar.make(coordinator, ASMException.class.getSimpleName() + ":" + e.statusCode, Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private void doConfirm() {
        if (TextUtils.isEmpty(message)) {
            showError(R.string.message_error);
            return;
        }
        processMessage(message);
    }

    private void processMessage(String inUafOperationMsg) {
        String inMsg = extract(inUafOperationMsg);
        asmMessageHandler = ASMMessageHandler.parseMessage(this, intentType, inMsg, channelBinding, this);
        asmMessageHandler.handle();
    }

    private String extract(String inMsg) {
        UAFMessage uafMessage = new UAFMessage();
        uafMessage.loadFromJson(inMsg);
        return uafMessage.uafProtocolMessage;
    }

    private void showError(int errorId) {
        tvInfo.setText(errorId);
    }

    @Override
    public void onResult(String asmRequest) {
        if (asmRequest != null) {
            ASMApi.doOperation(this, REQUEST_ASM_OPERATION, asmRequest);
        } else {

        }
    }

    public void showAuthenticator(List<AuthenticatorInfo> infos, AuthenticatorAdapter.OnAuthenticatorClickCallback callback) {
        AuthenticatorAdapter adapter = new AuthenticatorAdapter(this, infos, callback);
        rvAuthenticators.setAdapter(adapter);
    }

}
