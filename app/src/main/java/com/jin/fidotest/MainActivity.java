package com.jin.fidotest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.msg.AuthenticationResponse;
import com.jin.fidoclient.msg.RegistrationResponse;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidotest.net.GetRequest;
import com.jin.fidotest.net.NetService;
import com.jin.fidotest.net.PostRequest;
import com.jin.fidotest.net.RequestQueueHelper;
import com.jin.fidotest.net.response.FinishAuthResponse;
import com.jin.fidotest.net.response.FinishRegResponse;
import com.jin.fidotest.net.response.StartAuthResponse;
import com.jin.fidotest.net.response.StartRegResponse;
import com.jin.uitoolkit.activity.BaseLoadActivity;

public class MainActivity extends BaseLoadActivity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_DISCOVER = 1;
    private static final int REQUEST_REG = 2;
    private static final int REQUEST_AUTH = 3;

    private TextView tvLog;
    private Button btnReg;
    private Button btnAuth;
    private View rootCoordinator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
    }

    private void initView() {
        tvLog = (TextView) findViewById(R.id.tv_log);
        btnReg = (Button) findViewById(R.id.btn_reg);
        btnAuth = (Button) findViewById(R.id.btn_auth);
        btnReg.setOnClickListener(this);
        btnAuth.setOnClickListener(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        rootCoordinator = findViewById(R.id.root_coordinator);
    }

    private void startReg() {
        String username = "jinkg";
        String appId = "yalin.appid";
        showLoading();
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getApplicationContext());
        GetRequest<StartRegResponse> request = new GetRequest<>(NetService.getStartRegUrl(username, appId), StartRegResponse.class,
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
        request.setBody(new Gson().toJson(registrationResponses));
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private void startAuth() {
        String appId = "yalin.appid";
        showLoading();
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getApplicationContext());
        GetRequest<StartAuthResponse> request = new GetRequest<>(NetService.getStartAuthUrl(appId), StartAuthResponse.class,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        startAuthSuccess((StartAuthResponse) response);
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

    private void finishAuth(AuthenticationResponse[] authenticationResponses) {
        showLoading();
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getApplicationContext());
        PostRequest<FinishAuthResponse> request = new PostRequest<>(NetService.FINISH_AUTH_URL, FinishAuthResponse.class, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        finishAuthSuccess((FinishAuthResponse) response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestError(error.getMessage());
                    }
                }
        );
        request.setBody(new Gson().toJson(authenticationResponses));
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private void startRegSuccess(StartRegResponse response) {
        dismissLoading();
        tvLog.setText(response.toJson());
        UAFClientApi.doOperation(MainActivity.this, REQUEST_REG, response.toJson(), "channel");
    }

    private void finishRegSuccess(FinishRegResponse response) {
        dismissLoading();
        tvLog.setText(response.toJson());
        Snackbar.make(rootCoordinator, R.string.reg_success, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void startAuthSuccess(StartAuthResponse response) {
        dismissLoading();
        tvLog.setText(response.toJson());
        UAFClientApi.doOperation(MainActivity.this, REQUEST_AUTH, response.toJson(), "channel");
    }

    private void finishAuthSuccess(FinishAuthResponse response) {
        dismissLoading();
        tvLog.setText(response.data.get(0).username);
        Snackbar.make(rootCoordinator, R.string.auth_success, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void requestError(String msg) {
        dismissLoading();
        Snackbar.make(rootCoordinator, msg, Snackbar.LENGTH_SHORT)
                .show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_DISCOVER) {
                Bundle bundle = data.getExtras();
                String discoveryData = bundle.getString(UAFIntent.DISCOVERY_DATA_KEY);
                String componentName = bundle.getString(UAFIntent.COMPONENT_NAME_KEY);
                Log.d(TAG, discoveryData + " " + componentName);
            } else if (requestCode == REQUEST_REG) {
                Bundle bundle = data.getExtras();
                String message = bundle.getString(UAFIntent.MESSAGE_KEY);
                String componentName = bundle.getString(UAFIntent.COMPONENT_NAME_KEY);

                Gson gson = new Gson();
                UAFMessage uafMessage = gson.fromJson(message, UAFMessage.class);
                RegistrationResponse[] registrationResponses = gson.fromJson(uafMessage.uafProtocolMessage, RegistrationResponse[].class);
                Log.d(TAG, "reg ok message : " + message + " componentName : " + componentName);
                finishReg(registrationResponses);
            } else if (requestCode == REQUEST_AUTH) {
                Bundle bundle = data.getExtras();
                String message = bundle.getString(UAFIntent.MESSAGE_KEY);
                String componentName = bundle.getString(UAFIntent.COMPONENT_NAME_KEY);

                Gson gson = new Gson();
                UAFMessage uafMessage = gson.fromJson(message, UAFMessage.class);
                AuthenticationResponse[] authenticationResponses = gson.fromJson(uafMessage.uafProtocolMessage, AuthenticationResponse[].class);
                Log.d(TAG, "reg ok message : " + message + " componentName : " + componentName);
                finishAuth(authenticationResponses);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reg:
                startReg();
                break;
            case R.id.btn_auth:
                startAuth();
                break;
        }
    }
}
