package com.jin.fidoclient.asm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
public class ASMOperationActivity extends AppCompatActivity implements View.OnClickListener, ASMOperator.HandleResultCallback {
    private TextView tvInfo;
    private Button btnDo;

    private String message;

    private Request OpType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asm);
        Bundle extras = this.getIntent().getExtras();
        message = extras.getString(ASMIntent.MESSAGE_KEY);

        initView();
        initData();

        processOp();
    }

    private void initView() {
        tvInfo = (TextView) findViewById(R.id.tv_prompt);
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
            } else if (type.equals(Request.GetInfo.name())) {
                OpType = Request.GetInfo;
            }
        } catch (JSONException e) {
        }

        if (OpType == Request.Deregister || OpType == Request.GetInfo) {
            btnDo.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_do) {
            processOp();
        }
    }

    private void processOp() {
        ASMOperator.parseMessage(this, message, this).handle();
    }

    @Override
    public void onHandleResult(String result) {
        Intent intent = ASMIntent.getASMOperationResultIntent(result);
        setResult(RESULT_OK, intent);
        finish();
    }
}
