package com.dyh.browser.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.dyh.browser.plugin.HostManager;
import com.dyh.browser.util.WebUtil;
import com.dyh.movienow.R;
import com.dyh.movienow.base.BaseActivity;
import com.dyh.movienow.constants.Auto;
import com.dyh.movienow.util.Helper;
import com.dyh.movienow.util.ShareUtil;
import com.dyh.movienow.util.ToastMgr;

import java.util.List;

import cn.refactor.lib.colordialog.ColorDialog;

/**
 * 作者：By hdy
 * 日期：On 2018/6/17
 * 时间：At 11:19
 */

public class HostActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private HostAdapter adapter;

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.browser_ac_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.host_right_top, menu);
        return true;
    }

    @Override
    protected void initView() {
        try {
            setSupportActionBar((Toolbar) findView(R.id.home_toolbar));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle("广告拦截");
        recyclerView = findView(R.id.home_recy);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        adapter = new HostAdapter(getContext());
        adapter.setOnItemClickListener(new HostAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position, String url) {
                if (!url.startsWith("http")) {
                    url = "http://" + url;
                }
                WebUtil.goWeb(getContext(), url, 2);
            }

            @Override
            public void onLongClick(String title, final String url) {
                ColorDialog colorDialog = new ColorDialog(getContext());
                colorDialog.setTheTitle("温馨提示")
                        .setContentText("确认删除该拦截网址吗？删除后无法恢复，可以复制网址分享给别人，点击空白处取消")
                        .setPositiveListener("删除", new ColorDialog.OnPositiveListener() {
                            @Override
                            public void onClick(ColorDialog dialog) {
                                HostManager.getInstance().delete(url);
                                adapter.notifyDataSetChanged();
                                ToastMgr.toastShortCenter(getContext(), "删除成功");
                                dialog.dismiss();
                            }
                        })
                        .setNegativeListener("复制", new ColorDialog.OnNegativeListener() {
                            @Override
                            public void onClick(ColorDialog dialog) {
                                ShareUtil.copyToClipboard(getContext(), url);
                                ToastMgr.toastShortCenter(getContext(), "复制成功");
                                dialog.dismiss();
                            }
                        });
                colorDialog.setCancelable(true);
                colorDialog.show();
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        String sss = getIntent().getStringExtra("ads");
        if(!TextUtils.isEmpty(sss)){
            showAdd(sss);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                shareAds();
                break;
            case R.id.action_add:
                showAdd(null);
                break;
            case R.id.action_delete:
                HostManager.getInstance().deleteAll();
                adapter.notifyDataSetChanged();
                ToastMgr.toastShortBottomCenter(getContext(),"已删除全部拦截规则");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAdd(@Nullable String text) {
        final EditText et = new EditText(getContext());
        et.setHint("添加多个请用; 隔开（英文分号加空格）");
        if(text!=null){
            et.setText(text);
        }
        new AlertDialog.Builder(getContext()).setTitle("添加拦截网址或者域名")
                .setView(et)
                .setCancelable(true)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (TextUtils.isEmpty(input)) {
                            ToastMgr.toastShortCenter(getContext(), "网址不能为空");
                        } else {
                            String[] ss = input.split("; ");
                            for (String s : ss) {
                                HostManager.getInstance().addUrl(s);
                            }
                            adapter.notifyDataSetChanged();
                            ToastMgr.toastShortBottomCenter(getContext(), "已成功添加" + ss.length + "条网址");
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void shareAds() {
        List<String> ads = HostManager.getInstance().getUrlList();
        if(ads.size()<1){
            ToastMgr.toastShortBottomCenter(getContext(),"没有拦截规则哦");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ads.size()-1; i++) {
            sb.append(ads.get(i)).append("; ");
        }
        sb.append(ads.get(ads.size()-1));
        String text = sb.toString();
        text = Auto.AD + text;
        Helper.copyToClipboard(getContext(),text);
        ToastMgr.toastShortBottomCenter(getContext(),"已经复制全部拦截规则");
    }
}
