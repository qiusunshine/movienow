package com.dyh.movienow.core.parser;

import com.dyh.movienow.base.BaseCallback;
import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.bean.SearchResult;
import com.dyh.movienow.ui.searchV2.SearchView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2018/10/22
 * 时间：At 14:46
 */
public class SearchParser {
    /**
     * 解析搜索结果
     * @param movieInfo 规则1
     * @param s 源码
     * @param callback 回调
     */
    public static void findList(MovieInfoUse movieInfo, String s, final BaseCallback<List<SearchResult>> callback) {
        findList(movieInfo, s, new SearchBridgeCallBack() {
            @Override
            public void showData(List<SearchResult> data) {
                callback.onSuccess(data);
            }

            @Override
            public void showErr(String msg) {
                callback.onError(msg);
            }
        }, -1);
    }

    /**
     * 解析搜索结果
     * @param movieInfo 规则1
     * @param s 源码
     * @param callback 回调
     * @param fromItem 来源
     */
    public static void findList(MovieInfoUse movieInfo, String s, final SearchView callback, int fromItem) {
        findList(movieInfo, s, new SearchBridgeCallBack() {
            @Override
            public void showData(List<SearchResult> data) {
                callback.showData(data);
            }

            @Override
            public void showErr(String msg) {
                callback.showErr(msg);
            }
        }, fromItem);

    }

    /**
     * 解析搜索结果
     * @param movieInfo 规则1
     * @param s 源码
     * @param callback 回调
     * @param fromItem 来源
     */

    private static void findList(MovieInfoUse movieInfo, String s, SearchBridgeCallBack callback, int fromItem) {
        if (movieInfo.getSearchFind().startsWith("js:")) {
            JsEngineBridge.parseCallBack(movieInfo, s, callback);
            return;
        }
        boolean isOk = true;
        List<SearchResult> listBeanList = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(s);
            String[] ss = movieInfo.getSearchFind().split(";");
            String[] ss0 = ss[0].split("&&");
            Element element;
            element = CommonParser.getTrueElement(ss0[0], doc);
            Elements elements;
            /**
             * 获取列表
             */
            for (int i = 1; i < ss0.length - 1; i++) {
                element = CommonParser.getTrueElement(ss0[i], element);
            }
            elements = CommonParser.selectElements(element, ss0[ss0.length - 1]);
            /**
             * 获取名字和链接
             */
            for (Element elementt : elements) {
                try {
                    SearchResult listBean = new SearchResult();
                    //获取名字
                    String[] ss1 = ss[1].split("&&");
                    Element element2;
                    if (ss1.length == 1) {
                        element2 = elementt;
                    } else {
                        element2 = CommonParser.getTrueElement(ss1[0], elementt);
                    }
                    for (int i = 1; i < ss1.length - 1; i++) {
                        element2 = CommonParser.getTrueElement(ss1[i], element2);
                    }
                    listBean.setTitle(CommonParser.getText(element2, ss1[ss1.length - 1]));
                    //获取链接
                    String[] ss2 = ss[2].split("&&");
                    Element element3;
                    if (ss2.length == 1) {
                        element3 = elementt;
                    } else {
                        element3 = CommonParser.getTrueElement(ss2[0], elementt);
                    }
                    for (int i = 1; i < ss2.length - 1; i++) {
                        element3 = CommonParser.getTrueElement(ss2[i], elementt);
                    }
                    listBean.setUrl(CommonParser.getUrl(element3, ss2[ss2.length - 1], movieInfo.getBaseUrl(), movieInfo.getChapterUrl()));
                    //来自的网站名称
                    listBean.setDesc(movieInfo.getTitle());
                    listBean.setType("video");
                    if (fromItem != -1) {
                        listBean.setFromMovieInfo(fromItem);
                    }
                    listBeanList.add(listBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isOk = false;
            callBackError(callback, "findList：msg：" + e.toString());
        }
        if (isOk) {
            callBackSuccess(callback, listBeanList);
        }

    }


    /**
     * static方法会锁住所有这个类中的这个方法，方便线程同步
     *
     * @param callback 返回到view中
     * @param msg      错误信息
     */
    public static synchronized void callBackError(SearchBridgeCallBack callback, String msg) {
        callback.showErr(msg);
    }

    private static synchronized void callBackSuccess(SearchBridgeCallBack callback, List<SearchResult> listBeanList) {
        callback.showData(listBeanList);
    }

    public interface SearchBridgeCallBack {
        void showData(List<SearchResult> data);

        void showErr(String msg);
    }
}
