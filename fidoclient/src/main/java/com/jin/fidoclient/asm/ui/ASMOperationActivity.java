package com.jin.fidoclient.asm.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jin.fidoclient.R;
import com.jin.fidoclient.asm.api.ASMIntent;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.op.ASMOperator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YaLin on 2016/1/13.
 */
public class ASMOperationActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvInfo;
    private Button btnDo;

    private String message;

    private Request OpType;

    private FingerprintAuthenticationDialogFragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asm);
        Bundle extras = this.getIntent().getExtras();
        message = extras.getString(ASMIntent.MESSAGE_KEY);

        initView();
        initData();
        showFingerprint();
    }

    private void initView() {
        tvInfo = (TextView) findViewById(R.id.tv_info);
        btnDo = (Button) findViewById(R.id.btn_do);
        btnDo.setOnClickListener(this);
    }

    private void initData() {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String type = jsonObject.getString(ASMOperator.TYPE_KEY);
            if (type.equals(Request.Register.name())) {
                OpType = Request.Register;
                String registerInStr = jsonObject.getString(ASMOperator.ARGS_KEY);
                RegisterIn registerIn = new Gson().fromJson(registerInStr, RegisterIn.class);
                tvInfo.setText(String.format(getString(R.string.reg_request), registerIn.username));
            } else if (type.equals(Request.Authenticate.name())) {
                OpType = Request.Authenticate;
                tvInfo.setText(getString(R.string.auth_request));
            } else if (type.equals(Request.Deregister.name())) {
                OpType = Request.Deregister;
                tvInfo.setText(getString(R.string.dereg_request));
            }
        } catch (JSONException e) {
        }

        if (OpType == Request.Deregister) {
            btnDo.setEnabled(false);
            String result = ASMOperator.parseMessage(null, message).handle();
            Intent intent = ASMIntent.getASMOperationResultIntent(result);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void showFingerprint() {
        mFragment = new FingerprintAuthenticationDialogFragment();
        mFragment.setStage(
                FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
        mFragment.show(getFragmentManager(), ASMOperationActivity.class.getSimpleName());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_do) {
            showFingerprint();
        }
    }

    public void fingerprintComplete(String fingerId) {
        processOp(fingerId);
    }

    private void processOp(String touchId) {
        String result = ASMOperator.parseMessage(touchId, message).handle();
        Intent intent = ASMIntent.getASMOperationResultIntent(result);
        setResult(RESULT_OK, intent);
        finish();
    }

    void selectTouchId() {
        final String[] ids = getResources().getStringArray(R.array.touch_ids);
        new AlertDialog.Builder(this)
                .setItems(ids,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                processOp(ids[which]);
                            }
                        }).show();
    }
}
