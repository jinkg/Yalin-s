package com.jin.fidotest.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.msg.AuthenticationResponse;
import com.jin.fidoclient.msg.client.UAFMessage;
import com.jin.fidoclient.utils.StatLog;
import com.jin.fidotest.R;
import com.jin.fidotest.activity.AccountActivity;
import com.jin.fidotest.data.User;
import com.jin.fidotest.net.GetRequest;
import com.jin.fidotest.net.NetService;
import com.jin.fidotest.net.PostRequest;
import com.jin.fidotest.net.RequestQueueHelper;
import com.jin.fidotest.net.response.FinishAuthResponse;
import com.jin.fidotest.net.response.StartAuthResponse;

/**
 * Created by YaLin on 2016/1/14.
 */
public class UAFLoginFragment extends BaseLoadingFragment implements View.OnClickListener {
    private static final String TAG = LoginFragment.class.getSimpleName();
    private static final int REQUEST_AUTH = 3;

    View rootCoordinator;

    private LoginFragment.LoginSuccessCallback callback;

    public static UAFLoginFragment getInstance(LoginFragment.LoginSuccessCallback callback) {
        UAFLoginFragment fragment = new UAFLoginFragment();
        fragment.callback = callback;
        return fragment;
    }

    public static void open(int container, FragmentManager manager, LoginFragment.LoginSuccessCallback callback) {
        if (manager.findFragmentByTag(TAG) != null) {
            return;
        }
        manager.beginTransaction().setCustomAnimations(
                R.anim.push_left_in,
                R.anim.push_left_out,
                R.anim.push_right_in,
                R.anim.push_right_out)
                .add(container, getInstance(callback), TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uaf_login, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        rootCoordinator = view.findViewById(R.id.root_coordinator);
        view.findViewById(R.id.uaf_login_btn_login).setOnClickListener(this);
        view.findViewById(R.id.uaf_login_tv_account_login).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected String getRequestTag() {
        return LoginFragment.class.getName();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uaf_login_btn_login:
                startAuth();
                break;
            case R.id.uaf_login_tv_account_login:
                if (getActivity() instanceof AccountActivity) {
                    ((AccountActivity) getActivity()).addContent(AccountActivity.OpenType.Login);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_AUTH) {
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

    private void startAuth() {
        showLoading();
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getActivity().getApplicationContext());
        GetRequest<StartAuthResponse> request = new GetRequest<>(NetService.getStartAuthUrl(null), StartAuthResponse.class,
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
        RequestQueue requestQueue = RequestQueueHelper.getInstance(getActivity().getApplicationContext());
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
        StatLog.printLog(TAG, "finish auth:" + new Gson().toJson(authenticationResponses));
        request.setBody(new Gson().toJson(authenticationResponses));
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private void startAuthSuccess(StartAuthResponse response) {
        if (getActivity() == null) {
            return;
        }
        dismissLoading();
        UAFClientApi.doOperation(this, REQUEST_AUTH, response.toJson(), "channel");
    }

    private void finishAuthSuccess(FinishAuthResponse response) {
        if (getActivity() == null) {
            return;
        }
        loginSuccess(response.data.get(0).username);
    }

    private void requestError(String msg) {
        dismissLoading();
        Snackbar.make(rootCoordinator, msg, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void loginSuccess(String username) {
        User user = new User(username);
        User.storeUserInfo(getActivity().getApplicationContext(), user);
        if (callback != null) {
            callback.onLoginSuccess();
            callback = null;
        }
        if (getActivity() == null) {
            return;
        }
        dismissLoading();
        Snackbar.make(rootCoordinator, R.string.login_success, Snackbar.LENGTH_SHORT)
                .show();
    }
}
