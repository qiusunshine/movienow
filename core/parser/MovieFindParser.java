package com.dyh.movienow.core.parser;
import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.core.http.CodeUtil;
import com.dyh.movienow.ui.chapter.MovieCallback;
import com.dyh.movienow.util.HeavyTaskUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cn.finalteam.okhttpfinal.HttpRequest;
import cn.finalteam.okhttpfinal.StringHttpRequestCallback;

/**
 * 作者：By hdy
 * 日期：On 2017/10/31
 * 时间：At 19:04
 */

public class MovieFindParser {
    public void get(final MovieInfoUse movieInfo, final MovieCallback callback){
        callback.movieLoading();
        if(movieInfo.getMovieFind().startsWith("js:")){
            if(movieInfo.getMovieFind().startsWith("js:noCode:")){
                movieInfo.setMovieFind(movieInfo.getMovieFind().replaceFirst("noCode:",""));
                HeavyTaskUtil.executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        JSEngine.getInstance().parseMovieFind("", movieInfo, new JSEngine.OnFindCallBack<String>() {
                            @Override
                            public void onSuccess(String url) {
                                callback.movieLoadSuccess(url);
                            }

                            @Override
                            public void showErr(String msg) {
                                callback.movieLoadFail("JSParseError-msg："+msg);
                            }
                        });
                    }
                });
            }else {
                CodeUtil.get(movieInfo.getMovieUrl(), new CodeUtil.OnCodeGetListener() {
                    @Override
                    public void onSuccess(final String s) {
                        HeavyTaskUtil.executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                JSEngine.getInstance().parseMovieFind(s, movieInfo, new JSEngine.OnFindCallBack<String>() {
                                    @Override
                                    public void onSuccess(String url) {
                                        callback.movieLoadSuccess(url);
                                    }

                                    @Override
                                    public void showErr(String msg) {
                                        callback.movieLoadFail("JSParseError-msg："+msg);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        callback.movieLoadFail("HttpRequestError-msg："+msg);
                    }
                });
            }
            return;
        }
        HttpRequest.get(movieInfo.getMovieUrl(),null,new StringHttpRequestCallback(){
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                //解析数据源
                Document doc = Jsoup.parse(s);
                //获取视频播放链接
                String[] ss0 = movieInfo.getMovieFind().split("&&");
                Element element0;
                element0 = CommonParser.getTrueElement(ss0[0],doc);
                for (int i = 1; i < ss0.length-1; i++) {
                    element0 = CommonParser.getTrueElement(ss0[i],element0);
                }
                callback.movieLoadSuccess(CommonParser.getUrl(element0,ss0[ss0.length-1],movieInfo,movieInfo.getMovieUrl()));
            }
            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                callback.movieLoadFail("HttpRequestError-msg："+msg);
            }
        });
    }
}
