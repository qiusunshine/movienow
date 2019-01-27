package com.dyh.browser.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.dyh.browser.bean.homeBean;
import com.dyh.browser.plugin.VipManager;
import com.dyh.browser.util.WebUtil;
import com.dyh.movienow.R;
import com.dyh.movienow.base.BaseActivity;
import com.dyh.movienow.constants.Auto;
import com.dyh.movienow.constants.VersionTag;
import com.dyh.movienow.core.player.DetectUrlUtil;
import com.dyh.movienow.core.player.PlayChooser;
import com.dyh.movienow.ui.setting.util.FileUtil;
import com.dyh.movienow.util.BackupUtil;
import com.dyh.movienow.util.FileUtils;
import com.dyh.movienow.util.Helper;
import com.dyh.movienow.util.NotifyUtil;
import com.dyh.movienow.util.PreferenceMgr;
import com.dyh.movienow.util.ShareUtil;
import com.dyh.movienow.util.ToastMgr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.qqtheme.framework.picker.FilePicker;
import cn.refactor.lib.colordialog.ColorDialog;

/**
 * 作者：By hdy
 * 日期：On 2018/6/17
 * 时间：At 11:19
 */

public class BookmarkActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private List<homeBean> list = new ArrayList<>();
    private BookmarkAdapter adapter;
    private List<homeBean> websListBean = new ArrayList<>();
    private int realWebStartPos = 0;

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.browser_ac_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_right_top, menu);
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
        setTitle("我的书签");
        recyclerView = findView(R.id.home_recy);
        String web = getIntent().getStringExtra("addWeb");
        if (!TextUtils.isEmpty(web)) {
            addWeb(web);
        }
    }

    private void addWebs(final String webs) {
        ColorDialog colorDialog = new ColorDialog(getContext());
        colorDialog.setTheTitle("视频源一键导入")
                .setContentText("确定导入全部视频源？已经存在的网站不会重复导入")
                .setPositiveListener("确定", new ColorDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ColorDialog dialog) {
                        try {
                            saveWebs(webs);
                            ToastMgr.toastShortBottomCenter(getContext(), "导入成功！");
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastMgr.toastShortBottomCenter(getContext(), "出错：" + e.toString());
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeListener("取消", new ColorDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ColorDialog dialog) {
                        dialog.dismiss();
                    }
                });
        colorDialog.setCancelable(true);
        colorDialog.show();
    }

    private void saveWebs(String webs) {
        try {
            List<homeBean> beans = JSON.parseArray(webs, homeBean.class);
            out:
            for (homeBean bean : beans) {
                for (int i = 0; i < websListBean.size(); i++) {
                    if (websListBean.get(i).getUrl().equals(bean.getUrl())) {
                        continue out;
                    }
                }
                websListBean.add(bean);
                bean.setDrawableId(0);
                adapter.getList().add(adapter.getList().size() - 1, bean);
                adapter.notifyDataSetChanged();
            }
            FileUtils.write(getContext(), "webList.txt", JSON.toJSONString(websListBean));
        } catch (Exception e) {
            e.printStackTrace();
            ToastMgr.toastShortBottomCenter(getContext(),"失败！" + e.getMessage());
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        getList();
        adapter = new BookmarkAdapter(getContext(), list);
        adapter.setOnItemClickListener(new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void onClick(final View view, int position, int type) {
                if (type == 1) {
                    addWeb(null);
                } else {
                    chooseClickOption(list.get(position).getTitle(), list.get(position).getUrl());
                }
            }

            @Override
            public void onLongClick(final String title, final String url, int type, final int position) {
                if (type == 0) {
                    chooseLongClickOption(title, url, position);
                }
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        String webs = getIntent().getStringExtra("webs");
        if (!TextUtils.isEmpty(webs)) {
            addWebs(webs);
        }
        loadUrlFromClipboard();
        VipManager.getInstance();
        NotifyUtil.showDialog(getContext(), VersionTag.BOOKMARK);
    }

    private void chooseLongClickOption(final String title, final String url, final int position){
        String[] titles = new String[]{"置顶书签","删除书签","设为首页","分享书签","取消操作"};
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setItems(titles, new DialogInterface.OnClickListener() { //content
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                topTheWeb(title, url, position);
                                break;
                            case 1:
                                updateWebs(true, title, url);
                                adapter.getList().remove(position);
                                adapter.notifyDataSetChanged();
                                ToastMgr.toastShortCenter(getContext(), "删除成功");
                                dialog.dismiss();
                                break;
                            case 2:
                                PreferenceMgr.put(getContext(), "home", url);
                                ToastMgr.toastShortCenter(getContext(), "设置成功，下次生效");
                                break;
                            case 3:
                                List<homeBean> bean= new ArrayList<>();
                                bean.add(list.get(position));
                                Helper.copyToClipboard(getContext(),Auto.WEBS + JSON.toJSONString(bean));
                                ToastMgr.toastShortCenter(getContext(), "已经复制到剪贴板");
                                break;
                        }
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
        if(dialog.getWindow()!=null){
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            int width = Helper.getScreenWidth(getContext());
            if(width>0){
                lp.width = 2 * width/3;
                dialog.getWindow().setAttributes(lp);
            }
        }
    }

    private void chooseClickOption(String title, String url) {
        if (DetectUrlUtil.isVideoSimple(url)>=0) {
            PlayChooser.startPlayer(getContext(), title, url);
        } else {
            WebUtil.goWeb(getContext(), url, null);
        }
    }

    private void loadUrlFromClipboard() {
        final String shareText = Helper.getTextFromClipBoard(getContext());
        if (shareText.startsWith("http")) {
            Snackbar.make(recyclerView, "是否访问剪贴板的链接", Snackbar.LENGTH_LONG)
                    .setAction("访问", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            WebUtil.goWeb(getContext(), shareText, null);
                        }
                    }).show();
        }
    }

    public void getList() {
//        String[] titles = {"爱奇艺", "腾讯视频", "优酷",
//                "乐视", "芒果TV", "搜狐视频", "PPTV"};
//        String[] urls = {"http://www.iqiyi.com/", "http://m.v.qq.com/", "http://www.youku.com/",
//                "http://m.le.com/", "https://m.mgtv.com/", "https://m.tv.sohu.com/", "http://m.pptv.com/"};
//        int[] drawableIds = {R.drawable.iqiyi, R.drawable.qqlogo, R.drawable.youkulogo,
//                R.drawable.letvlogo, R.drawable.hunantvlogo, R.drawable.sohulogo, R.drawable.pptv};
//        for (int i = 0; i < titles.length; i++) {
//            homeBean homeBean = new homeBean();
//            homeBean.setTitle(titles[i]);
//            homeBean.setDrawableId(drawableIds[i]);
//            homeBean.setUrl(urls[i]);
//            list.add(homeBean);
//        }
        realWebStartPos = list.size();
        List<homeBean> homeBeanList = getWebFromStorage();
        if (homeBeanList != null) {
            list.addAll(homeBeanList);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                shareAllWebs();
                break;
            case R.id.action_home:
                PreferenceMgr.put(getContext(), "home", "hot");
                ToastMgr.toastShortBottomCenter(getContext(), "已经清除首页设置，恢复默认");
                break;
            case R.id.action_import_local:
                importBookmark();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void importBookmark() {
        FilePicker filePicker = new FilePicker(BookmarkActivity.this, FilePicker.FILE);
        filePicker.setBackgroundColor(0xffF5F5F5);
        filePicker.setTopBackgroundColor(0xffF5F5F5);
        filePicker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
            @Override
            public void onFilePicked(String s) {
                if (!TextUtils.isEmpty(s)) {
                    String bookmarks = FileUtil.fileToString(s);
                    saveWebs(bookmarks);
                    ToastMgr.toastShortCenter(getContext(),"书签导入成功！");
                }else {
                    ToastMgr.toastShortCenter(getContext(),"路径不能为空！");
                }
            }
        });
        filePicker.show();
        filePicker.getSubmitButton().setText("系统文件选择器");
        filePicker.getSubmitButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                filePicker.dismiss();
                ToastMgr.toastShortBottomCenter(getContext(),"暂不支持！");
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");//无类型限制
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 400);
            }
        });
    }

    private void shareAllWebs() {
        byte[] json = JSON.toJSONBytes(websListBean);
        try {
            final String finalFilePath = BackupUtil.backupToFile("bookmark", "rules",  json, true);
            if (TextUtils.isEmpty(finalFilePath)) {
                ToastMgr.toastShortBottomCenter(getContext(), "保存书签失败！");
            }
            ToastMgr.toastShortBottomCenter(getContext(), "书签已成功保存到" + finalFilePath);
            try {
                ShareUtil.findChooserToSend(getContext(), finalFilePath);
            } catch (final Exception e) {
                e.printStackTrace();
                //延迟3秒避免Toast频繁就不显示
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ToastMgr.toastShortBottomCenter(getContext(), "文件分享失败！" + e.toString());
                    }
                }, 3000);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ToastMgr.toastShortBottomCenter(getContext(), e.toString());
        }
    }

    private List<homeBean> getWebFromStorage() {
        String webs;
        String path = "webList.txt";
        try {
            if (FileUtils.exist(getContext(), path)) {
                webs = FileUtils.read(getContext(), path);
                if (TextUtils.isEmpty(webs)) {
                    webs = initWebs();
                }
            } else {
                webs = initWebs();
            }
        } catch (IOException e) {
            e.printStackTrace();
            webs = initWebs();
        }
        if (TextUtils.isEmpty(webs)) {
            return null;
        }
        try {
            if (webs.startsWith("[") || webs.startsWith("{")) {
                websListBean = JSON.parseArray(webs, homeBean.class);
            } else {
                websListBean = new ArrayList<>();
                FileUtils.write(getContext(), webs, "");
            }
        } catch (Exception ignored) {
            websListBean = new ArrayList<>();
            FileUtils.write(getContext(), webs, "");
        }
        List<homeBean> list = new ArrayList<>(websListBean);
        homeBean bean = new homeBean();
        bean.setTitle("添加书签");
        bean.setUrl("fang");
        bean.setDrawableId(1);
        list.add(bean);
        return list;
    }

    private String initWebs() {
        List<homeBean> list = new ArrayList<>();
        homeBean home = new homeBean();
        home.setTitle("豌豆影视");
        home.setUrl("http://www.wandouys.com");
        home.setDrawableId(0);
        list.add(home);
        homeBean home1 = new homeBean();
        home1.setTitle("乐猫TV");
        home1.setUrl("http://www.30ts.com");
        home1.setDrawableId(0);
        list.add(home1);
        String webs = JSON.toJSONString(list);
        FileUtils.write(getContext(), "webList.txt", webs);
        return webs;
    }

    private void updateWebs(boolean isRm, String title, String url) {
        if (isRm) {
            for (int i = 0; i < websListBean.size(); i++) {
                if (websListBean.get(i).getTitle().equals(title)
                        && websListBean.get(i).getUrl().equals(url)) {
                    websListBean.remove(i);
                    FileUtils.write(getContext(), "webList.txt", JSON.toJSONString(websListBean));
                    return;
                }
            }
        } else {
            homeBean bean = new homeBean();
            bean.setTitle(title);
            bean.setUrl(url);
            bean.setDrawableId(0);
            websListBean.add(bean);
            FileUtils.write(getContext(), "webList.txt", JSON.toJSONString(websListBean));
        }
    }

    private void topTheWeb(String title, String url, int pos) {
        for (int i = 0; i < websListBean.size(); i++) {
            if (websListBean.get(i).getTitle().equals(title)
                    && websListBean.get(i).getUrl().equals(url)) {
                websListBean.add(0, websListBean.remove(i));
                adapter.getList().add(realWebStartPos,adapter.getList().remove(pos));
                adapter.notifyDataSetChanged();
                ToastMgr.toastShortCenter(getContext(), "已经把" + title + "置顶");
                FileUtils.write(getContext(), "webList.txt", JSON.toJSONString(websListBean));
                return;
            }
        }
    }

    private void addWeb(@Nullable String addWeb) {
        final View view1 = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_web_add, null, false);
        final EditText titleE = view1.findViewById(R.id.web_add_title);
        final EditText urlE = view1.findViewById(R.id.web_add_url);
        titleE.setHint("网站名称（支持直播源）");
        urlE.setHint("网站地址（支持视频地址）");
        if (addWeb != null) {
            String[] detail = addWeb.split("￥");
            if (detail.length == 2) {
                titleE.setText(detail[0]);
                urlE.setText(detail[1]);
            }
        }
        new AlertDialog.Builder(getContext())
                .setTitle(addWeb != null ? "保存来自剪贴板的聚合源" : "保存书签")
                .setView(view1)
                .setCancelable(true)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String title = titleE.getText().toString();
                        String url = urlE.getText().toString();
                        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
                            ToastMgr.toastShortCenter(getContext(), "请输入完整信息");
                        } else {
                            for (int i = 0; i < websListBean.size(); i++) {
                                if (websListBean.get(i).getTitle().equals(title)
                                        && websListBean.get(i).getUrl().equals(url)) {
                                    ToastMgr.toastShortCenter(getContext(), "请勿重复添加！");
                                    return;
                                }
                            }
                            updateWebs(false, title, url);
                            homeBean bean = new homeBean();
                            bean.setTitle(title);
                            bean.setUrl(url);
                            bean.setDrawableId(0);
                            adapter.getList().add(adapter.getList().size() - 1, bean);
                            adapter.notifyDataSetChanged();
                            ToastMgr.toastShortCenter(getContext(), "保存成功");
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

}
