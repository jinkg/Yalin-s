package com.jin.fidotest.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jin.fidoclient.utils.StatLog;
import com.jin.fidotest.R;
import com.jin.fidotest.data.User;

public class MainActivity extends BaseLoadActivity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView tvWelcome;
    private View rootCoordinator;
    private ViewStub vsNotLogin;

    private Button emptyBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        vsNotLogin = (ViewStub) findViewById(R.id.vs_not_login);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        rootCoordinator = findViewById(R.id.root_coordinator);
    }

    private void initData() {
        if (User.isLogin(getApplicationContext())) {
            vsNotLogin.setVisibility(View.GONE);
            String username = User.getUsername(getApplicationContext());
            tvWelcome.setText(String.format(getString(R.string.welcome), username));
        } else {
            if (emptyBtnLogin == null) {
                emptyBtnLogin = (Button) vsNotLogin.inflate().findViewById(R.id.empty_btn_login);
                emptyBtnLogin.setOnClickListener(this);
            }
            vsNotLogin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onLoginStatChanged() {
        super.onLoginStatChanged();
        initData();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MAIN;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                super.onClick(v);
                break;
        }
    }
}
