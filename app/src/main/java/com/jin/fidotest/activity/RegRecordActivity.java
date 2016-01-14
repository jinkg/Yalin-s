package com.jin.fidotest.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidotest.R;
import com.jin.fidotest.adapter.RegRecordAdapter;
import com.jin.fidotest.data.User;

import java.util.List;

/**
 * Created by YaLin on 2016/1/14.
 */
public class RegRecordActivity extends BaseLoadActivity {
    private View rootCoordinator;
    private ViewStub vsNotLogin;
    private ViewStub vsEmpty;
    private RecyclerView rvRecords;

    private Button emptyBtnLogin;

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
                vsEmpty.setVisibility(View.VISIBLE);
            } else {
                vsEmpty.setVisibility(View.GONE);
                RegRecordAdapter adapter = new RegRecordAdapter(this, regRecords);
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
}
