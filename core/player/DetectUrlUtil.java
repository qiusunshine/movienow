package com.dyh.movienow.core.player;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.dyh.movienow.ui.event.ShowToastMessageEvent;
import com.dyh.movienow.ui.setting.entity.VideoFormat;
import com.dyh.movienow.ui.setting.entity.VideoInfo;
import com.dyh.movienow.ui.setting.util.HttpRequestUtil;
import com.dyh.movienow.ui.setting.util.M3U8Util;
import com.dyh.movienow.ui.setting.util.UUIDUtil;
import com.dyh.movienow.ui.setting.util.VideoFormatUtil;
import com.dyh.movienow.util.CollectionUtil;
import com.dyh.movienow.util.TextPinyinUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 作者：By hdy
 * 日期：On 2018/11/1
 * 时间：At 13:23
 */
public class DetectUrlUtil {
    public static volatile List<String> filters = CollectionUtil.asList(".css", ".html", ".js", ".ttf", ".ico", ".png", ".jpg", ".jpeg", ".cnzz");
    public static volatile List<String> images = CollectionUtil.asList("mp4", "m3u8", ".flv", ".avi", ".3gp", "mpeg", ".wmv", ".mov", "rmvb", ".dat", "qqBFdownload", ".mp3", ".wav", ".ogg", ".flac", ".m4a");

    public static void detectUrlToDownload(String url, String title, DetectListener listener) {
        try {
            HttpRequestUtil.HeadRequestResponse headRequestResponse = HttpRequestUtil.performHeadRequest(url);
            url = headRequestResponse.getRealUrl();
            Map<String, List<String>> headerMap = headRequestResponse.getHeaderMap();
            if (headerMap == null || !headerMap.containsKey("Content-Type")) {
                //检测失败，未找到Content-Type
                Log.d("WorkerThread", "fail 未找到Content-Type:" + JSON.toJSONString(headerMap) + " taskUrl=" + url);
                EventBus.getDefault().post(new ShowToastMessageEvent(title + "检测链接失败，请重试"));
                return;
            }
            Log.d("WorkerThread", "Content-Type:" + headerMap.get("Content-Type").toString() + " taskUrl=" + url);
            VideoFormat videoFormat = VideoFormatUtil.detectVideoFormat(url, headerMap.get("Content-Type").toString());
            if (videoFormat == null) {
                //检测成功，不是视频
                Log.d("WorkerThread", "fail not video taskUrl=" + url);
                EventBus.getDefault().post(new ShowToastMessageEvent(title + "无法下载"));
                return;
            }
            com.dyh.movienow.ui.setting.entity.VideoInfo videoInfo = new com.dyh.movienow.ui.setting.entity.VideoInfo();
            if ("player/m3u8".equals(videoFormat.getName())) {
                double duration = M3U8Util.figureM3U8Duration(url);
                if (duration <= 0) {
                    //检测成功，不是m3u8的视频
                    Log.d("WorkerThread", "fail not m3u8 taskUrl=" + url);
                    return;
                }
                videoInfo.setDuration(duration);
            } else {
                long size = 0;
                Log.d("WorkerThread", JSON.toJSONString(headerMap));
                if (headerMap.containsKey("Content-Length") && headerMap.get("Content-Length").size() > 0) {
                    try {
                        size = Long.parseLong(headerMap.get("Content-Length").get(0));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Log.d("WorkerThread", "NumberFormatException", e);
                    }
                }
                videoInfo.setSize(size);
            }
            videoInfo.setUrl(url);
            String fileName;
            try {
                String adjustTitle;
                String[] titles = title.replace(File.separator, "").split("\\$");
                if (titles.length > 0) {
                    adjustTitle = titles[0];
                } else {
                    adjustTitle = title.replace(File.separator, "");
                }
                fileName = TextPinyinUtil.getInstance().getPinyin(adjustTitle) + "_" + System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
                fileName = UUIDUtil.genUUID();
            }
            videoInfo.setFileName(fileName);
            videoInfo.setVideoFormat(videoFormat);
            videoInfo.setSourcePageTitle(title);
            videoInfo.setSourcePageUrl(url);
            //检测成功，是视频
            Log.d("WorkerThread", "found video taskUrl=" + url);
            listener.onSuccess(videoInfo);
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ShowToastMessageEvent(title + "下载出错了！" + e.toString()));
        }

    }

    public static int isVideoSimple(String url) {
        String needCheckUrl = getNeedCheckUrl(url);
        for (String filter : filters) {
            if (needCheckUrl.contains(filter)) {
                return -1;
            }
        }
        for (int i = 0; i < images.size(); i++) {
            if (needCheckUrl.contains(images.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static String getNeedCheckUrl(String url) {
        url = url.replace("http://", "").replace("https://", "");
        String[] urls = url.split("/");
        if (urls.length > 1) {
            //去掉域名
            return url.replace(urls[0], "");
        }
        return url;
    }

    public static VideoInfo detectVideoComplex(String url, String title) throws IOException {
        HttpRequestUtil.HeadRequestResponse headRequestResponse = HttpRequestUtil.performHeadRequest(url);
        url = headRequestResponse.getRealUrl();
        Map<String, List<String>> headerMap = headRequestResponse.getHeaderMap();
        if (headerMap == null || !headerMap.containsKey("Content-Type")) {
            //检测失败，未找到Content-Type
            return null;
        }
        VideoFormat videoFormat = VideoFormatUtil.detectVideoFormat(url, headerMap.get("Content-Type").toString());
        if (videoFormat == null) {
            //检测成功，不是视频
            return null;
        }
        com.dyh.movienow.ui.setting.entity.VideoInfo videoInfo = new com.dyh.movienow.ui.setting.entity.VideoInfo();
        if ("player/m3u8".equals(videoFormat.getName())) {
            double duration = M3U8Util.figureM3U8Duration(url);
            if (duration <= 0) {
                //检测成功，不是m3u8的视频
                return null;
            }
            videoInfo.setDetectImageType("m3u8");
            videoInfo.setDuration(duration);
        } else {
            long size = 0;
            if (headerMap.containsKey("Content-Length") && headerMap.get("Content-Length").size() > 0) {
                try {
                    size = Long.parseLong(headerMap.get("Content-Length").get(0));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            videoInfo.setDetectImageType(size / 1024 / 1024 + "MB");
            videoInfo.setSize(size);
        }
        videoInfo.setUrl(url);
        videoInfo.setFileName(UUIDUtil.genUUID());
        videoInfo.setVideoFormat(videoFormat);
        videoInfo.setSourcePageTitle(title);
        videoInfo.setSourcePageUrl(url);
        return videoInfo;
    }

    public interface DetectListener {
        void onSuccess(com.dyh.movienow.ui.setting.entity.VideoInfo videoInfo);
    }
}
