package com.dyh.movienow.core.parser;

import android.text.TextUtils;

import com.dyh.movienow.bean.MovieChoose;
import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.bean.MovieRecommends;
import com.dyh.movienow.ui.TypeConstant;
import com.dyh.movienow.ui.daoHang.DaoHangMovieCallBack;
import com.dyh.movienow.util.StringUtil;

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
public class HomeParser {

    private static String[] filterWords = {"妻子", "性瘾", "小姨子", "情欲", "岳母", "出轨", "的妈妈", "性爱", "情事", "妻的"};

    private static boolean canAdd(String title) {
        boolean needAdd = true;
        for (String filterWord : filterWords) {
            if (title.contains(filterWord)) {
                needAdd = false;
                break;
            }
        }
        return needAdd;
    }

    public static void findList(MovieChoose movieChoose, String rule, String s, boolean newLoad, DaoHangMovieCallBack callback) {
        if(rule.startsWith("js:")){
            JsEngineBridge.parseHomeCallBack(movieChoose.getColType(), rule, s, newLoad, callback);
            return;
        }
        String baseUrl = StringUtil.getBaseUrl(movieChoose.getUrl());
        MovieInfoUse movieInfo = new MovieInfoUse();
        movieInfo.setBaseUrl(baseUrl);
        movieInfo.setSearchUrl(movieChoose.getUrl());
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
                    if(!TextUtils.isEmpty(movieChoose.getColType())){
                        listBean.setType(movieChoose.getColType());
                    }else {
                        listBean.setType(TypeConstant.HOME_COL_MOVIE_3);
                    }
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
                    //获取图片链接
                    if("*".equals(ss[2])){
                        listBean.setPic("*");
                        if(movieChoose.getColType().equals(TypeConstant.HOME_COL_MOVIE_1)) {
                            listBean.setType(TypeConstant.HOME_COL_TEXT_1);
                        }
                    }else {
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
                        listBean.setPic(CommonParser.getUrl(element3,ss4[ss4.length-1],movieInfo, movieInfo.getChapterUrl()));
                    }
                    //获取详情
                    String[] ss5 = ss[3].split("&&");
                    Element element4;
                    if (ss5.length == 1) {
                        element4 = elementt;
                    } else {
                        element4 = CommonParser.getTrueElement(ss5[0], elementt);
                    }
                    for (int i = 1; i < ss5.length - 1; i++) {
                        element4 = CommonParser.getTrueElement(ss5[i], element4);
                    }
                    listBean.setDesc(CommonParser.getText(element4, ss5[ss5.length - 1]));
                    //获取链接
                    if(ss.length > 4){
                        String[] ss6 = ss[4].split("&&");
                        Element element5;
                        if (ss6.length == 1) {
                            element5 = elementt;
                        } else {
                            element5 = CommonParser.getTrueElement(ss6[0], elementt);
                        }
                        for (int i = 1; i < ss6.length - 1; i++) {
                            element5 = CommonParser.getTrueElement(ss6[i], element5);
                        }
                        listBean.setUrl(CommonParser.getUrl(element5,ss6[ss6.length-1],movieInfo, movieInfo.getChapterUrl()));
                    }
                    if (canAdd(listBean.getTitle())) {
                        lists.add(listBean);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (newLoad) {
                callback.loadNewSuccess(lists);
            } else {
                callback.onSuccess(lists);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.toString());
        }
    }
}
