package com.jin.fidoclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jin.fidoclient.R;
import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMApi;
import com.jin.fidoclient.asm.api.ASMIntent;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.msg.client.UAFIntentType;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.ASMMessageHandler;


/**
 * Created by YaLin on 2015/10/21.
 */
public class FIDOOperationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = FIDOOperationActivity.class.getSimpleName();
    public static final int REQUEST_ASM_OPERATION = 1;

    private TextView tvOperation;
    private TextView tvUafMsg;
    private View coordinator;

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
        tvOperation = (TextView) findViewById(R.id.textViewOperation);
        tvUafMsg = (TextView) findViewById(R.id.textViewOpMsg);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        coordinator = findViewById(R.id.root_coordinator);
    }

    void initData() {
        tvOperation.setText(intentType);
        tvUafMsg.setText(message);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_confirm) {
            doConfirm();
        }
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
        asmMessageHandler = ASMMessageHandler.parseMessage(this, intentType, inMsg, channelBinding);
        String asmRequest = asmMessageHandler.generateAsmRequest();
        if (asmRequest != null) {
            ASMApi.doOperation(this, REQUEST_ASM_OPERATION, asmRequest);
        } else {

        }
    }

    private void checkPolicy(String uafMessage) {

    }

    private String extract(String inMsg) {
        UAFMessage uafMessage = new UAFMessage();
        uafMessage.loadFromJson(inMsg);
        return uafMessage.uafProtocolMessage;
    }

    private void showError(int errorId) {
        tvOperation.setText(errorId);
    }
}
