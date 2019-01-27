package com.dyh.browser.activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dyh.browser.plugin.HostManager;
import com.dyh.browser.plugin.VipManager;
import com.dyh.movienow.R;
import com.dyh.movienow.base.BaseActivity;
import com.dyh.movienow.bean.Video;
import com.dyh.movienow.core.event.FindVideoEvent;
import com.dyh.movienow.core.player.DetectorManager;
import com.dyh.movienow.core.player.JieXiUtil;
import com.dyh.movienow.core.player.PlayChooser;
import com.dyh.movienow.ui.event.BackToChapterEvent;
import com.dyh.movienow.ui.event.LoadNextMovieEvent;
import com.dyh.movienow.ui.home.MainUtil;
import com.dyh.movienow.ui.setting.entity.VideoInfo;
import com.dyh.movienow.util.DebugUtil;
import com.dyh.movienow.util.FileUtils;
import com.dyh.movienow.util.HeavyTaskUtil;
import com.dyh.movienow.util.Helper;
import com.dyh.movienow.util.PreferenceMgr;
import com.dyh.movienow.util.ShareUtil;
import com.dyh.movienow.util.StringUtil;
import com.dyh.movienow.util.ToastMgr;
import com.dyh.movienow.view.CornerImageView;
import com.dyh.movienow.view.PowerSingleLineMenu;
import com.dyh.movienow.view.ScrollWebView;
import com.maning.updatelibrary.InstallUtils;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.trycatch.mysnackbar.Prompt;
import com.trycatch.mysnackbar.TSnackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.refactor.lib.colordialog.PromptDialog;
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 作者：By hdy
 * 日期：On 2018/3/13
 * 时间：At 20:10
 */

public class WebViewActivity extends BaseActivity implements View.OnClickListener {
    private ScrollWebView webViewT;
    private ProgressBar bar;
    private String movieFind;
    private String baseUrl;
    private String text = "";
    private String webUrl;
    private PowerMenu powerMenu;
    private boolean isUsePlayer = false;
    private boolean hasDismissXiuTan = false;
    private String[] videoWebs;
    private boolean isOnPause;
    private PowerSingleLineMenu videoListMenu;
    //视频全屏参数
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS
            = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private String updateApkPath;
    private Map<String, String> jsStrs = new HashMap<>();
    private ClipboardManager.OnPrimaryClipChangedListener hasPrimaryClip;
    private long previousTime = 0;
    private boolean isDebug = false;
    private Toolbar toolbar;
    private ViewGroup viewGroup;//注意getRootView()最为重要，直接关系到TSnackBar的位置
    private TSnackbar tSnackbar;
    private boolean isXiuTan = false;
    private boolean blockImg;
    private boolean needCache = false;
    private TextView bottomTitleView;
    private View bottomBar;
    private boolean isXiuTanAutoPlayMode = true;
    private CornerImageView bottomBarXiuTan;
    private boolean isConfirm;
    private String movieTitle;
    private boolean hasAutoPlay = false;

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.browser_ac_web);
    }

    @Override
    protected void initView() {
        try {
            toolbar = (Toolbar) findView(R.id.web3_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bar = findView(R.id.myProgressBar3);
        FrameLayout frameLayout = findView(R.id.web_container);
        webViewT = new ScrollWebView(getContext());
        frameLayout.addView(webViewT, 0);
        webViewT.setFocusable(true);
        webViewT.setFocusableInTouchMode(true);
        String urls = ".iqiyi.com .youku.com .le.com .letv.com v.qq.com .tudou.com .mgtv.com film.sohu.com tv.sohu.com .acfun.cn .bilibili.com .pptv.com vip.1905.com .yinyuetai.com .fun.tv .56.com";
        videoWebs = urls.split(" ");
        blockImg = false;
        bottomTitleView = findView(R.id.bottom_bar_title);
        bottomBar = findView(R.id.bottom_bar_bg);
        findView(R.id.bottom_bar_jie_xi).setOnClickListener(this);
        findView(R.id.bottom_bar_refresh).setOnClickListener(this);
        findView(R.id.bottom_bar_xiu_tan_bg).setOnClickListener(this);
        bottomBarXiuTan = findView(R.id.bottom_bar_xiu_tan);
        bottomBarXiuTan.setCornerText("0");
        bottomTitleView.setText("loading");
        bottomTitleView.setOnClickListener(this);
        movieTitle = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(movieTitle)) {
            movieTitle = "";
        }
        //初始化WebSettings
        initWebSettings();
        //初始化WebView
        initWebView();
        //初始化加载参数
        startLoadUrl();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_bar_refresh:
                webViewT.reload();
                break;
            case R.id.bottom_bar_title:
                Helper.copyToClipboard(getContext(), webViewT.getUrl());
                ToastMgr.toastShortBottomCenter(getContext(), "已经复制链接到剪贴板");
                break;
            case R.id.bottom_bar_jie_xi:
                try {
                    tryJieXi();
                } catch (Exception e) {
                    e.printStackTrace();
                    jieXiUseDialog();
                }
                break;
            case R.id.bottom_bar_xiu_tan_bg:
                if (!isUsePlayer) {
                    showVideoList();
                } else {
                    ToastMgr.toastShortCenter(getContext(), "当前页面不支持此功能");
                }
                break;
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        //初始化监听剪贴板
        final ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        hasPrimaryClip = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                long now = System.currentTimeMillis();
                if (now - previousTime < 5000) {
                    return;
                }
                previousTime = now;
                if (manager == null || !manager.hasPrimaryClip()) {
                    return;
                }
                //如果是文本信息
                if (manager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        || manager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                    ClipData cdText = manager.getPrimaryClip();
                    ClipData.Item item = cdText.getItemAt(0);
                    //此处是TEXT文本信息
                    if (item.getText() != null) {
                        final String t = item.getText().toString();
                        Snackbar.make(webViewT, "剪贴板被使用，是否撤销内容", Snackbar.LENGTH_LONG)
                                .setAction("撤销", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Helper.copyToClipboard(getContext(), text);
                                    }
                                }).addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                text = t;
                                super.onDismissed(transientBottomBar, event);
                            }
                        }).show();
                    }
                }
            }
        };
        if (manager != null) {
            manager.addPrimaryClipChangedListener(hasPrimaryClip);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        bottomTitleView.setText(webViewT.getUrl());
    }

    @Override
    public void onPause() {
        try {
            if (webViewT != null) {
                webViewT.onPause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isOnPause = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        text = Helper.getTextFromClipBoard(getContext());
        try {
            if (isOnPause) {
                if (webViewT != null) {
                    webViewT.onResume();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isOnPause = false;
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (manager != null) {
            manager.removePrimaryClipChangedListener(hasPrimaryClip);
        }
        try {
            releaseWebViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void backToChapter(BackToChapterEvent event) {
        if(isXiuTan) {
            ToastMgr.toastShortBottomCenter(getContext(), "正在自动帮您跳转");
            tryBackToChapter(0, event.isLast());
        }
    }

    private void tryBackToChapter(final int times, final boolean last) {
        if (times > 5) return;
        if (isOnPause) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryBackToChapter(times + 1, last);
                }
            }, 500);
        } else {
            EventBus.getDefault().post(new LoadNextMovieEvent(last));
            finish();
        }
    }

    private synchronized void releaseWebViews() {
        if (webViewT != null) {
            try {
                if (webViewT.getParent() != null) {
                    ((ViewGroup) webViewT.getParent()).removeView(webViewT);
                }
                webViewT.destroy();
            } catch (IllegalArgumentException ignored) {

            }
            webViewT = null;
        }
    }

    @Override
    public void onBackPressed() {
        /** 回退键 事件处理 优先级:视频播放全屏-网页回退-关闭页面 */
        if (customView != null) {
            hideCustomView();
        } else if (webViewT.canGoBack()) webViewT.goBack();
        else finish();
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
            case R.id.hide_bottom_bar:
                if (bottomBar.getVisibility() == GONE) {
                    bottomBar.setVisibility(VISIBLE);
                    bottomBar.animate().alpha(0).setDuration(300).start();
                    break;
                }
                bottomBar.setVisibility(GONE);
                bottomBar.animate().alpha(1).setDuration(300).start();
                break;
            case R.id.menu_clear_all:
                webViewT.loadUrl("javascript:localStorage.clear()");
                ToastMgr.toastShortBottomCenter(getContext(), "已清除完毕");
                break;
            case R.id.menu_share:
                ShareUtil.findChooserToDeal(getContext(), webViewT.getUrl());
                break;
            case R.id.menu_copy:
                Helper.copyToClipboard(getContext(), webViewT.getUrl());
                ToastMgr.toastShortBottomCenter(getContext(), "已经复制链接到剪贴板");
                break;
            case R.id.menu_debug:
                isDebug = true;
                webViewT.reload();
                break;
            case R.id.fresh:
                webViewT.reload();
                break;
            case R.id.menu_xiutan:
                if (!isUsePlayer) {
                    showVideoList();
                } else {
                    ToastMgr.toastShortCenter(getContext(), "当前页面不支持此功能");
                }
                break;
            case R.id.jiexi:
                try {
                    tryJieXi();
                } catch (Exception e) {
                    e.printStackTrace();
                    jieXiUseDialog();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void jieXiUseDialog() {
        webUrl = webViewT.getUrl();
        if (webUrl == null) {
            return;
        }
        if (!shouldLoadVIP(webUrl)) {
            ToastMgr.toastShortCenter(getContext(), "当前网站不可解析");
            return;
        }
        List<PowerMenuItem> items = VipManager.getInstance().getItemList();
        String[] name = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            name[i] = items.get(i).getTitle();
        }
        new AlertDialog.Builder(this).setTitle("选择解析接口")
                .setSingleChoiceItems(name
                        , 0,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (webUrl.contains("youku.com") || webUrl.contains("v.qq.com")) {
                                    webUrl = webUrl.split(".html")[0] + ".html";
                                }
                                String url = VipManager.getInstance().getUrlList().get(which);
                                dialog.dismiss();
                                webViewT.loadUrl(url.replace("**", JieXiUtil.tryGetRealUrl(webUrl)));
                                VipManager.getInstance().setSelect(which);
                            }
                        }).setNegativeButton("取消", null).show();
    }

    private void tryJieXi() {
        webUrl = webViewT.getUrl();
        if (webUrl == null) {
            return;
        }
        if (!shouldLoadVIP(webUrl)) {
            ToastMgr.toastShortCenter(getContext(), "当前网站不可解析");
            return;
        }
        List<PowerMenuItem> items = VipManager.getInstance().getItemList();
        powerMenu = new PowerMenu.Builder(getContext())
                .addItem(new PowerMenuItem(items.size() == 0 ? "接口正在初始化，请重试" : "解析接口", false))
                .addItemList(items)
                .setAnimation(MenuAnimation.DROP_DOWN) // Animation start point (TOP | LEFT)
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
                        webViewT.loadUrl(url.replace("**", JieXiUtil.tryGetRealUrl(webUrl)));
                        VipManager.getInstance().setSelect(position);
                    }
                }).build();
        powerMenu.showAsDropDown(toolbar, toolbar.getWidth(), 0);
    }

    private void loadDebugJs() {
        if (!isDebug) {
            return;
        }
        webViewT.evaluateJavascript(getJs("console.js"), null);
    }

    /**
     * 检测到视频
     *
     * @param videoEvent 视频
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFindVideoEvent(FindVideoEvent videoEvent) {
        if (tSnackbar != null) {
            try {
                tSnackbar.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bottomBar.getVisibility() == GONE) {
            bottomBar.setVisibility(VISIBLE);
            bottomBar.animate().alpha(1).setDuration(300).start();
        }
        bottomBarXiuTan.setCornerText(videoEvent.getTitle());
        if (isXiuTanAutoPlayMode) {
            String dom = StringUtil.getDom(webViewT.getUrl());
            String url = StringUtil.getDom(videoEvent.getUrl());
            if (DetectorManager.getInstance().inXiuTanLiked(getContext(), dom, url)) {
                int code = getIntent().getIntExtra("uWho", 0);
                if (TextUtils.isEmpty(movieTitle)) movieTitle = webViewT.getTitle();
                if (code == 0 && !isOnPause && !hasAutoPlay) {
                    hasAutoPlay = true;
                    ToastMgr.toastShortBottomCenter(getContext(), "已自动播放常用的嗅探地址");
                    PlayChooser.startPlayer(getContext(), movieTitle, videoEvent.getUrl());
                } else if (code == 304 && !isOnPause && !hasAutoPlay) {
                    hasAutoPlay = true;
                    ToastMgr.toastShortBottomCenter(getContext(), "已自动播放常用的嗅探地址");
                    Intent intent = new Intent();
                    intent.putExtra("videourl", videoEvent.getUrl());
                    intent.putExtra("title", movieTitle);
                    setResult(code, intent);
                    finish();
                }
            }
        }
        if (!hasDismissXiuTan) {
            hasDismissXiuTan = true;
            if (!hasAutoPlay) {
                new PromptDialog(this)
                        .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                        .setAnimationEnable(true)
                        .setTitleText("方圆嗅探")
                        .setContentText("网页中检测到" + videoEvent.getTitle() + "条视频链接，点击查看按钮查看或者播放视频链接")
                        .setPositiveListener("查看视频", new PromptDialog.OnPositiveListener() {
                            @Override
                            public void onClick(PromptDialog dialog) {
                                dialog.dismiss();
                                showVideoList();
                            }
                        }).show();
            }
        }
    }

    private void startLoadUrl() {
        movieFind = getIntent().getStringExtra("movieFind");
        baseUrl = getIntent().getStringExtra("baseUrl");
        isUsePlayer = getIntent().getBooleanExtra("isUsePlayer", false);
        isXiuTanAutoPlayMode = (boolean) PreferenceMgr.get(getContext(), "xiuTanAutoPlayMode", true);
        if (isUsePlayer) {
            String text = FileUtils.getAssetsString("DPlayer.html", getContext());
            String url = getIntent().getStringExtra("url");
            text = text.replace("fy_player_url", url);
            FileUtils.write(getContext(), "video.html", text);
            String path = "file://" + FileUtils.getFilePath(getContext(), "video.html");
            webViewT.loadUrl(path);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            isXiuTan = getIntent().getBooleanExtra("is_xiu_tan", false);
            if (isXiuTan) {
                needCache = true;
                showXiuTanMessage();
            } else {
                needCache = false;
            }
            String loadUrl = getIntent().getStringExtra("url");
            webViewT.loadUrl(loadUrl);
        }
        if (!isUsePlayer) {
            DetectorManager.getInstance().createThread();
        }
    }

    private void showXiuTanMessage() {
        boolean blockImgForXiuTan = (boolean) PreferenceMgr.get(getContext(), "blockImgForXiuTan", true);
        if (blockImgForXiuTan) {
            blockImg = true;
        }
        if (viewGroup == null) {
            try {
                viewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (viewGroup == null) {
            return;
        }
        if (tSnackbar == null) {
            tSnackbar = TSnackbar.make(viewGroup, "正在嗅探中...请稍候", TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
            tSnackbar.setPromptThemBackground(Prompt.SUCCESS);
            tSnackbar.addIconProgressLoading(0, true, false);
            tSnackbar.setAction("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tSnackbar.dismiss();
                }
            });
        }
        tSnackbar.show();
    }

    private void initWebView() {
        webViewT.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                final String surl = url;
                DetectorManager.getInstance().addTask(new Video(url, url));
                Snackbar.make(webViewT, "是否允许网页中的下载请求？", Snackbar.LENGTH_LONG)
                        .setAction("允许", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (surl.contains(".apk")) {
                                    MainUtil.DownLoadApk(getContext(), surl, new MainUtil.OkListener() {
                                        @Override
                                        public void onClickOk(String arg) {
                                            updateApkPath = arg;
                                            install();
                                        }
                                    });
                                } else {
                                    ShareUtil.findChooserToDeal(getContext(), surl);
                                }
                            }
                        }).show();
            }
        });
        webViewT.setWebChromeClient(mWebChromeClient);
        webViewT.setWebViewClient(webViewClient);
//        webViewT.addJavascriptInterface(new VideoInterface(), "fy_bridge_app");
        webViewT.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final WebView.HitTestResult result = webViewT.getHitTestResult();
                int type = result.getType();
                if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    Snackbar.make(view, "是否隐藏该图片？", Snackbar.LENGTH_LONG)
                            .setAction("隐藏", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String js = "(function(){ var videoElements = document.getElementsByTagName(\"img\");for(var i = 0;i < videoElements.length; i++) {var videoSrc = videoElements[i].src;if(videoSrc==\"" + result.getExtra() + "\"){videoElements[i].style.display=\"none\";break;};};})();";
                                    webViewT.evaluateJavascript(js, null);
                                }
                            }).show();
                }
                return false;
            }
        });
        webViewT.setOnScrollChangeListener(new ScrollWebView.OnScrollChangeListener() {
            @Override
            public void onPageEnd(int l, int t, int oldl, int oldt) {
            }

            @Override
            public void onPageTop(int l, int t, int oldl, int oldt) {
            }

            @Override
            public void onScrollChanged(int l, int t, int dx_change, int dy_change) {
                // webview的高度
                if (dy_change > 50) {
                    //上滑隐藏
                    if (bottomBar.getVisibility() == VISIBLE) {
                        bottomBar.setVisibility(GONE);
                        bottomBar.animate().alpha(0).setDuration(300).start();
                    }
                } else if (dy_change < -50) {
                    //下滑显示
                    if (bottomBar.getVisibility() == GONE) {
                        bottomBar.setVisibility(VISIBLE);
                        bottomBar.animate().alpha(1).setDuration(300).start();
                    }
                }
            }
        });
        if (getIntent().getIntExtra("uWho", 0) == 304) {
            webViewT.clearCache(false);
        }
    }

    private void initWebSettings() {
        WebSettings webSettings = webViewT.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 240) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == 160) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else if (mDensity == 120) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            if (!getIntent().getStringExtra("url").equals(webView.getUrl())) {
                movieTitle = movieTitle + "_" + s;
            } else if (TextUtils.isEmpty(movieTitle)) {
                movieTitle = s;
            }
            setTitle(s);
        }

        @Override
        public void onProgressChanged(WebView webView, int i) {
            super.onProgressChanged(webView, i);
            if (i == 100) {
                bar.setVisibility(View.INVISIBLE);
            } else {
                if (bar.getVisibility() == View.INVISIBLE) {
                    bar.setVisibility(VISIBLE);
                }
                bar.setProgress(i);
            }
            if (tSnackbar != null) {
                tSnackbar.setText("正在嗅探中" + i + "%...请稍候");
            }
        }

        /*** 视频播放相关的方法 **/
        @Override
        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(getContext());
            frameLayout.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            return frameLayout;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            showCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            hideCustomView();
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    .setAction("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            result.confirm();
                        }
                    }).show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            isConfirm = false;
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    .setAction("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isConfirm = true;
                            result.confirm();
                        }
                    }).addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (!isConfirm) {
                        result.cancel();
                    }
                    super.onDismissed(transientBottomBar, event);
                }
            }).show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_web_add, null, false);
            final EditText titleE = view1.findViewById(R.id.web_add_title);
            EditText urlE = view1.findViewById(R.id.web_add_url);
            titleE.setHint("message");
            urlE.setHint("请在这里输入");
            titleE.setText(message);
            urlE.setText(defaultValue);
            new AlertDialog.Builder(getContext()).setTitle("来自网页的输入请求").setView(view1)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String value = titleE.getText().toString();
                            result.confirm(value);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    }).show();
            return true;
        }
    };

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Snackbar.make(webViewT, "证书校验失败，是否回撤网页？", Snackbar.LENGTH_LONG)
                    .setAction("回撤", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    }).show();
            handler.proceed();
        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            if (bottomBar.getVisibility() == GONE) {
                bottomBar.setVisibility(VISIBLE);
                bottomBar.animate().alpha(1).setDuration(300).start();
            }
            bottomBarXiuTan.setCornerText("0");
            hasAutoPlay = false;
            webViewT.getSettings().setBlockNetworkImage(true);
            hasDismissXiuTan = false;
            if (!isUsePlayer) {
                DetectorManager.getInstance().startDetect();
            }
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            if (!blockImg) {
                webViewT.getSettings().setBlockNetworkImage(false);
            }
//            else {
//                //下一次就不再拦截，比如刷新了网页
//                blockImg = false;
//            }
            if (baseUrl != null && !baseUrl.equals("") && s.contains(baseUrl)) {
                String js = "(function() {" +
                        movieFind +
                        "})();";
                webViewT.evaluateJavascript(js, null);
            }
            try {
                loadMyJs(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadDebugJs();
            if (!isXiuTan) {
                HeavyTaskUtil.saveHistory(getContext(), "网页浏览", s, webView.getTitle());
            }
            super.onPageFinished(webView, s);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String url = request.getUrl().toString();
                if (HostManager.getInstance().shouldIntercept(url)) {
                    return new WebResourceResponse(null, null, null);
                } else {
                    if (!isUsePlayer) {
                        DetectorManager.getInstance().addTask(new Video(url, url));
                    }
                    if (needCache) {
                        return WebViewCacheInterceptorInst.getInstance().interceptRequest(request);
                    } else {
                        return super.shouldInterceptRequest(view, request);
                    }
                }
            }
            if (needCache) {
                return WebViewCacheInterceptorInst.getInstance().interceptRequest(request);
            } else {
                return super.shouldInterceptRequest(view, request);
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (HostManager.getInstance().shouldIntercept(url)) {
                    return new WebResourceResponse(null, null, null);
                } else {
                    if (!isUsePlayer) {
                        DetectorManager.getInstance().addTask(new Video(url, url));
                    }
                }
            }
            if (needCache) {
                return WebViewCacheInterceptorInst.getInstance().interceptRequest(url);
            } else {
                return super.shouldInterceptRequest(view, url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            if (url.startsWith("http")) {
                return super.shouldOverrideUrlLoading(view, url);
            } else {
                Snackbar.make(webViewT, "是否允许网页打开外部应用？", Snackbar.LENGTH_LONG)
                        .setAction("允许", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ShareUtil.findChooserToDeal(getContext(), url);
                            }
                        }).show();
                return true;
            }
        }
    };

    /**
     * 视频播放全屏 函数集合
     **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {

        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        getWindow().getDecorView();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(getContext());
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        customViewCallback = callback;
        webViewT.setVisibility(View.INVISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setStatusBarVisibility(false);
    }

    /**
     * 隐藏视频全屏
     */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        webViewT.setVisibility(VISIBLE);
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    private void showVideoList() {
        List<PowerMenuItem> items = new ArrayList<>();
        final List<VideoInfo> videoInfos = new ArrayList<>(DetectorManager.getInstance().getVideoList());
        if (videoInfos.size() < 1) {
            ToastMgr.toastShortCenter(getContext(), "还没有嗅探到视频，请稍候重试");
            return;
        }
        for (int i = 0; i < videoInfos.size(); i++) {
            String detectImageType = videoInfos.get(i).getDetectImageType();
            String title;
            if (TextUtils.isEmpty(detectImageType)) {
                title = "视频" + (i + 1) + "：" + videoInfos.get(i).getSourcePageTitle();
            } else {
                title = "[" + detectImageType + "]视频" + (i + 1) + "：" + videoInfos.get(i).getSourcePageTitle();
            }
            PowerMenuItem item = new PowerMenuItem(title, false);
            items.add(item);
        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        videoListMenu = new PowerSingleLineMenu.Builder(getContext())
                .addItem(new PowerMenuItem("嗅探到的视频，点击播放", false))
                .addItemList(items)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER) // Animation start point (TOP | LEFT)
                .setMenuRadius(10f).setMenuShadow(10f)
                .setDivider(getResources().getDrawable(R.drawable.divider_power_menu))
                .setWith((int) ((float) dm.widthPixels * 0.8))
                .setSelectedTextColor(getContext().getResources().getColor(R.color.redColor))
                .setSelectedMenuColor(getContext().getResources().getColor(R.color.white))
                .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int position, PowerMenuItem item) {
                        try {
                            videoListMenu.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (position == 0) return;
                            int code = getIntent().getIntExtra("uWho", 0);
                            VideoInfo videoInfo = videoInfos.get(position - 1);
                            String dom = StringUtil.getDom(webViewT.getUrl());
                            String url = StringUtil.getDom(videoInfo.getSourcePageUrl());
                            DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, url);
                            if (TextUtils.isEmpty(movieTitle)) movieTitle = webViewT.getTitle();
                            if (code == 0) {
                                PlayChooser.startPlayer(getContext(), movieTitle, videoInfo.getSourcePageUrl());
                            } else if (code == 304) {
                                Intent intent = new Intent();
                                intent.putExtra("videourl", videoInfo.getSourcePageUrl());
                                intent.putExtra("title", movieTitle);
                                setResult(code, intent);
                                finish();
                            }
                        } catch (Exception e) {
                            DebugUtil.showErrorMsg(webViewT, getContext(), e.getMessage() + "\n可能是第三方播放器调用失败，常见问题里面有下载地址");
                        }
                    }
                }).build();
        videoListMenu.showAtCenter(webViewT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000 && updateApkPath != null) {//获得安装应用程序的权限
            install();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void install() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean haveInstallPermission = getContext().getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {
                //跳转设置开启允许安装  //先获取是否有安装未知来源应用的权限
                new PromptDialog(this)
                        .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                        .setAnimationEnable(true)
                        .setTitleText("温馨提示")
                        .setContentText("尊敬的用户你好，必须开启安装软件的权限，才能完成新版本的成功安装")
                        .setPositiveListener("好的", new PromptDialog.OnPositiveListener() {
                            @Override
                            public void onClick(PromptDialog dialog) {
                                dialog.dismiss();
                                Uri packageURI = Uri.parse("package:" + getContext().getPackageName());
                                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                                intent.putExtra("NewAppPath", updateApkPath);
                                startActivityForResult(intent, 1000);
                            }
                        }).show();
                return;
            }
        }
        InstallUtils.installAPK(WebViewActivity.this, updateApkPath, new InstallUtils.InstallCallBack() {
            @Override
            public void onSuccess() {
                ToastMgr.toastShortBottomCenter(getContext(), "正在安装程序");
            }

            @Override
            public void onFail(Exception e) {
                ToastMgr.toastShortCenter(getContext(), "安装失败:" + e.toString());
            }
        });
    }

    private void loadMyJs(String url) {
        if (url.contains("m.icantv.cn/chan")) {
            webViewT.evaluateJavascript(getJs("icantv.js"), null);
        } else {
            for (String videoWeb : videoWebs) {
                if (url.contains(videoWeb)) {
                    String jjjs = getJs("vipLoad.js");
                    if (!TextUtils.isEmpty(jjjs)) {
                        webViewT.evaluateJavascript(jjjs, null);
                    }
                    break;
                }
            }
        }
        if (url.contains("youku.com")) {
            webViewT.evaluateJavascript(getJs("youkuApp.js"), null);
        } else if (url.contains("mgtv.com")) {
            webViewT.evaluateJavascript(getJs("mgtvApp.js"), null);
        }
    }

    private String getJs(String name) {
        if (!jsStrs.containsKey(name)) {
            if ("vipLoad.js".equals(name)) {
                String jss = FileUtils.getAssetsString(name, getContext());
                List<PowerMenuItem> list = VipManager.getInstance().getItemList();
                if (list.size() < 1) {
                    return null;
                }
                List<String> urls = VipManager.getInstance().getUrlList();
                StringBuilder builder = new StringBuilder("{name:ye+\"");
                for (int i = 0; i < list.size() - 1; i++) {
                    String u = urls.get(i);
                    builder.append(list.get(i).getTitle()).append("\",url:\"").append(u.replace("**", ""))
                            .append("\",title:\"").append(list.get(i).getTitle()).append("\"},{name:ye+\"");
                }
                String u2 = urls.get(list.size() - 1);
                builder.append(list.get(list.size() - 1).getTitle()).append("\",url:\"").append(u2.replace("**", ""))
                        .append("\",title:\"").append(list.get(list.size() - 1).getTitle()).append("\"}");
                jss = jss.replace("***", builder.toString());
                jsStrs.put(name, jss);
            } else {
                jsStrs.put(name, FileUtils.getAssetsString(name, getContext()));
            }
        }
        return jsStrs.get(name);
    }
}
