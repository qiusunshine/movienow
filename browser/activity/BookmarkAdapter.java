package com.dyh.browser.activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dyh.browser.bean.homeBean;
import com.dyh.movienow.R;
import com.dyh.movienow.ui.setting.util.RandomUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

class BookmarkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<homeBean> list;
    private OnItemClickListener onItemClickListener;
    private int[] colors = {0xfff5f5f5, 0xfff0fbff, 0xfffef3ef, 0xfff7eeff, 0xf0e6f3e6};

    BookmarkAdapter(Context context, List<homeBean> list) {
        this.context = context;
        this.list = list;
    }

    public List<homeBean> getList() {
        return list;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getDrawableId() == 0) return 1;
        else if (list.get(position).getDrawableId() == 1) return 2;
        else return 0;
    }

    interface OnItemClickListener {
        void onClick(View view, int position, int type);

        void onLongClick(String title, String url, int type, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new WebViewHolder(LayoutInflater.from(context).inflate(R.layout.item_home_web, parent, false));
        } else if (viewType == 2) {
            return new WebViewHolder(LayoutInflater.from(context).inflate(R.layout.item_home_web, parent, false));
        }
        return new HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_home_video, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof HeaderViewHolder) {
            final HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            String mytitle = list.get(holder.getAdapterPosition()).getTitle();
            Glide.with(context)
                    .asDrawable()
                    .load(list.get(holder.getAdapterPosition()).getDrawableId())
                    .into(holder.img);
            holder.desc.setText(mytitle);
            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(v, holder.getAdapterPosition(), 0);
                }
            });
        } else if (viewHolder instanceof WebViewHolder) {
            final WebViewHolder holder = (WebViewHolder) viewHolder;
            String mytitle = list.get(holder.getAdapterPosition()).getTitle();
            holder.web_bottom_title.setText(mytitle);
            holder.web_center_title.setText(mytitle);
            int color = RandomUtil.getRandom(0, colors.length);
            if (color < 0 || color >= colors.length) {
                color = colors.length - 1;
            }
            holder.web_center_bg.setBackgroundColor(colors[color]);
            holder.web_center_bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    onItemClickListener.onClick(v, position, list.get(pos).getDrawableId());
                }
            });
            holder.web_center_bg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getAdapterPosition();
                    onItemClickListener.onLongClick(list.get(pos).getTitle(), list.get(pos).getUrl(), list.get(pos).getDrawableId(), pos);
                    return false;
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView desc;
        ImageView img;

        HeaderViewHolder(View itemView) {
            super(itemView);
            desc = itemView.findViewById(R.id.movie_detail_desc);
            img = itemView.findViewById(R.id.movie_detail_img);
        }
    }

    private class WebViewHolder extends RecyclerView.ViewHolder {
        TextView web_bottom_title;
        TextView web_center_title;
        FrameLayout web_center_bg;

        WebViewHolder(View itemView) {
            super(itemView);
            web_bottom_title = itemView.findViewById(R.id.web_bottom_title);
            web_center_title = itemView.findViewById(R.id.web_center_title);
            web_center_bg = itemView.findViewById(R.id.web_center_bg);
        }
    }
}
