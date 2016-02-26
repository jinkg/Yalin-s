package com.jin.fidotest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jin.fidotest.R;
import com.jin.fidotest.fragments.SettingFragment;


/**
 * Created by YaLin on 2015/12/23.
 */
public class SettingsActivity extends BaseActivity implements SettingFragment.SettingItemClickCallback {
    private View rootCoordinator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        overridePendingTransition(0, 0);
        initView();
        switchContent();
    }

    private void initView() {
        rootCoordinator = findViewById(R.id.root_coordinator);
    }

    private void switchContent() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rl_content, SettingFragment.getInstance(this, rootCoordinator))
                .commit();
    }


    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SETTINGS;
    }

    @Override
    public void onPersonalInfoClicked() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportFragmentManager().popBackStack();
    }


}
