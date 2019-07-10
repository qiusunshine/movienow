package com.dyh.movienow.core.parser;

import android.os.Looper;
import android.text.TextUtils;

import com.dyh.movienow.base.BaseCallback;
import com.dyh.movienow.bean.ChapterBean;
import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.bean.MovieRecommends;
import com.dyh.movienow.bean.SearchResult;
import com.dyh.movienow.ui.TypeConstant;
import com.dyh.movienow.ui.chapter.ChapterCallback;
import com.dyh.movienow.ui.daoHang.DaoHangMovieCallBack;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2018/12/9
 * 时间：At 18:46
 */
public class JsEngineBridge {
    /**
     * 搜索
     * @param movieInfo 规则
     * @param s 源码
     * @param callback 回调
     */
    public static void parseCallBack(MovieInfoUse movieInfo, String s, final BaseCallback<List<SearchResult>> callback){
        parseCallBack(movieInfo, s, new SearchJsCallBack<List<SearchResult>>() {
            @Override
            public void showData(List<SearchResult> data) {
                callback.onSuccess(data);
            }

            @Override
            public void showErr(String msg) {
                callback.onError(msg);
            }
        });
    }
    /**
     * 搜索
     * @param movieInfo 规则
     * @param s 源码
     * @param callback 回调
     */
    public static void parseCallBack(final MovieInfoUse movieInfo, String s, final SearchParser.SearchBridgeCallBack callback){
        parseCallBack(movieInfo, s, new SearchJsCallBack<List<SearchResult>>() {
            @Override
            public void showData(List<SearchResult> data) {
                callback.showData(data);
            }

            @Override
            public void showErr(String msg) {
                callback.showErr(msg);
            }
        });
    }
    /**
     * 搜索
     * @param movieInfo 规则
     * @param s 源码
     * @param callback 回调
     */
    public static void parseCallBack(final MovieInfoUse movieInfo, final String s, final SearchJsCallBack<List<SearchResult>> callback){
        if(Looper.myLooper() != Looper.getMainLooper()){
            JSEngine.getInstance().parseSearchRes(s, movieInfo, new JSEngine.OnFindCallBack<List<SearchResult>>() {
                @Override
                public void onSuccess(List<SearchResult> data) {
                    callback.showData(data);
                }

                @Override
                public void showErr(String msg) {
                    callback.showErr(msg);
                }
            });
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSEngine.getInstance().parseSearchRes(s, movieInfo, new JSEngine.OnFindCallBack<List<SearchResult>>() {
                        @Override
                        public void onSuccess(List<SearchResult> data) {
                            callback.showData(data);
                        }

                        @Override
                        public void showErr(String msg) {
                            callback.showErr(msg);
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * 集数
     * @param movieInfo 规则
     * @param s 源码
     * @param callback 回调
     */

    public static void parseCallBack(final MovieInfoUse movieInfo, final String s, final ChapterCallback callback){
        if(Looper.myLooper() != Looper.getMainLooper()){
            JSEngine.getInstance().parseChapter(s, movieInfo, new JSEngine.OnFindCallBack<List<ChapterBean>>() {
                @Override
                public void onSuccess(List<ChapterBean> data) {
                    callback.loadSuccess(data);
                }

                @Override
                public void showErr(String msg) {
                    callback.loadFail(msg);
                }
            });
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSEngine.getInstance().parseChapter(s, movieInfo, new JSEngine.OnFindCallBack<List<ChapterBean>>() {
                        @Override
                        public void onSuccess(List<ChapterBean> data) {
                            callback.loadSuccess(data);
                        }

                        @Override
                        public void showErr(String msg) {
                            callback.loadFail(msg);
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * 用JS解析字符串
     * @param input 字符串
     * @param js js规则
     * @param movieInfoUse movieInfoUse
     * @param callBack 返回
     */
    public static void parseCallBack(final String input, final String js,
                                      final MovieInfoUse movieInfoUse, final JSEngine.OnFindCallBack<String> callBack){
        if(Looper.myLooper() != Looper.getMainLooper()){
            JSEngine.getInstance().parseStr(input, js, movieInfoUse, callBack);
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSEngine.getInstance().parseStr(input, js, movieInfoUse, callBack);
                }
            }).start();
        }
    }

    public static void parseHomeCallBack(final String colType, final String rule, final String s, final boolean newLoad, final DaoHangMovieCallBack callback){
        if(Looper.myLooper() != Looper.getMainLooper()){
            JSEngine.getInstance().parseHome(s, rule, new JSEngine.OnFindCallBack<List<MovieRecommends>>() {
                @Override
                public void onSuccess(List<MovieRecommends> data) {
                    if(!TextUtils.isEmpty(colType)) {
                        for (int i = 0; i < data.size(); i++) {
                            if("*".equals(data.get(i).getPic())){
                                data.get(i).setType(TypeConstant.HOME_COL_TEXT_1);
                            }else {
                                data.get(i).setType(colType);
                            }
                        }
                    }else {
                        for (int i = 0; i < data.size(); i++) {
                            if("*".equals(data.get(i).getPic())){
                                data.get(i).setType(TypeConstant.HOME_COL_TEXT_1);
                            }
                        }
                    }
                    if (newLoad) {
                        callback.loadNewSuccess(data);
                    } else {
                        callback.onSuccess(data);
                    }
                }

                @Override
                public void showErr(String msg) {
                    callback.onError(msg);
                }
            });
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSEngine.getInstance().parseHome(s, rule, new JSEngine.OnFindCallBack<List<MovieRecommends>>() {
                        @Override
                        public void onSuccess(List<MovieRecommends> data) {
                            if(!TextUtils.isEmpty(colType)) {
                                for (int i = 0; i < data.size(); i++) {
                                    if("*".equals(data.get(i).getPic())){
                                        data.get(i).setType(TypeConstant.HOME_COL_TEXT_1);
                                    }else {
                                        data.get(i).setType(colType);
                                    }
                                }
                            }else {
                                for (int i = 0; i < data.size(); i++) {
                                    if("*".equals(data.get(i).getPic())){
                                        data.get(i).setType(TypeConstant.HOME_COL_TEXT_1);
                                    }
                                }
                            }
                            if (newLoad) {
                                callback.loadNewSuccess(data);
                            } else {
                                callback.onSuccess(data);
                            }
                        }

                        @Override
                        public void showErr(String msg) {
                            callback.onError(msg);
                        }
                    });
                }
            }).start();
        }
    }

}
