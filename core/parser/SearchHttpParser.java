package com.dyh.movienow.core.parser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.dyh.movienow.core.http.CodeUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cn.finalteam.okhttpfinal.RequestParams;

/**
 * 作者：By hdy
 * 日期：On 2018/9/8
 * 时间：At 8:36
 */

public class SearchHttpParser {
    public static void getSearchUrl(String wd, String sourceUrl, @NonNull OnSearchCallBack onSearchCallBack) {
        String[] d = sourceUrl.split(";");
        if (d.length == 1) {
            get(sourceUrl.replace("**", wd), null, getHeaders(sourceUrl), onSearchCallBack);
        } else if (d.length == 2) {
            if ("get".equals(d[1])) {
                get(d[0].replace("**", wd), null, getHeaders(sourceUrl), onSearchCallBack);
            } else if ("post".equals(d[1])) {
                d[0] =  d[0].replace("**", wd);
                String[] ss = d[0].split("\\?");
                String[] sss = ss[1].split("&");
                RequestParams params = new RequestParams();
                for (int i = 0; i < sss.length; i++) {
                    if(TextUtils.isEmpty(sss[i])){
                        continue;
                    }
                    String[] kk = sss[i].split("=");
                    if(kk.length>=2){
                        params.addFormDataPart(kk[0], kk[1]);
                    }
                }
                post(ss[0], null, getHeaders(sourceUrl), params, onSearchCallBack);
            } else {
                wd = encodeUrl(wd, d[1]);
                get(d[0].replace("**", wd), d[1], getHeaders(sourceUrl), onSearchCallBack);
            }
        } else {
            wd = encodeUrl(wd, d[2]);
            if ("post".equals(d[1])) {
                d[0] =  d[0].replace("**", wd);
                String[] ss = d[0].split("\\?");
                String[] sss = ss[1].split("&");
                RequestParams params = new RequestParams();
                for (int i = 0; i < sss.length; i++) {
                    if(TextUtils.isEmpty(sss[i])){
                        continue;
                    }
                    String[] kk = sss[i].split("=");
                    if(kk.length>=2){
                        params.addFormDataPart(kk[0], kk[1]);
                    }
                }
                post(ss[0], d[2], getHeaders(sourceUrl), params, onSearchCallBack);
            } else {
                get(d[0].replace("**", wd), d[2], getHeaders(sourceUrl), onSearchCallBack);
            }
        }
    }

    public static Map<String, String> getHeaders(String searchUrl){
        String[] d = searchUrl.split(";");
        if(!d[d.length-1].startsWith("{") || !d[d.length-1].endsWith("}")){
            return null;
        }
        Map<String, String> headers = new HashMap<>();
        String h = d[d.length-1].replace("{","").replace("}","");
        String[] hs = h.split("; ");
        for (String h1 : hs) {
            String[] keyValue = h1.split(": ");
            if(keyValue.length >= 2){
                if("getTimeStamp()".equals(keyValue[1])){
                    headers.put(keyValue[0], System.currentTimeMillis() + "");
                }else {
                    headers.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return headers;
    }

    public static void get(String url, @Nullable final String code, @Nullable Map<String, String> headers, @NonNull final OnSearchCallBack onSearchCallBack) {
        CodeUtil.get(url, code, headers, new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                onSearchCallBack.onSuccess(s);
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                onSearchCallBack.onFailure(errorCode, msg);
            }
        });
    }

    public static void post(String url, @Nullable final String code, @Nullable Map<String, String> headers, RequestParams params, @NonNull final OnSearchCallBack onSearchCallBack) {
        CodeUtil.post(url, params, code, headers, new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                onSearchCallBack.onSuccess(s);
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                onSearchCallBack.onFailure(111, msg);
            }
        });
    }

    public interface OnSearchCallBack {

        void onSuccess(String s);

        void onFailure(int errorCode, String msg);
    }

    public static String encodeUrl(String str, String code) {//url解码
        try {
            str = java.net.URLEncoder.encode(str, code);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

}
