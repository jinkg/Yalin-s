package com.jin.fidotest.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jin.fidotest.R;
import com.jin.fidotest.data.User;
import com.jin.uitoolkit.fragment.BaseLoadingFragment;
import com.jin.uitoolkit.util.Utils;

/**
 * Created by 雅麟 on 2015/3/22.
 */
public class LoginFragment extends BaseLoadingFragment implements View.OnClickListener {

    public interface LoginSuccessCallback {
        void onLoginSuccess();
    }

    private static final String TAG = LoginFragment.class.getSimpleName();

    View rootCoordinator;
    EditText etUsername;
    EditText etPassword;
    Button btnLogin;

    TextInputLayout tilAccount;
    TextInputLayout tilPassword;


    private LoginSuccessCallback callback;

    public static LoginFragment getInstance(LoginSuccessCallback callback) {
        LoginFragment fragment = new LoginFragment();
        fragment.callback = callback;
        return fragment;
    }

    public static void open(int container, FragmentManager manager, LoginSuccessCallback callback) {
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        rootCoordinator = view.findViewById(R.id.root_coordinator);
        etUsername = (EditText) view.findViewById(R.id.login_et_account);
        etPassword = (EditText) view.findViewById(R.id.login_et_password);
        btnLogin = (Button) view.findViewById(R.id.login_btn_login);
        btnLogin.setOnClickListener(this);
        tilAccount = (TextInputLayout) view.findViewById(R.id.login_til_account);
        tilPassword = (TextInputLayout) view.findViewById(R.id.login_til_password);
    }

    @Override
    public void onStart() {
        super.onStart();
        etPassword.setText(null);
        etUsername.post(new Runnable() {
            @Override
            public void run() {
                etUsername.requestFocus();
            }
        });
    }

    @Override
    protected String getRequestTag() {
        return LoginFragment.class.getName();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn_login:
                Utils.closeInput(getActivity());
                login();
                break;
        }
    }

    private void login() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Snackbar.make(rootCoordinator, R.string.user_is_null, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        loginSuccess(username);
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
