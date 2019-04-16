package com.dyh.movienow.core.parser;

import android.text.TextUtils;

import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.util.StringUtil;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 作者：By hdy
 * 日期：On 2018/10/22
 * 时间：At 14:47
 */
public class CommonParser {

    private static String[] normalAttrs = {"href", "src", "class", "title", "alt"};

    public static Element getTrueElement(String rule, Element element) {
        if (rule.startsWith("Text") || rule.startsWith("Attr")) {
            return element;
        }
        for (String normalAttr : normalAttrs) {
            if (normalAttr.equals(rule)) {
                return element;
            }
        }
        String[] ors = rule.split("\\|\\|");
        if (ors.length > 1) {
            for (int i = 0; i < ors.length; i++) {
                Element e = null;
                try {
                    e = getTrueElement(ors[i], element);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (e != null) {
                    return e;
                }
            }
        }
        String[] ss01 = rule.split(",");
        if (ss01.length > 1) {
            int index = Integer.parseInt(ss01[1]);
            Elements elements = element.select(ss01[0]);
            if (index < 0) {
                return elements.get(elements.size() + index);
            } else {
                return element.select(ss01[0]).get(index);
            }
        } else return element.select(rule).first();
    }

    public static Elements selectElements(Element element, String rule) {
        String[] ors = rule.split("\\|\\|");
        Elements res = new Elements();
        for (int i = 0; i < ors.length; i++) {
            try {
                res.addAll(selectElementsWithoutOr(element, ors[i]));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return res;
    }

    private static Elements selectElementsWithoutOr(Element element, String rule) {
        String[] rules = rule.split(",");
        if (rules.length > 1) {
            String[] indexNumbs = rules[1].split(":", -1);
            int startPos = 0;
            int endPos = 0;
            if (TextUtils.isEmpty(indexNumbs[0])) {
                startPos = 0;
            } else {
                try {
                    startPos = Integer.parseInt(indexNumbs[0]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (TextUtils.isEmpty(indexNumbs[1])) {
                endPos = 0;
            } else {
                try {
                    endPos = Integer.parseInt(indexNumbs[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            Elements elements = element.select(rules[0]);
            if (endPos > elements.size()) {
                endPos = elements.size();
            }
            if (endPos <= 0) {
                endPos = elements.size() + endPos;
            }
            Elements res = new Elements();
            for (int i = startPos; i < endPos; i++) {
                res.add(elements.get(i));
            }
            return res;
        } else {
            return element.select(rule);
        }
    }

    public static String getText(Element element, String lastRule) {
        if ("*".equals(lastRule)) {
            return "null";
        }
        String[] ors = lastRule.split("\\|\\|");
        if (ors.length > 1) {
            for (int i = 0; i < ors.length; i++) {
                String e = null;
                try {
                    e = getTextWithoutOr(element, ors[i]);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (!TextUtils.isEmpty(e)) {
                    return e;
                }
            }
        }
        return getTextWithoutOr(element, lastRule);
    }

    private static String getTextWithoutOr(Element element, String lastRule) {
        String[] rules = lastRule.split("!");
        String text;
        if (rules.length > 1) {
            if (rules[0].equals("Text")) {
                text = element.text();
            } else if (rules[0].contains("Attr")) {
                text = element.attr(rules[0].replace("Attr", ""));
            } else {
                text = element.select(rules[0]).first().toString();
            }
            text = StringUtil.replaceBlank(text);
            for (int i = 1; i < rules.length; i++) {
                text = text.replace(rules[i], "");
            }
            return text;
        } else {
            if (lastRule.equals("Text")) {
                text = element.text();
            } else if (lastRule.contains("Attr")) {
                text = element.attr(lastRule.replace("Attr", ""));
            } else {
                text = element.attr(lastRule);
//                text = element.select(lastRule).first().toString();
            }
            return StringUtil.replaceBlank(text);
        }
    }

    public static String getUrl(Element element3, String lastRule, MovieInfoUse movieInfoUse, String lastUrl) {
        if ("*".equals(lastRule)) {
            return "null";
        }
        String[] ors = lastRule.split("\\|\\|");
        if (ors.length > 1) {
            for (int i = 0; i < ors.length; i++) {
                String e = null;
                try {
                    e = getUrlWithoutOr(element3, ors[i], movieInfoUse, lastUrl);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (!TextUtils.isEmpty(e)) {
                    return e;
                }
            }
        }
        return getUrlWithoutOr(element3, lastRule, movieInfoUse, lastUrl);
    }

    private static String getUrlWithoutOr(Element element3, String lastRule, MovieInfoUse movieInfoUse, String lastUrl) {
        String js = "";
        String[] ss = lastRule.split("\\.js:");
        if (ss.length > 1) {
            lastRule = ss[0];
            js = ss[1];
        }
        String url;
//        String[] rules = lastRule.split("@js:");
        if (lastRule.startsWith("Text")) {
            url = element3.text();
        } else if (lastRule.startsWith("AttrNo")) {
            url = element3.attr(lastRule.replaceFirst("AttrNo", ""));
            return movieInfoUse.getBaseUrl() + url;
        } else if (lastRule.startsWith("AttrYes")) {
            url = element3.attr(lastRule.replaceFirst("AttrYes", ""));
        } else if (lastRule.startsWith("Attr")) {
            url = element3.attr(lastRule.replaceFirst("Attr", ""));
        } else {
            url = element3.attr(lastRule);
//            url = element3.select(lastRule).first().toString();
        }
        if (TextUtils.isEmpty(js)) {
            url = StringUtil.replaceBlank(url);
        } else {
            try {
                url = JSEngine.getInstance().evalJS(js, url);
            } catch (Exception e) {
                url = StringUtil.replaceBlank(url);
            }
        }
        if (url.startsWith("http")) {
            return url;
        } else if (url.startsWith("//")) {
            return "http:" + url;
        } else if (url.startsWith("magnet") || url.startsWith("thunder") || url.startsWith("ftp") || url.startsWith("ed2k")) {
            return url;
        } else if (url.startsWith("/")) {
            return movieInfoUse.getBaseUrl() + url;
        } else if (url.startsWith("./")) {
            String searchUrl = movieInfoUse.getSearchUrl().split(";")[0];
            String[] c = searchUrl.split("/");
            if (c.length <= 1) {
                return url;
            }
            String sub = searchUrl.replace(c[c.length - 1], "");
            return sub + url.replace("./", "");
        } else if (url.startsWith("?")) {
            return lastUrl + url;
        } else {
            String[] urls = url.split("\\$");
            if (urls.length > 1 && urls[1].startsWith("http")) {
                return urls[1];
            }
            if (url.contains("url(")) {
                String[] urls2 = url.split("url\\(");
                if (urls2.length > 1 && urls2[1].startsWith("http")) {
                    return urls2[1].split("\\)")[0];
                }
            }
            if (movieInfoUse.getBaseUrl().endsWith("/")) {
                return movieInfoUse.getBaseUrl() + url;
            } else {
                return lastUrl + "/" + url;
            }
        }
    }
}
