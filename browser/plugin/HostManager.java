package com.dyh.browser.plugin;

import android.text.TextUtils;

import com.dyh.movienow.App;
import com.dyh.movienow.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2018/8/24
 * 时间：At 22:30
 */

public class HostManager {
    private volatile static HostManager sInstance;
    private List<String> urlList = new ArrayList<>();
    private String filePath = "host.txt";
    private String[] notInterceptSources = {".html", ".m3u8", "min.css", ".ico"};
    private String hostText;

    private HostManager() {
        loadUrls();
    }

    public static HostManager getInstance() {
        if (sInstance == null) {
            synchronized (HostManager.class) {
                if (sInstance == null) {
                    sInstance = new HostManager();
                }
            }
        }
        return sInstance;
    }

    private void loadUrls() {
        try {
            if(!FileUtils.exist(App.getContext(), filePath)){
                return;
            }
            hostText = FileUtils.read(App.getContext(), filePath);
            String[] hosts = hostText.split("; ");
            urlList.addAll(Arrays.asList(hosts));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean shouldIntercept(String url) {
        boolean shouldIntercept = true;
        for (int i = 0; i < notInterceptSources.length; i++) {
            if (url.endsWith(notInterceptSources[i])) {
                shouldIntercept = false;
                break;
            }
        }
        if (shouldIntercept) {
            return hasContain(url);
        }
        return false;
    }

    private boolean hasContain(String url) {
        for (int i = 0; i < urlList.size(); i++) {
            if (url.contains(urlList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public int addUrl(String url) {
        if(TextUtils.isEmpty(url)){
            return 0;
        }
        for (int i = 0; i < urlList.size(); i++) {
            if (url.equals(urlList.get(i))) {
                //已经存在
                return 0;
            }
        }
        if (urlList.size() < 1) {
            hostText = url;
            FileUtils.write(App.getContext(), filePath, url);
        } else {
            hostText = hostText + "; " + url;
            FileUtils.writeEnd(App.getContext(), filePath, "; " + url);
        }
        urlList.add(url);
        return 1;
    }

    public void deleteAll() {
        urlList.clear();
        hostText = "";
        FileUtils.write(App.getContext(), filePath, "");
    }

    public boolean delete(String url) {
        if (urlList.size() == 1) {
            urlList.remove(0);
            FileUtils.write(App.getContext(), filePath, "");
            return true;
        } else {
            for (int i = 0; i < urlList.size(); i++) {
                if (urlList.get(i).equals(url)) {
                    urlList.remove(i);
                    break;
                }
            }
            if (urlList.get(0).equals(url)) {
                hostText = hostText.replaceFirst(url + "; ", "");
            } else {
                hostText = hostText.replaceFirst("; " + url, "");
            }
            FileUtils.write(App.getContext(), filePath, hostText);
            return true;
        }
    }

    public List<String> getUrlList(){
        return urlList;
    }
}
