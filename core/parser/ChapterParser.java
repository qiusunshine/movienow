package com.dyh.movienow.core.parser;

import com.dyh.movienow.bean.ChapterBean;
import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.ui.chapter.ChapterCallback;

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
public class ChapterParser {
//    private static String[] filters = {"播放","地址","源","来源","接口"};

//    private static String getfilteredTitle(String name){
//        for (int i = 0; i < filters.length; i++) {
//            name = name.replace(filters[i],"");
//        }
//        return name;
//    }
    public static void findList(MovieInfoUse movieInfo, String s, ChapterCallback callback){
        if(movieInfo.getChapterFind().startsWith("js:")){
            JsEngineBridge.parseCallBack(movieInfo, s, callback);
            return;
        }
        try {
            boolean shouldGetDesc = false;
            Document doc = Jsoup.parse(s);
            List<ChapterBean> lists=new ArrayList<>();
            String[] ss = movieInfo.getChapterFind().split(";");
            if(ss.length==6){
                shouldGetDesc = true;
            }
            ChapterBean listBeanHeader=new ChapterBean();
            listBeanHeader.setType(1);
            //DebugUtil.log(doc.toString());
            //获取图片链接
            if(ss[0].startsWith("*")){
                listBeanHeader.setUrl("*");
            }else {
                String[] ss0 = ss[0].split("&&");
                Element element0;
                element0= CommonParser.getTrueElement(ss0[0],doc);
                for (int i = 1; i < ss0.length-1; i++) {
                    element0= CommonParser.getTrueElement(ss0[i],element0);
                }
                listBeanHeader.setUrl(CommonParser.getUrl(element0,ss0[ss0.length-1],movieInfo.getBaseUrl(),movieInfo.getChapterUrl()));
            }
            //获取简介
            String[] ss1 = ss[1].split("&&");
            Element element1;
            element1= CommonParser.getTrueElement(ss1[0],doc);
            for (int i = 1; i < ss1.length-1; i++) {
                element1= CommonParser.getTrueElement(ss1[i],element1);
            }
            listBeanHeader.setTitle(CommonParser.getText(element1,ss1[ss1.length-1]));
            lists.add(listBeanHeader);
            //获取描述---哪个源
            String[] descs = null;
            if(shouldGetDesc){
                try {
                    if(ss[5].contains("@")){
                        String[] descAll = ss[5].split("@");
                        String[] descPre = descAll[0].split("&&");
                        Element elementDesc = CommonParser.getTrueElement(descPre[0],doc);
                        for (int i = 1; i < descPre.length-1; i++) {
                            elementDesc= CommonParser.getTrueElement(descPre[i],elementDesc);
                        }
                        Elements elementDescLast = CommonParser.selectElements(elementDesc,descPre[descPre.length-1]);
                        descs = new String[elementDescLast.size()];
                        for (int i = 0; i < elementDescLast.size(); i++) {
                            String[] descSuf = descAll[1].split("&&");
                            Element elementDesc2 = CommonParser.getTrueElement(descSuf[0],elementDescLast.get(i));
                            for (int j = 1; i < descSuf.length-1; j++) {
                                elementDesc2= CommonParser.getTrueElement(descSuf[j],elementDesc2);
                            }
                            descs[i] = CommonParser.getText(elementDesc2,descSuf[descSuf.length-1]);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //循环获取集数
            List<String> descStrs = new ArrayList<>();
            Elements elements = new Elements();
            if(ss[2].contains("@")){
                String[] kkk = ss[2].split("@");
                String[] kkk2 = kkk[0].split("&&");
                Element element;
                element= CommonParser.getTrueElement(kkk2[0],doc);
                for (int i = 1; i < kkk2.length-1; i++) {
                    //Log.w("调试调试", "findList: element="+ element.toString());
                    element= CommonParser.getTrueElement(kkk2[i],element);
                }
                Elements elements1 = CommonParser.selectElements(element,kkk2[kkk2.length-1]);
                for (int i = 0; i < elements1.size(); i++) {
                    Elements tElements = CommonParser.selectElements(elements1.get(i), kkk[1]);
                    if(descs!=null && i<descs.length){
                        for (int j = 0; j < tElements.size(); j++) {
                            descStrs.add(descs[i]);
                        }
                    }
                    elements.addAll(tElements);
                }
            }else {
                String[] ss2 = ss[2].split("&&");
                Element element;
                element= CommonParser.getTrueElement(ss2[0],doc);
                for (int i = 1; i < ss2.length-1; i++) {
                    //Log.w("调试调试", "findList: element="+ element.toString());
                    element= CommonParser.getTrueElement(ss2[i],element);
                }
                elements.addAll(CommonParser.selectElements(element,ss2[ss2.length-1]));
            }
            //获取集数和链接
            int k = 0;
            for (Element elementt : elements) {
                try {
                    ChapterBean listBean=new ChapterBean();
                    listBean.setType(2);
                    //获取集数名字
                    String[] ss3 = ss[3].split("&&");
                    Element element2;
                    if(ss3.length==1){
                        element2=elementt;
                    }else element2= CommonParser.getTrueElement(ss3[0],elementt);
                    for (int i = 1; i < ss3.length-1; i++) {
                        element2= CommonParser.getTrueElement(ss3[i],element2);
                    }
                    listBean.setTitle(CommonParser.getText(element2,ss3[ss3.length-1]));
                    //获取集数链接
                    String[] ss4 = ss[4].split("&&");
                    Element element3;
                    if(ss4.length==1){
                        element3=elementt;
                    }else element3= CommonParser.getTrueElement(ss4[0],elementt);
                    for (int i = 1; i < ss4.length-1; i++) {
                        element3= CommonParser.getTrueElement(ss4[i],element3);
                    }
                    listBean.setUrl(CommonParser.getUrl(element3, ss4[ss4.length-1], movieInfo.getBaseUrl(), movieInfo.getChapterUrl()));
                    if(k<descStrs.size()){
                        if(k==0||!descStrs.get(k).equals(descStrs.get(k-1))){
                            ChapterBean chapterBean=new ChapterBean();
                            chapterBean.setType(5);
                            chapterBean.setTitle(descStrs.get(k));
                            lists.add(chapterBean);
                        }
                        listBean.setDesc(descStrs.get(k));
                    }
                    lists.add(listBean);
                    k++;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            callback.loadSuccess(lists);//返回结果
        } catch (Exception e) {
            e.printStackTrace();
            callback.loadFail(e.toString());
        }
    }
}
