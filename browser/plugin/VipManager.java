package com.dyh.browser.plugin;

import android.os.Environment;
import android.text.TextUtils;

import com.dyh.movienow.ui.setting.util.FileUtil;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.finalteam.okhttpfinal.HttpRequest;
import cn.finalteam.okhttpfinal.JsonHttpRequestCallback;
import cn.finalteam.okhttpfinal.RequestParams;

/**
 * 作者：By hdy
 * 日期：On 2018/8/24
 * 时间：At 22:30
 */

public class VipManager {
    private volatile static VipManager sInstance;
    private List<PowerMenuItem> itemList = new ArrayList<>();
    private List<String> urlList = new ArrayList<>();

    private VipManager() {
        loadJs();
    }

    public static VipManager getInstance() {
        if (sInstance == null) {
            synchronized (VipManager.class) {
                if (sInstance == null) {
                    sInstance = new VipManager();
                }
            }
        }
        return sInstance;
    }

    private void loadJs() {
        boolean exist = false;
        try {
            exist = loadJsFromFileIfExist();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(exist){
            return;
        }
        RequestParams params = new RequestParams();
        params.addHeader("X-Bmob-Application-Id", "58c79a9de526c3f4f1ec6025f0f4506e");
        params.addHeader("X-Bmob-REST-API-Key", "d522c3d4e999a1b208b72f445735a4d9");
        HttpRequest.get("https://api.bmob.cn/1/classes/vipjs/MmJY444A", params, new JsonHttpRequestCallback() {
            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
            }

            @Override
            protected void onSuccess(final com.alibaba.fastjson.JSONObject jsonObject) {
                String js = jsonObject.getString("titles");
                String[] urls = js.split("; ");
                for (int i = 0; i < urls.length; i++) {
                    PowerMenuItem item = new PowerMenuItem(urls[i], false);
                    itemList.add(item);
                }
                String jss = jsonObject.getString("urls");
                String[] urls2 = jss.split("; ");
                urlList.addAll(Arrays.asList(urls2));
                StringBuilder builder = new StringBuilder("//‘//’是注释，每行注释都要加‘//’");
                builder.append("\n").append("//一行一个解析接口，修改后重启方圆才能生效！")
                        .append("\n").append("//一个解析接口中间以！英文！分号加一个空格隔开，如‘方圆解析; http://fy.com/?url=**’")
                        .append("\n").append("//解析链接的地方用‘**’代表");
                for (int i = 0; i < Math.min(urlList.size(), itemList.size()); i++) {
                    builder.append("\n").append(urls[i]).append("; ").append(urls2[i]);
                }
                try {
                    saveJsToFile(builder.toString().getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onSuccess(jsonObject);
            }
        });
    }
    private void saveJsToFile(byte[] text) {
        String path = Environment.getExternalStorageDirectory() + File.separator + "FangYuan" + File.separator + "rules";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String finalFilePath = path + File.separator + "vip.txt";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(finalFilePath);
            fos.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private boolean loadJsFromFileIfExist() {
        String path = Environment.getExternalStorageDirectory() + File.separator + "FangYuan" + File.separator + "rules";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String finalFilePath = path + File.separator + "vip.txt";
        File file = new File(finalFilePath);
        if (!file.exists()) {
            return false;
        }
        byte[] bytes = FileUtil.fileToBytes(finalFilePath);
        if (bytes == null) {
            return false;
        }
        String text = new String(bytes);
        if (TextUtils.isEmpty(text)) {
            return true;
        }
        String[] texts = text.split("\n");
        for (int i = 0; i < texts.length; i++) {
            if (TextUtils.isEmpty(texts[i]) || texts[i].startsWith("//")) {
                continue;
            }
            String[] rules = texts[i].split("; ");
            if (rules.length < 2 || !rules[1].startsWith("http")) {
                continue;
            }
            PowerMenuItem item = new PowerMenuItem(rules[0], false);
            itemList.add(item);
            urlList.add(rules[1]);
        }
        return true;
    }

    public List<PowerMenuItem> getItemList() {
        return itemList;
    }

    public void setSelect(int pos) {
        for (int i = 0; i < itemList.size(); i++) {
            if (i == pos) {
                itemList.get(i).setIsSelected(true);
            } else {
                itemList.get(i).setIsSelected(false);
            }
        }
    }

    public List<String> getUrlList() {
        return urlList;
    }
}
