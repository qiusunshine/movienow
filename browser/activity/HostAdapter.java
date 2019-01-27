package com.dyh.browser.activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dyh.browser.plugin.HostManager;
import com.dyh.movienow.R;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

class HostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private OnItemClickListener onItemClickListener;
    HostAdapter(Context context) {
        this.context = context;
    }

    interface OnItemClickListener {
        void onClick(View view, int position, String url);
        void onLongClick(String title, String url);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_host_url, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof HeaderViewHolder) {
            final HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            final String mytitle = HostManager.getInstance().getUrlList().get(holder.getAdapterPosition());
            holder.desc.setText(mytitle);
            holder.desc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(v, holder.getAdapterPosition(), mytitle);
                }
            });
            holder.desc.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onLongClick(mytitle,mytitle);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return HostManager.getInstance().getUrlList().size();
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView desc;
        HeaderViewHolder(View itemView) {
            super(itemView);
            desc = itemView.findViewById(R.id.item_host_title);
        }
    }
}
