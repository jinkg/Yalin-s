package com.jin.fidotest.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidoclient.msg.DeRegistrationRequest;
import com.jin.fidoclient.msg.RegistrationResponse;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.utils.StatLog;
import com.jin.fidotest.R;
import com.jin.fidotest.adapter.RegRecordAdapter;
import com.jin.fidotest.data.User;
import com.jin.fidotest.net.GetRequest;
import com.jin.fidotest.net.NetService;
import com.jin.fidotest.net.PostRequest;
import com.jin.fidotest.net.RequestQueueHelper;
import com.jin.fidotest.net.response.DeRegResponse;
import com.jin.fidotest.net.response.FinishRegResponse;
import com.jin.fidotest.net.response.StartRegResponse;

import java.util.List;

/**
 * Created by YaLin on 2016/1/14.
 */
public class RegRecordActivity extends BaseLoadActivity implements RegRecordAdapter.OnRecordItemClickListener {
    private static final String TAG = RegRecordActivity.class.getSimpleName();
    private static final int REQUEST_REG = 2;
    private static final int REQUEST_DEREG = 4;

    private View rootCoordinator;
    private ViewStub vsNotLogin;
    private ViewStub vsEmpty;
    private RecyclerView rvRecords;

    private Button emptyBtnLogin;
    private Button emptyAddDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_records);
        overridePendingTransition(0, 0);

        initView();
        initData();
    }

    private void initView() {
        rootCoordinator = findViewById(R.id.root_coordinator);
        vsNotLogin = (ViewStub) findViewById(R.id.vs_not_login);
        vsEmpty = (ViewStub) findViewById(R.id.vs_empty);
        rvRecords = (RecyclerView) findViewById(R.id.rv_records);
        rvRecords.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        if (User.isLogin(getApplicationContext())) {
            vsNotLogin.setVisibility(View.GONE);
            String username = User.getUsername(getApplicationContext());
            List<RegRecord> regRecords = UAFClientApi.getRegRecords(username);
            boolean empty = regRecords == null || regRecords.size() == 0;
            if (empty) {
                if (emptyAddDevice == null) {
                    emptyAddDevice = (Button) vsEmpty.inflate().findViewById(R.id.empty_btn_add_device);
                    emptyAddDevice.setOnClickListener(this);
                }
                vsEmpty.setVisibility(View.VISIBLE);
            } else {
                vsEmpty.setVisibility(View.GONE);
                RegRecordAdapter adapter = new RegRecordAdapter(this, regRecords, this);
                rvRecords.setAdapter(adapter);
            }
        } else {
            if (emptyBtnLogin == null) {
                emptyBtnLogin = (Button) vsNotLogin.inflate().findViewById(R.id.empty_btn_login);
                emptyBtnLogin.setOnClickListener(this);
            }
            vsEmpty.setVisibility(View.GONE);
            vsNotLogin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_EXPLORE;
    }

    @Override
    protected void onLoginStatChanged() {
        super.onLoginStatChanged();
        initData();
    }

    @Override
    public void onItemClicked(RegRecord item) {
        showConfirmAction(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_DEREG) {
                initData();
                Snackbar.make(rootCoordinator, R.string.dereg_success, Snackbar.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_REG) {
                Bundle bundle = data.getExtras();
                String message = bundle.getString(UAFIntent.MESSAGE_KEY);

                Gson gson = new Gson();
                UAFMessage uafMessage = gson.fromJson(message, UAFMessage.class);
                RegistrationResponse[] registrationResponses = gson.fromJson(uafMessage.uafProtocolMessage, RegistrationResponse[].class);
                finishReg(registrationResponses);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.empty_btn_add_device:
                String username = User.getUsername(getApplicationContext());
                startReg(username);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void deReg(RegRecord regRecord) {
        showLoading();
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getApplicationContext());
        PostRequest<DeRegResponse> request = new PostRequest<>(NetService.DEREG_URL, DeRegResponse.class, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        deRegSuccess((DeRegResponse) response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestError(error.getMessage());
                    }
                }
        );
        DeRegistrationRequest[] deRegistrationRequests = UAFClientApi.getDeRegistrationRequests(regRecord);
        request.setBody(new Gson().toJson(deRegistrationRequests));
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private void startReg(String username) {
        showLoading();
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getApplicationContext());
        GetRequest<StartRegResponse> request = new GetRequest<>(NetService.getStartRegUrl(username, null), StartRegResponse.class,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        startRegSuccess((StartRegResponse) response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestError(error.getMessage());
                    }
                }
        );
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private void finishReg(RegistrationResponse[] registrationResponses) {
        showLoading();
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getApplicationContext());
        PostRequest<FinishRegResponse> request = new PostRequest<>(NetService.FINISH_REG_URL, FinishRegResponse.class, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        finishRegSuccess((FinishRegResponse) response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestError(error.getMessage());
                    }
                }
        );

        StatLog.printLog(TAG, "finish reg:" + new Gson().toJson(registrationResponses));
        request.setBody(new Gson().toJson(registrationResponses));
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private void startRegSuccess(StartRegResponse response) {
        dismissLoading();
        UAFClientApi.doOperation(this, REQUEST_REG, response.toJson(), "channel");
    }

    private void finishRegSuccess(FinishRegResponse response) {
        dismissLoading();
        initData();
        Snackbar.make(rootCoordinator, R.string.reg_success, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void deRegSuccess(DeRegResponse deRegResponse) {
        dismissLoading();
        UAFClientApi.doOperation(this, REQUEST_DEREG, deRegResponse.toJson(), null);
    }

    private void requestError(String msg) {
        dismissLoading();
        Snackbar.make(rootCoordinator, msg, Snackbar.LENGTH_SHORT)
                .show();
    }

    protected void showConfirmAction(final RegRecord regRecord) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format(getString(R.string.dereg_title), regRecord.biometricsId));
        builder.setMessage(getString(R.string.dereg_prompt));
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deReg(regRecord);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        builder.create().show();
    }


}
