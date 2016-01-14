package com.jin.fidotest.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jin.fidotest.R;
import com.jin.uitoolkit.util.PixelUtil;


public abstract class BaseLoadingFragment extends BaseFragment {
    private FrameLayout flRoot;

    private View failedView;
    private TextView failedWarningView;
    private View loadingView;
    private Button btnReload;

    ViewStub loadingStub;
    ViewStub failedStub;
    ViewStub failedWarningStub;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        failedView = null;
        failedWarningView = null;
        loadingView = null;
        btnReload = null;
        return setupLoading(createContentView(inflater, container, savedInstanceState));
    }

    public abstract View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    protected abstract String getRequestTag();

    protected View setupLoading(View view) {
        flRoot = new FrameLayout(getActivity());
        failedWarningStub = new ViewStub(getActivity());
        failedWarningStub.setLayoutResource(R.layout.dialog_loading_failed_head);
        LinearLayout llContent = new LinearLayout(getActivity());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.addView(failedWarningStub, LinearLayout.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(getActivity().getApplicationContext(), 40));
        llContent.addView(view);
        flRoot.addView(llContent, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        loadingStub = new ViewStub(getActivity());
        loadingStub.setLayoutResource(R.layout.dialog_content_loading);
        failedStub = new ViewStub(getActivity());
        failedStub.setLayoutResource(R.layout.dialog_loading_failed);

        flRoot.addView(failedStub, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        flRoot.addView(loadingStub, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        flRoot.setLayoutParams(params);
        return flRoot;
    }

    protected void showLoading() {
        if (loadingView == null) {
            loadingView = loadingStub.inflate();
        }
        dismissToLoad();
        loadingView.setVisibility(View.VISIBLE);
    }

    private void dismissToLoad() {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        if (failedView != null) {
            failedView.setVisibility(View.GONE);
        }
    }

    protected void dismissLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        if (failedView != null) {
            failedView.setVisibility(View.GONE);
        }
        if (failedWarningView != null) {
            failedWarningView.setVisibility(View.GONE);
        }
    }

    protected void dismissByFailed(int tag) {
        if (failedView == null) {
            failedView = failedStub.inflate();
            btnReload = (Button) failedView.findViewById(R.id.loading_btn_reload);
            btnReload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload(v);
                }
            });
        }
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        btnReload.setTag(tag);
        failedView.setVisibility(View.VISIBLE);
    }

    protected void failedWithContent(final int tag, String warning) {
        if (failedWarningView == null) {
            failedWarningView = (TextView) failedWarningStub.inflate();
            failedWarningView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload(v);
                }
            });
        }
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        if (failedView != null) {
            failedView.setVisibility(View.GONE);
        }
        failedWarningView.setTag(tag);
        failedWarningView.setText(warning);
        failedWarningView.setVisibility(View.VISIBLE);
    }

    private void reload(View v) {
        int tag = (int) v.getTag();
        onReload(tag);
    }

    protected void onReload(int tag) {

    }


}
