package com.dyh.movienow.core.player;

import android.content.Context;

import com.dyh.movienow.App;
import com.dyh.movienow.bean.Video;
import com.dyh.movienow.bean.XiuTanLiked;
import com.dyh.movienow.core.event.FindVideoEvent;
import com.dyh.movienow.ui.setting.entity.VideoInfo;
import com.dyh.movienow.util.HeavyTaskUtil;
import com.dyh.movienow.util.PreferenceMgr;
import org.greenrobot.eventbus.EventBus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 作者：By hdy
 * 日期：On 2018/8/25
 * 时间：At 22:19
 */

public class DetectorManager {
    private volatile static DetectorManager sInstance;
    private LinkedBlockingQueue<Video> taskList = new LinkedBlockingQueue<>();
    private Set<VideoInfo> videoList = new HashSet<>();
    private List<MyThread> detectThreads = null;
    private int detectThreadSize;
    private boolean isSimpleMode;
    private Map<String, String> xiuTanLiked = null;

    private DetectorManager() {
        isSimpleMode = (boolean)PreferenceMgr.get(App.getContext(),"isSimpleMode",true);
        if(isSimpleMode){
            detectThreadSize = 2;
        }else {
            detectThreadSize = 3;
        }
    }

    public static DetectorManager getInstance() {
        if (sInstance == null) {
            synchronized (DetectorManager.class) {
                if (sInstance == null) {
                    sInstance = new DetectorManager();
                }
            }
        }
        return sInstance;
    }

    public boolean inXiuTanLiked(Context context, String dom, String url) {
        initXiuTanLiked(context);
        return xiuTanLiked.containsKey(dom) && xiuTanLiked.get(dom).equals(url);
    }

    private void initXiuTanLiked(Context context){
        if(xiuTanLiked==null){
            synchronized (this){
                if(xiuTanLiked==null){
                    xiuTanLiked = new HashMap<>();
                    List<XiuTanLiked> liked = HeavyTaskUtil.getXiuTanLiked(context);
                    for (int i = 0; i < liked.size(); i++) {
                        xiuTanLiked.put(liked.get(i).getDom(), liked.get(i).getUrl());
                    }
                }
            }
        }
    }

    public void putIntoXiuTanLiked(Context context, String dom, String url){
        initXiuTanLiked(context);
        xiuTanLiked.put(dom,url);
        HeavyTaskUtil.saveXiuTanLiked(context,dom,url);
    }

    public void addTask(Video video) {
        if (taskList.size() > 100) {
            return;
        }
        try {
            taskList.offer(video,5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void startDetect() {
        taskList.clear();
        videoList.clear();
    }

    public Set<VideoInfo> getVideoList() {
        return videoList;
    }

    public synchronized void createThread() {
        if (detectThreads == null) {
            detectThreads = new ArrayList<>();
            for (int i = 0; i < detectThreadSize; i++) {
                MyThread thread = new MyThread();
                detectThreads.add(thread);
                thread.start();
            }
        }
    }

    public synchronized void destroyDetector() {
        if (detectThreads != null) {
            for (int i = 0; i < detectThreadSize; i++) {
                if (!detectThreads.get(i).isInterrupted()) {
                    detectThreads.get(i).interrupt();
                }
            }
            sInstance = null;
            detectThreads = null;
            taskList = null;
            videoList = null;
        }
    }

    private static VideoInfo detect(Video video, boolean isSimpleMode) {
        VideoInfo info = detectSimple(video.getUrl(),video.getTitle());
        if(isSimpleMode){
            return info;
        }else if(info==null){
            try {
                info = detectComplex(video.getUrl(),video.getTitle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info;
    }

    private static VideoInfo detectSimple(String url,String title){
        int res = DetectUrlUtil.isVideoSimple(url);
        if(res>=0){
            com.dyh.movienow.ui.setting.entity.VideoInfo videoInfo1 = new com.dyh.movienow.ui.setting.entity.VideoInfo();
            if(res < DetectUrlUtil.images.length){
                videoInfo1.setDetectImageType(DetectUrlUtil.images[res].replace(".",""));
            }
            videoInfo1.setSourcePageTitle(title);
            videoInfo1.setSourcePageUrl(url);
            return videoInfo1;
        }
        return null;
    }

    private static VideoInfo detectComplex(String url,String title) throws IOException{
        if (!shouldDetect(url)) {
            return null;
        }
        if (isYouKuOrOther(url)) {
            if (!shouldDetectForYouKu(url)) {
                return null;
            }
        }
        return DetectUrlUtil.detectVideoComplex(url, title);
    }

    private static boolean shouldDetect(String url) {
        String urls = "acs.youku.com .ykimg.com iqiyipic.com alicdn.com alibaba.com .baidu.com gtimg.cn .js?" +
                " open.qq.com qpic.cn letvimg.com apple-www -go.letv.com irs01.com log.mgtv.com .css? cnzz.com";
        String[] u = urls.split(" ");
        for (String anU : u) {
            if (url.contains(anU)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isYouKuOrOther(String url) {
        String urls = ".iqiyi.com .youku.com .le.com .letv.com v.qq.com .tudou.com .mgtv.com film.sohu.com tv.sohu.com .acfun.cn .bilibili.com .pptv.com vip.1905.com .yinyuetai.com .fun.tv .56.com";
        String[] u = urls.split(" ");
        for (String anU : u) {
            if (url.contains(anU)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldDetectForYouKu(String url) {
        return url.contains("=");
    }


    private class MyThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!Thread.currentThread().isInterrupted()) {
                //阻塞获取直到有值
                if(taskList==null){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Video web = null;
                try {
                    if(taskList!=null){
                        web = taskList.take();
                    }
                } catch (InterruptedException ignored) {
                }
                if(!Thread.currentThread().isInterrupted()&&web != null){
                    VideoInfo videoInfo = null;
                    try {
                        videoInfo = detect(web, isSimpleMode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!Thread.currentThread().isInterrupted()&&videoInfo != null&&videoList!=null) {
                        videoList.add(videoInfo);
                        EventBus.getDefault().post(new FindVideoEvent(videoList.size() + "", videoInfo.getSourcePageUrl()));
                    }
                }
            }
        }
    }
}
