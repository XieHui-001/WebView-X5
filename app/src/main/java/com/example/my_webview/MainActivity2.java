package com.example.my_webview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {
    private WebView testview; //"http://192.168.1.111:8080"
    String url = MIN_HOME.Instance.Url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        testview = findViewById(R.id.testview);
        testview.loadUrl(MIN_HOME.Instance.Url);
        WebSetting();
        testview.addJavascriptInterface(MainActivity2.this, "androidObject");
        hideSystemUI();

    }

    private void OKhttps(String value) {
        try {
            JSONObject jsonObject = JSON.parseObject(value.replace("\\", ""));
            String serial = jsonObject.getString("serial");
            String code = jsonObject.getString("code");
            Log.e("获取JS 数据", serial + "::::" + code);
        } catch (Exception exception) {
            Log.e("获取JS 数据失败", exception.toString());
        }
    }
    @JavascriptInterface
    public void TestV() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testview.evaluateJavascript("getCurrentInfo()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("qcl0228", "js返回的数据" + value);
//                        Pattern pattern = Pattern.compile("\t|\r|\n|\\s*");
//                        Matcher matcher = pattern.matcher(value);
//                        String dest = matcher.replaceAll("");
//                        OKhttps(dest.replace("\"", ""));
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public void addJavascriptInterface(Object object, String name) {
        Log.e("ADD JavaScript 接口数据", "" + object + "************" + name);

    }

    public void WebSetting() {
        WebSettings webSettings = testview.getSettings();
        testview.getSettings().setBlockNetworkImage(false);
        webSettings.setJavaScriptEnabled(true); // 是否开启JS支持
//        webSettings.setPluginsEnabled(true); // 是否开启插件支持
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 是否允许JS打开新窗口

        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);


        webSettings.setUseWideViewPort(true); // 缩放至屏幕大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕大小
        webSettings.setSupportZoom(true); // 是否支持缩放
        webSettings.setBuiltInZoomControls(true); // 是否支持缩放变焦，前提是支持缩放
        webSettings.setDisplayZoomControls(true); // 是否隐藏缩放控件

        webSettings.setAllowFileAccess(true); // 是否允许访问文件
        webSettings.setDomStorageEnabled(true); // 是否节点缓存
        webSettings.setDatabaseEnabled(true); // 是否数据缓存
        webSettings.setAppCacheEnabled(true); // 是否应用缓存
        webSettings.setAppCachePath(MIN_HOME.Instance.Url); // 设置缓存路径

        webSettings.setMediaPlaybackRequiresUserGesture(false); // 是否要手势触发媒体
        webSettings.setStandardFontFamily("sans-serif"); // 设置字体库格式
        webSettings.setFixedFontFamily("monospace"); // 设置字体库格式
        webSettings.setSansSerifFontFamily("sans-serif"); // 设置字体库格式
        webSettings.setSerifFontFamily("sans-serif"); // 设置字体库格式
        webSettings.setCursiveFontFamily("cursive"); // 设置字体库格式
        webSettings.setFantasyFontFamily("fantasy"); // 设置字体库格式
        webSettings.setTextZoom(100); // 设置文本缩放的百分比
        webSettings.setMinimumFontSize(8); // 设置文本字体的最小值(1~72)
        webSettings.setDefaultFontSize(16); // 设置文本字体默认的大小

        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 按规则重新布局
        webSettings.setLoadsImagesAutomatically(true); //// 是否自动加载图片
        webSettings.setDefaultTextEncodingName("UTF-8"); //// 设置编码格式
        webSettings.setNeedInitialFocus(true); // 是否需要获取焦点
        webSettings.setGeolocationEnabled(false); // 设置开启定位功能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            testview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        }
        webSettings.setBlockNetworkLoads(false); // 是否从网络获取资源

    }


    @JavascriptInterface
    public String androidMethod() {
        Log.i("qcl0228", "js调用了安卓的方法");
        return "我是js调用安卓获取的数据";
    }

    @Override
    public void onPause() {
        super.onPause();
        TestV();
        testview.onPause();
        testview.pauseTimers();
    }

    @Override
    public void onResume() {
        super.onResume();
        testview.resumeTimers();
        testview.onResume();
    }


    @Override
    protected void onDestroy() {
        testview.destroy();
        testview = null;
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        Log.d("home", "onUserLeaveHint");
        super.onUserLeaveHint();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("云台通知", "界面绘制完成");
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
        //解决在华为手机上横屏时，状态栏不消失的问题
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
    }
}