package com.jin.fidoclient.asm.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
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

    private String message;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asm);
        Bundle extras = this.getIntent().getExtras();
        message = extras.getString(ASMIntent.MESSAGE_KEY);

        initView();
        initData();
    }

    private void initView() {
        tvInfo = (TextView) findViewById(R.id.tv_info);
        findViewById(R.id.btn_do).setOnClickListener(this);
    }

    private void initData() {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String type = jsonObject.getString(ASMOperator.TYPE_KEY);
            if (type.equals(Request.Register.name())) {
                String registerInStr = jsonObject.getString(ASMOperator.ARGS_KEY);
                RegisterIn registerIn = new Gson().fromJson(registerInStr, RegisterIn.class);
                tvInfo.setText(String.format(getString(R.string.reg_request), registerIn.username));
            } else if (type.equals(Request.Authenticate.name())) {
                tvInfo.setText(getString(R.string.auth_request));
            }
        } catch (JSONException e) {
        }


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_do) {
            selectTouchId();
        }
    }

    private void processOp(int touchId) {
        String result = ASMOperator.parseMessage(touchId, message).handle();
        Intent intent = ASMIntent.getASMOperationResultIntent(result);
        setResult(RESULT_OK, intent);
        finish();
    }

    void selectTouchId() {
        new AlertDialog.Builder(this)
                .setItems(getResources().getStringArray(R.array.touch_ids),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                processOp(which);
                            }
                        }).show();
    }
}
