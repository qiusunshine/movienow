package com.dyh.browser.activity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import com.dyh.browser.plugin.VipManager;
import com.dyh.movienow.base.BaseActivity;
import com.dyh.movienow.core.player.PlayChooser;
import com.dyh.movienow.R;
import com.dyh.movienow.util.HeavyTaskUtil;
import com.dyh.movienow.util.Helper;
import com.dyh.movienow.util.ShareUtil;
import com.dyh.movienow.util.StatusBarUtil;
import com.dyh.movienow.util.ToastMgr;
import com.just.agentwebX5.AgentWebX5;
import com.just.agentwebX5.DefaultWebClient;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/**
 * 作者：By hdy
 * 日期：On 2018/3/13
 * 时间：At 20:10
 */

public class WebActivity extends BaseActivity {
    private AgentWebX5 mAgentWeb;
    private String movieFind;
    private String baseUrl;
    private String text = "";
    private String webUrl;
    private PowerMenu powerMenu;

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_web);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            StatusBarUtil.setColor(WebActivity.this, getResources().getColor(R.color.white));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_right_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_share:
                ShareUtil.findChooserToDeal(getContext(), mAgentWeb.getWebCreator().get().getUrl());
                break;
            case R.id.menu_copy:
                Helper.copyToClipboard(getContext(), mAgentWeb.getWebCreator().get().getUrl());
                ToastMgr.toastShortBottomCenter(getContext(), "已经复制链接到剪贴板");
                break;
            case R.id.menu_xiutan:
                ToastMgr.toastShortCenter(getContext(), "本页面不支持此功能");
                break;
            case R.id.jiexi:
                webUrl = mAgentWeb.getWebCreator().get().getUrl();
                if (webUrl.contains("goudidiao")) {
                    mAgentWeb.getLoader().loadUrl(webUrl);
                    break;
                }
                if (!shouldLoadVIP(webUrl)) {
                    ToastMgr.toastShortCenter(getContext(), "当前网站不可解析");
                    break;
                }
                powerMenu = new PowerMenu.Builder(getContext())
                        .addItem(new PowerMenuItem("解析接口", false))
                        .addItemList(VipManager.getInstance().getItemList())
                        .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT)
                        .setMenuRadius(10f)
                        .setMenuShadow(10f)
                        .setDivider(getResources().getDrawable(R.drawable.divider_power_menu))
                        .setDividerHeight(Helper.dpToPx(getContext(), 1))
                        .setSelectedTextColor(getContext().getResources().getColor(R.color.redColor))
                        .setSelectedMenuColor(getContext().getResources().getColor(R.color.white))
                        .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                            @Override
                            public void onItemClick(int position, PowerMenuItem item) {
                                position = position - 1;
                                if (position < 0) {
                                    return;
                                }
                                if (webUrl.contains("youku.com") || webUrl.contains("v.qq.com")) {
                                    webUrl = webUrl.split(".html")[0] + ".html";
                                }
                                String url = VipManager.getInstance().getUrlList().get(position);
                                powerMenu.dismiss();
                                mAgentWeb.getLoader().loadUrl(url.replace("**", webUrl));
                                VipManager.getInstance().setSelect(position);
                            }
                        }).build();
                powerMenu.showAsDropDown(findView(R.id.jiexi));
//                mAgentWeb.getLoader().loadUrl("http://goudidiao.com/?url=" + webUrl);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initView() {
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        VipManager.getInstance();
        mAgentWeb = AgentWebX5.with(this)
                .setAgentWebParent((LinearLayout) findView(R.id.web_container), new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setIndicatorColor(getResources().getColor(R.color.redColor))
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .addJavascriptInterface("androidss", new VideoInterface(mAgentWeb, getContext()))
                .interceptUnkownScheme() //拦截找不到相关页面的Scheme
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(webViewClient)
                .createAgentWeb()
                .ready()
                .go(getIntent().getStringExtra("url"));
        movieFind = getIntent().getStringExtra("movieFind");
        baseUrl = getIntent().getStringExtra("baseUrl");
        mAgentWeb.getWebCreator().get().setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                ShareUtil.startUrl(getContext(), url);
            }
        });
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            setTitle(s);
        }
    };
    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            String url = webView.getUrl();
            if (url.contains("m.icantv.cn")) {
                String js = "javascript:function clearTvAd(){\n" +
                        "\tvar body_element = document.getElementsByTagName(\"body\")[0];\n" +
                        "\tvar alls=body_element.children;\n" +
                        "\tfor(var i=0;i<alls.length;i++){\n" +
                        "       if(alls[i].className!==\"wrap\"){\n" +
                        "          alls[i].style.display=\"none\";\n" +
                        "       }\n" +
                        "    }\n" +
                        "    var e=document.getElementsByClassName(\"wrap\")[0];\n" +
                        "    var childs=e.children;\n" +
                        "    for(var i=0;i<childs.length;i++){\n" +
                        "       if(childs[i].getAttribute('id')!==\"play_player\"&&childs[i].className!==\"line\"){\n" +
                        "          childs[i].style.display=\"none\";\n" +
                        "       }\n" +
                        "    }\n" +
                        "   var e2=document.getElementsByClassName(\"footer_content\")[0];\n" +
                        "   e2.style.display=\"none\";\n" +
                        "}\n" +
                        "clearTvAd();";
                webView.loadUrl(js);
            } else if (url.contains("goudidiao")) {
                String js = "javascript:$(\".container\").children().hide();\n" +
                        "var fy_form = $(\"form\").eq(0);\n" +
                        "fy_form.siblings().hide(); \n" +
                        "fy_form.parent().show();\n" +
                        "$(\"#play_iframe\").parent().parent().show();";
                webView.loadUrl(js);
            } else if (baseUrl != null && !baseUrl.equals("") && url.contains(baseUrl)) {
                String js = "javascript:function sendVideoUrl(){" +
                        movieFind +
                        "}sendVideoUrl();";
                webView.loadUrl(js);
            }
            HeavyTaskUtil.saveHistory(getContext(), "网页浏览", s, webView.getTitle());
            super.onPageFinished(webView, s);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String url = request.getUrl().getPath();
                if ((url.contains("gif") || url.contains("GIF") || url.contains("cnzz.c"))) {
                    return new WebResourceResponse(null, null, null);
                }
                /**
                 * if ((webUrl.contains(".jpg") || webUrl.contains(".JPG") || webUrl.contains(".JPEG") || webUrl.contains(".jpeg"))) {
                 return new WebResourceResponse(null, null, null);
                 }
                 */
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            DetectorManager.getInstance().addTask(new Video(url,url));
            if ((url.contains("gif") || url.contains("GIF") || url.contains("cnzz.c"))) {
                return new WebResourceResponse(null, null, null);
            }
            /**
             * if ((webUrl.contains(".jpg") || webUrl.contains(".JPG") || webUrl.contains(".JPEG") || webUrl.contains(".jpeg"))) {
             return new WebResourceResponse(null, null, null);
             }
             */
            return super.shouldInterceptRequest(view, url);
        }
    };

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        ShareUtil.copyToClipboard(getContext(), text);
        super.onPause();
    }

    @Override
    public void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        text = Helper.getTextFromClipBoard(getContext());
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mAgentWeb.getWebCreator().get().clearCache(true);
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mAgentWeb.handleKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * 视频和图片链接处理
     */
    private class VideoInterface {
        private AgentWebX5 agent;
        private Context context;

        public VideoInterface(AgentWebX5 agent, Context context) {
            this.agent = agent;
            this.context = context;
        }

        private Handler deliver = new Handler(Looper.getMainLooper());

        @JavascriptInterface
        public void sendVideoUrl(final String videourl) {
            deliver.post(new Runnable() {
                @Override
                public void run() {
                    if (!videourl.equals("")) {
                        PlayChooser.startPlayer(getContext(), mAgentWeb.getWebCreator().get().getTitle(), videourl);
                    }
                }
            });
        }

        @JavascriptInterface
        public void sendMsg(final String msg) {
            deliver.post(new Runnable() {
                @Override
                public void run() {
                    ToastMgr.toastShortCenter(getContext(), msg);
                    Log.w("看看看", msg);
                }
            });
        }
    }

    public boolean shouldLoadVIP(String url) {
        String urls = "iqiyi.com youku.com le.com letv.com v.qq.com tudou.com mgtv.com film.sohu.com tv.sohu.com acfun.cn bilibili.com pptv.com vip.1905.com yinyuetai.com fun.tv 56.com";
        String[] u = urls.split(" ");
        for (int i = 0; i < u.length; i++) {
            if (url.contains(u[i])) {
                return true;
            }
        }
        return false;
    }
}
