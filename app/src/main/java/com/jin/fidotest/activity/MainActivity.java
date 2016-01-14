package com.jin.fidotest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidotest.R;
import com.jin.fidotest.data.User;

public class MainActivity extends BaseLoadActivity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_DISCOVER = 1;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_DISCOVER) {
                Bundle bundle = data.getExtras();
                String discoveryData = bundle.getString(UAFIntent.DISCOVERY_DATA_KEY);
                String componentName = bundle.getString(UAFIntent.COMPONENT_NAME_KEY);
                Log.d(TAG, discoveryData + " " + componentName);
            }
        }
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
