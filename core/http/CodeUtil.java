package com.dyh.movienow.core.http;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Map;

import cn.finalteam.okhttpfinal.HttpRequest;
import cn.finalteam.okhttpfinal.Part;
import cn.finalteam.okhttpfinal.RequestParams;
import cn.finalteam.okhttpfinal.StringHttpRequestCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 作者：By hdy
 * 日期：On 2018/12/2
 * 时间：At 15:00
 */
public class CodeUtil {

    public static void get(String url, OnCodeGetListener listener) {
        get(url, "UTF-8", null, listener);
    }

    public static void get(String url, final String charset, Map<String, String> headers, final OnCodeGetListener listener) {
        if (TextUtils.isEmpty(charset) || "UTF-8".equals(charset)) {
            RequestParams params = null;
            if(headers!=null && !headers.isEmpty()){
                params = new RequestParams();
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    params.addHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpRequest.get(url, params, new StringHttpRequestCallback() {
                @Override
                protected void onSuccess(String s) {
                    super.onSuccess(s);
                    listener.onSuccess(s);
                }

                @Override
                public void onFailure(int errorCode, String msg) {
                    super.onFailure(errorCode, msg);
                    listener.onFailure(errorCode, msg);
                }
            });
        } else {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(url);
            if(headers!=null && !headers.isEmpty()){
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            final Request request = builder.build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        byte[] b = response.body().bytes();
                        String info = new String(b, charset);
                        listener.onSuccess(info);
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(112, e.toString());
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    listener.onFailure(111, e.toString());
                }
            });
        }
    }

    public static void post(String url, RequestParams params, OnCodeGetListener listener) {
        post(url, params, "UTF-8", null, listener);
    }

    public static void post(String url, RequestParams params, final String charset, Map<String, String> headers, final OnCodeGetListener listener) {
        boolean isJsonBody = false;
        for (Part part : params.getFormParams()) {
            if(part.getKey().equals("JsonBody")){
                isJsonBody = true;
            }
        }
        if (TextUtils.isEmpty(charset) || "UTF-8".equals(charset)) {
            if(isJsonBody){
                params.applicationJson();
            }
            if(headers!=null && !headers.isEmpty()){
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    params.addHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpRequest.post(url, params, new StringHttpRequestCallback() {
                @Override
                protected void onSuccess(String s) {
                    super.onSuccess(s);
                    listener.onSuccess(s);
                }

                @Override
                public void onFailure(int errorCode, String msg) {
                    super.onFailure(errorCode, msg);
                    listener.onFailure(errorCode, msg);
                }
            });
        } else {
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody requestBody;
            if(isJsonBody){
                JSONObject jsonObject = new JSONObject();
                for (Part part : params.getFormParams()) {
                    jsonObject.put(part.getKey(), part.getValue());
                }
                String json = jsonObject.toJSONString();
                requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            }else {
                FormBody.Builder builder = new FormBody.Builder();
                for (Part part : params.getFormParams()) {
                    String key = part.getKey();
                    String value = part.getValue();
                    builder.add(key, value);
                }
                requestBody = builder.build();
            }
            Request.Builder builder = new Request.Builder().url(url);
            if(headers!=null && !headers.isEmpty()){
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = builder.post(requestBody).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onFailure(111, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        byte[] b = response.body().bytes();
                        String info = new String(b, charset);
                        listener.onSuccess(info);
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(112, e.toString());
                    }
                }
            });
        }
    }

    public interface OnCodeGetListener {
        void onSuccess(String s);

        void onFailure(int errorCode, String msg);
    }
}
