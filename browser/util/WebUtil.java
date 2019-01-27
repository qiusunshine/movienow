package com.dyh.browser.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.dyh.browser.activity.WebActivity;
import com.dyh.browser.activity.WebViewActivity;

/**
 * 作者：By hdy
 * 日期：On 2018/8/26
 * 时间：At 10:29
 */

public class WebUtil {
    public static void goWeb(Context context, String url, @Nullable Integer option) {
        Intent intent = new Intent();
        if (option != null && option == 2) {
            intent.setClass(context, WebActivity.class);
        } else {
            intent.setClass(context, WebViewActivity.class);
        }
        intent.putExtra("is_xiu_tan",false);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public static void goWebForXiuTan(Context context, String title, String url, @Nullable Integer option) {
        Intent intent = new Intent();
        if (option != null && option == 2) {
            intent.setClass(context, WebActivity.class);
        } else {
            intent.setClass(context, WebViewActivity.class);
        }
        intent.putExtra("is_xiu_tan",true);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }
}
