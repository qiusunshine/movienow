package com.dyh.movienow.core.event;


/**
 * Created by xm on 17/8/21.
 */
public class ChapterEvent {
    private String url;

    public ChapterEvent(){

    }
    public ChapterEvent(String url){
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
