package com.dyh.movienow.core.player;

/**
 * 作者：By hdy
 * 日期：On 2018/11/18
 * 时间：At 11:15
 */
public class JieXiUtil {
    /**
     * 包裹起来避免直接报错
     * @param url url
     * @return
     */
    public static String tryGetRealUrl(String url) {
        String realUrl = url;
        try {
            realUrl = getRealUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realUrl;
    }

    public static String getRealUrl(String url) {
        String dataurl2 = url;
        String[] txurlc = dataurl2.split(":");
        String txurl = txurlc[1].substring(0, 12);
        String ykurl = txurlc[1].substring(0, 13);
        String ykdata;
        if (ykurl.equals("//m.youku.com")) {
            txurlc = dataurl2.split(":");
            ykdata = txurlc[1].substring(13);
            dataurl2 = "http://www.youku.com" + ykdata;
        } else if (ykurl.equals("//m.iqiyi.com")) {
            txurlc = dataurl2.split(":");
            ykdata = txurlc[1].substring(13);
            dataurl2 = "https://www.iqiyi.com" + ykdata;
        } else if (txurl.equals("//m.v.qq.com")) {
            String vid = getParam(dataurl2, "vid");
            String cid = getParam(dataurl2, "cid");
            String[] txdata2 = dataurl2.split("\\?");
            if (txdata2[0].endsWith("play.html")) {
                if (cid != null && !"".equals(cid)) {
                    dataurl2 = "https://v.qq.com/x/cover/" + cid + ".html";
                    return dataurl2;
                } else if (vid.length() == 11) {
                    //cid 为空，vid正常
                    return "https://v.qq.com/x/page/" + vid + ".html";
                }
            }
            cid = txdata2[0].substring(txdata2[0].length() - 20, txdata2[0].length() - 5);
            if (vid.length() == 11) {
                dataurl2 = "https://v.qq.com/x/cover/" + cid + "/" + vid + ".html";
            } else {
                dataurl2 = "https://v.qq.com/x/cover/" + cid + ".html";
            }
        } else if (ykurl.equals("//m.le.com/vp")) {
            String[] leurlc = dataurl2.split("_");
            String leurl = leurlc[1];
            dataurl2 = "http://www.le.com/ptv/vplay/" + leurl;
        }
        return dataurl2;
    }

    public static String getParam(String url, String vid) {
        if (!url.contains(vid)) {
            return "";
        }
        String[] ss = url.split(vid + "=");
        if (ss.length < 2) {
            return "";
        }
        String[] ss2 = ss[1].split("&");
        if (ss2.length < 2) {
            return ss[1];
        } else {
            return ss2[0];
        }
    }
}
