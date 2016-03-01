package com.jin.fidoclient.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jin.fidoclient.R;
import com.jin.fidoclient.asm.msg.obj.AuthenticatorInfo;

import java.util.List;

/**
 * Created by YaLin on 2016/1/19.
 */
public class AuthenticatorAdapter extends RecyclerView.Adapter<AuthenticatorItemViewHolder> {
    public interface OnAuthenticatorClickCallback {
        void onAuthenticatorClick(AuthenticatorInfo info);
    }

    private final Context mContext;
    private final List<AuthenticatorInfo> mInfos;
    private final OnAuthenticatorClickCallback mCallback;

    public AuthenticatorAdapter(Context context, List<AuthenticatorInfo> infos, OnAuthenticatorClickCallback callback) {
        mContext = context;
        mInfos = infos;
        mCallback = callback;
    }

    @Override
    public AuthenticatorItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.authenticator_item, null);
        return new AuthenticatorItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AuthenticatorItemViewHolder holder, int position) {
        final AuthenticatorInfo info = mInfos.get(position);
        if (TextUtils.isEmpty(info.title)) {
            info.title = mContext.getString(R.string.unknown_device);
        }
        holder.tvTitle.setText(info.title);
        if (TextUtils.isEmpty(info.icon)) {
            if (info.iconRes <= 0) {
                info.iconRes = R.drawable.ic_linked_camera_black_48dp;
                Resources res = mContext.getResources();
                Drawable background = res.getDrawable(info.iconRes);
                int primaryColor = res.getColor(R.color.hint_color);
                background.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                holder.ivIcon.setBackground(background);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_fp_40px);
            }

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onAuthenticatorClick(info);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mInfos == null ? 0 : mInfos.size();
    }
}

class AuthenticatorItemViewHolder extends RecyclerView.ViewHolder {
    ImageView ivIcon;
    TextView tvTitle;

    public AuthenticatorItemViewHolder(View itemView) {
        super(itemView);
        ivIcon = (ImageView) itemView.findViewById(R.id.item_iv_icon);
        tvTitle = (TextView) itemView.findViewById(R.id.item_tv_title);
    }
}
