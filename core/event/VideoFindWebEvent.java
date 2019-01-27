package com.dyh.movienow.core.event;


/**
 * Created by xm on 17/8/21.
 */
public class VideoFindWebEvent {
    private String url;

    public VideoFindWebEvent(String url) {
        this.url = url;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
