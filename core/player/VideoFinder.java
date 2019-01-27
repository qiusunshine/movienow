package com.dyh.movienow.core.player;

import com.dyh.movienow.ui.chapter.MovieCallback;

/**
 * 作者：By hdy
 * 日期：On 2018/12/2
 * 时间：At 14:55
 */
public class VideoFinder {

    private static VideoFinder instance;

    public VideoFinder() {

    }

    public static VideoFinder getInstance(){
        if(instance==null){
            synchronized (VideoFinder.class){
                if(instance==null){
                    instance = new VideoFinder();
                }
            }
        }
        return instance;
    }

    public static String findWithJavaScript(String code, String js, MovieCallback callback){
        return findWithJavaScript(code, js, "UTF-8", callback);
    }

    public static String findWithJavaScript(String code, String js, String charst, MovieCallback callback){
        return "";
    }
}
