package com.jin.fidotest.fragments;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.jin.fidotest.data.User;
import com.jin.fidotest.net.NetService;


/**
 * Created by YaLin on 2015/7/24.
 */
public class BaseFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onStart() {
        super.onStart();
        User.getUserSp(getActivity().getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        NetService.getNetSp(getActivity().getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        User.getUserSp(getActivity().getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
        NetService.getNetSp(getActivity().getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (User.USERNAME_KEY.equals(key)) {
            onLoginStateChanged();
        } else if (NetService.HTTP_KEY.equals(key)) {
            onHttpUrlChanged();
        }
    }

    protected void onLoginStateChanged() {
    }

    protected void onHttpUrlChanged() {

    }
}
