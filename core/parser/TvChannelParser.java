package com.dyh.movienow.core.parser;

import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.bean.MovieRecommends;
import com.dyh.movienow.ui.daoHang.DaoHangMovieCallBack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2018/10/22
 * 时间：At 14:57
 */
public class TvChannelParser {

    public static void findList(String sourceUrl, String rule, String s, DaoHangMovieCallBack callback) {
        if(rule.startsWith("js:")){
            JsEngineBridge.parseHomeCallBack("", rule, s, false, callback);
            return;
        }
        String baseUrls = sourceUrl.replace("http://", "").replace("https://","");
        String baseUrl2 = baseUrls.split("/")[0];
        String baseUrl;
        if(sourceUrl.startsWith("https")){
            baseUrl = "https://" + baseUrl2;
        }else {
            baseUrl = "http://" + baseUrl2;
        }
        MovieInfoUse movieInfo = new MovieInfoUse();
        movieInfo.setBaseUrl(baseUrl);
        movieInfo.setSearchUrl(sourceUrl);
        try {
            Document doc = Jsoup.parse(s);
            List<MovieRecommends> lists = new ArrayList<>();
            String[] ss = rule.split(";");
            //循环获取
            Elements elements = new Elements();
            String[] ss2 = ss[0].split("&&");
            Element element;
            element = CommonParser.getTrueElement(ss2[0], doc);
            for (int i = 1; i < ss2.length - 1; i++) {
                element = CommonParser.getTrueElement(ss2[i], element);
            }
            elements.addAll(CommonParser.selectElements(element, ss2[ss2.length - 1]));
            //获取详情
            for (Element elementt : elements) {
                try {
                    MovieRecommends listBean = new MovieRecommends();
                    //获取名字
                    String[] ss3 = ss[1].split("&&");
                    Element element2;
                    if (ss3.length == 1) {
                        element2 = elementt;
                    } else {
                        element2 = CommonParser.getTrueElement(ss3[0], elementt);
                    }
                    for (int i = 1; i < ss3.length - 1; i++) {
                        element2 = CommonParser.getTrueElement(ss3[i], element2);
                    }
                    listBean.setTitle(CommonParser.getText(element2, ss3[ss3.length - 1]));
                    //获取链接
                    String[] ss4 = ss[2].split("&&");
                    Element element3;
                    if (ss4.length == 1) {
                        element3 = elementt;
                    } else {
                        element3 = CommonParser.getTrueElement(ss4[0], elementt);
                    }
                    for (int i = 1; i < ss4.length - 1; i++) {
                        element3 = CommonParser.getTrueElement(ss4[i], element3);
                    }
                    listBean.setUrl(CommonParser.getUrl(element3,ss4[ss4.length-1],movieInfo, movieInfo.getSearchUrl()));
                    lists.add(listBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            callback.onSuccess(lists);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.toString());
        }
    }
}
