package com.dyh.movienow.core.player;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import com.dyh.movienow.ui.setting.util.FileUtil;
import com.dyh.movienow.util.FileUtils;
import com.dyh.movienow.util.Helper;
import com.dyh.movienow.util.StringUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：By hdy
 * 日期：On 2019/4/14
 * 时间：At 18:35
 */
public class WebJSManager {
    private volatile static WebJSManager sInstance;
    private Map<String, String> jsLoader;
    private String globalJs;

    private WebJSManager(Context context) {
        jsLoader = scanDomainToMap(context);
    }

    public static WebJSManager instance(Context context) {
        if (sInstance == null) {
            synchronized (WebJSManager.class) {
                if (sInstance == null) {
                    sInstance = new WebJSManager(context);
                }
            }
        }
        return sInstance;
    }

    private Map<String, String> scanDomainToMap(Context context) {
        Map<String, String> jsLoader = new HashMap<>();
        //APP自带的JS插件
        jsLoader.put("m.icantv.cn", FileUtils.getAssetsString("icantv.js", context));
        //扫描用户自定义的插件
        if (!Helper.canWriteFile(context)) {
            return jsLoader;
        }
        File jsDir = new File(getJsDirPath());
        initJsDir(jsDir);
        String[] fileNames = jsDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".js");
            }
        });
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if ("*.js".equals(fileName)) {
                    globalJs = initGlobalJs();
                } else {
                    jsLoader.put(fileName.substring(0, fileName.length() - 3), "");
                }
            }
        }
        return jsLoader;
    }

    public String getJs(String url) {
        String js = null;
        try {
            js = getJsWithException(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return js;
    }

    public String getGlobalJs() {
        return globalJs;
    }

    private void initJsDir(File jsDir) {
        if (!jsDir.exists()) {
            jsDir.mkdirs();
        }
        if (!jsDir.isDirectory()) {
            try {
                jsDir.delete();
                jsDir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String initGlobalJs(){
        String jsDirPath = getJsDirPath();
        initJsDir(new File(jsDirPath));
        String jsFilePath = jsDirPath + File.separator + "*.js";
        File file = new File(jsFilePath);
        if (!file.exists()) {
            return null;
        } else {
            byte[] bytes = FileUtil.fileToBytes(jsFilePath);
            if (bytes == null) {
                return null;
            }
            String text = new String(bytes);
            if (TextUtils.isEmpty(text)) {
                return null;
            } else {
                return text;
            }
        }
    }

    private String getJsDirPath() {
        String rulesPath = Environment.getExternalStorageDirectory() + File.separator + "FangYuan" + File.separator + "rules";
        File dir = new File(rulesPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return rulesPath + File.separator + "js";
    }

    private String getJsWithException(String url) {
        String dom = StringUtil.getDom(url);
        if (!jsLoader.containsKey(dom)) {
            return null;
        }
        if (TextUtils.isEmpty(jsLoader.get(dom))) {
            String jsDirPath = getJsDirPath();
            initJsDir(new File(jsDirPath));
            String jsFilePath = jsDirPath + File.separator + dom + ".js";
            File file = new File(jsFilePath);
            if (!file.exists()) {
                jsLoader.remove(dom);
                return null;
            } else {
                byte[] bytes = FileUtil.fileToBytes(jsFilePath);
                if (bytes == null) {
                    jsLoader.remove(dom);
                    return null;
                }
                String text = new String(bytes);
                if (TextUtils.isEmpty(text)) {
                    jsLoader.remove(dom);
                    return null;
                } else {
                    jsLoader.put(dom, text);
                }
            }
        }
        return jsLoader.get(dom);
    }
}
