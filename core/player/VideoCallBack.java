package com.dyh.movienow.core.player;

/**
 * 作者：By hdy
 * 日期：On 2018/2/24
 * 时间：At 10:12
 */

public interface VideoCallBack {
    void loadVideoSuccess(String url);
    void loadVideoFail(String msg);
    void loadingVideoStart();
    void loadVideoProgress(int progress);
}
