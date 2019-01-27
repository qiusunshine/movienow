package com.dyh.movienow.core.event;


/**
 * Created by xm on 17/8/21.
 */
public class FindVideoEvent {
    private String title;
    private String url;

    public FindVideoEvent(String title) {
        this.title = title;
    }

    public FindVideoEvent(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public FindVideoEvent() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
