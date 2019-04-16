package com.dyh.movienow.core.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.dyh.browser.activity.WebViewActivity;
import com.dyh.browser.util.WebUtil;
import com.dyh.movienow.core.parser.LocalServerParser;
import com.dyh.movienow.ui.videoPlayer.CommonVideoPlayer;
import com.dyh.movienow.ui.videoPlayer.V4VideoPlayer;
import com.dyh.movienow.util.FileUtils;
import com.dyh.movienow.util.HeavyTaskUtil;
import com.dyh.movienow.util.PreferenceMgr;
import com.dyh.movienow.util.ToastMgr;
import com.tencent.smtt.sdk.TbsVideo;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2018/8/27
 * 时间：At 20:31
 */

public class PlayChooser {

    public static void startPlayer(Context context, String title, String movieUrl) {
        int player = (int) PreferenceMgr.get(context,"ijkplayer", "player", 2);
        startPlayer(context, player, title, movieUrl);
    }

    private static void startPlayer(Context context, int player, String title, String movieUrl) {
        Intent intent = new Intent();
        movieUrl = LocalServerParser.getRealUrl(movieUrl);
        if(player==0){
            intent.putExtra("playerName","腾讯X5");
        }else if(player==3){
            intent.putExtra("playerName","播放器④");
            intent.putExtra("isUsePlayer",true);
        }else if(player==11){
            intent.putExtra("playerName","MxPlayer");
        }else if(player==12){
            intent.putExtra("playerName","XPlayer");
        }else if(player==13){
            intent.putExtra("playerName","KMPlayer");
        }else if(player==14){
            intent.putExtra("playerName","MoboPlayer");
        }else {
            intent.putExtra("playerName","腾讯X5");
        }
        if(player==0||player==3||player==11||player==12||player==13||player==14){
            intent.setClass(context, CommonVideoPlayer.class);
        }else {
            intent.setClass(context, V4VideoPlayer.class);
        }
        intent.putExtra("title", title);
        intent.putExtra("videourl", movieUrl);
        context.startActivity(intent);
        HeavyTaskUtil.saveHistory(context,"视频播放", movieUrl, title);
    }

    public static boolean startPlayer(Context context, String player,String title,String url){
        switch (player){
            case "MxPlayer":
                return startMxPlayer(context,title,url);
            case "XPlayer":
                return startXPlayer(context,title,url);
            case "KMPlayer":
                return startKMPlayer(context,title,url);
            case "MoboPlayer":
                return startMoboPlayer(context,title,url);
            case "腾讯X5":
                startX5(context, title, url);
                return true;
            case "播放器④":
                Intent intent = new Intent();
                intent.setClass(context, WebViewActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("isUsePlayer",true);
                context.startActivity(intent);
                return true;
        }
        return false;
    }

    private static boolean startMxPlayer(Context context, String title, String url){
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FileUtils.getUri(context,url));
        paramBundle.putExtra("title",title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context,"com.mxtech.videoplayer.ad")) {
            try {
                paramBundle.setComponent(new ComponentName("com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.ad.ActivityScreen"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        if (appInstalledOrNot(context,"com.mxtech.videoplayer.pro")) {
            try {
                paramBundle.setComponent(new ComponentName("com.mxtech.videoplayer.pro", "com.mxtech.videoplayer.ActivityScreen"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = message + "____" +e.getMessage();
            }
        }
        ToastMgr.toastShortBottomCenter(context,"没有安装MXPlayer！" + message);
        return false;
    }

    private static boolean startXPlayer(Context context, String title, String url){
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FileUtils.getUri(context,url));
        paramBundle.putExtra("title",title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context,"video.player.videoplayer")) {
            try {
                paramBundle.setComponent(new ComponentName("video.player.videoplayer", "com.inshot.xplayer.activities.PlayerActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        ToastMgr.toastShortBottomCenter(context,"没有安装XPlayer！" + message);
        return false;
    }
    private static boolean startKMPlayer(Context context, String title, String url) {
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FileUtils.getUri(context,url));
        paramBundle.putExtra("title",title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context,"com.kmplayerpro")) {
            try {
                paramBundle.setComponent(new ComponentName("com.kmplayerpro", "com.kmplayer.activity.VideoPlayerActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        if (appInstalledOrNot(context,"com.kmplayer")) {
            try {
                paramBundle.setComponent(new ComponentName("com.kmplayer", "com.kmplayer.activity.VideoPlayerActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = message + "____" +e.getMessage();
            }
        }
        ToastMgr.toastShortBottomCenter(context,"没有安装KMPlayer！" + message);
        return false;
    }

    public static void startX5(Context context, String title, String url){
        if(url.startsWith("http://127.0.0.1")){
            ToastMgr.toastShortBottomCenter(context,"X5不支持此视频，正在用播放器①打开");
            startPlayer(context, 2, title, url);
            return;
        }
        if(TbsVideo.canUseTbsPlayer(context)){
            Bundle data = new Bundle();
            data.putInt("screenMode", 102);
            TbsVideo.openVideo(context, url, data);
        }else {
            ToastMgr.toastShortBottomCenter(context,"X5视频播放器打开失败，正在用浏览器打开");
            WebUtil.goWeb(context, url,2);
        }
    }

    private static boolean startMoboPlayer(Context context, String title, String url){
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FileUtils.getUri(context,url));
        paramBundle.putExtra("title",title);
        paramBundle.putExtra("name",title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context,"com.clov4r.android.nil.noad")) {
            try {
                paramBundle.setComponent(new ComponentName("com.clov4r.android.nil.noad", "com.clov4r.android.nil.ui.activity.MainActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        if (appInstalledOrNot(context,"com.clov4r.android.nil")) {
            try {
                paramBundle.setComponent(new ComponentName("com.clov4r.android.nil", "com.clov4r.android.nil.ui.activity.MainActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = message + "____" +e.getMessage();
            }
        }
        ToastMgr.toastShortBottomCenter(context,"没有安装MoboPlayer！" + message);
        return false;
    }

    public static boolean appInstalledOrNot(Context context, String paramString) {
        try {
            PackageManager packageManager = context.getPackageManager();// 获取packagemanager
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName;
                    if (paramString.equals(pn)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PackageManager pm = context.getPackageManager();
            try {
                pm.getPackageInfo(paramString, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
