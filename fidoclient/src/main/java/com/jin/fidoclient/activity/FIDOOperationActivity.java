package com.jin.fidoclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jin.fidoclient.R;
import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMIntent;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.op.ClientOperator;
import com.jin.fidoclient.utils.StatLog;


/**
 * Created by YaLin on 2015/10/21.
 */
public class FIDOOperationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = FIDOOperationActivity.class.getSimpleName();

    private TextView tvOperation;
    private TextView tvUafMsg;
    private Button btnConfirm;
    private View coordinator;

    private String intentType;
    private String message;
    private String channelBinding;

    private ClientOperator clientOperator;

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
    }

    void findView() {
        tvOperation = (TextView) findViewById(R.id.textViewOperation);
        tvUafMsg = (TextView) findViewById(R.id.textViewOpMsg);
        btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
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
            if (requestCode == ClientOperator.REQUEST_ASM_OPERATION) {
                String resultStr = data.getExtras().getString(ASMIntent.MESSAGE_KEY);
                String response;
                try {
                    response = clientOperator.assemble(resultStr);
                    Intent intent = UAFIntent.getUAFOperationResultIntent(getComponentName().flattenToString(), UAFClientError.NO_ERROR, new UAFMessage(response).toJson());
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (ASMException e) {
                    Snackbar.make(coordinator, ASMException.class.getSimpleName() + ":" + e.statusCode, Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private void doConfirm() {
        try {
            if (message != null && message.length() > 0) {
                processOp(message);
            }
        } catch (Exception e) {
            StatLog.printLog(TAG, "Not able to get registration response" + e.getMessage());
        }
    }

    private void processOp(String inUafOperationMsg) {
        String inMsg = extract(inUafOperationMsg);
        clientOperator = ClientOperator.parseMessage(this, inMsg, channelBinding);
        clientOperator.handle();
    }

    private String extract(String inMsg) {
        UAFMessage uafMessage = new UAFMessage();
        uafMessage.loadFromJson(inMsg);
        return uafMessage.uafProtocolMessage;
    }
}
