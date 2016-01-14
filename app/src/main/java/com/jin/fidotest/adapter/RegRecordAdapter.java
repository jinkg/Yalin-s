package com.jin.fidotest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jin.fidoclient.asm.db.RegRecord;
import com.jin.fidotest.R;

import java.util.List;

/**
 * Created by YaLin on 2016/1/14.
 */
public class RegRecordAdapter extends RecyclerView.Adapter<RegRecordViewHolder> {

    private Context mContext;
    private List<RegRecord> mRecords;

    public RegRecordAdapter(Context context, List<RegRecord> recordList) {
        mContext = context;
        mRecords = recordList;
    }

    @Override
    public RegRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.reg_record_item, null);
        return new RegRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RegRecordViewHolder holder, int position) {
        RegRecord regRecord = mRecords.get(position);
        holder.tvType.setText("touchId");
        holder.tvId.setText(regRecord.touchId);
    }

    @Override
    public int getItemCount() {
        return mRecords == null ? 0 : mRecords.size();
    }
}

class RegRecordViewHolder extends RecyclerView.ViewHolder {
    TextView tvType;
    TextView tvId;

    public RegRecordViewHolder(View itemView) {
        super(itemView);
        tvType = (TextView) itemView.findViewById(R.id.tv_type);
        tvId = (TextView) itemView.findViewById(R.id.tv_id);
    }
}