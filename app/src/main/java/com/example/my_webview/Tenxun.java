package com.example.my_webview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ansen.http.entity.HttpConfig;
import com.ansen.http.net.HTTPCaller;
import com.ansen.http.net.Header;
import com.example.my_webview.x5.FullScreenActivity;
import com.example.my_webview.x5.WebViewJavaScriptFunction;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.lizhangqu.coreprogress.ProgressUIListener;

public class Tenxun extends AppCompatActivity {
    private X5WebView forum_context; //"http://192.168.1.111:8080"
    String url = MIN_HOME.Instance.Url;
    private ViewGroup mViewParent;
    private PowerManager.WakeLock wakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenxun);
//        isNotificationEnabled(this);
        verifyStoragePermissions(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Tenxun.class.getName());
        wakeLock.acquire();
        initHttp();
        initView();

    }

    // 动态权限
    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void initHttp() {
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setAgent(true);//有代理的情况能不能访问
        httpConfig.setDebug(true);//是否debug模式 如果是debug模式打印log
        httpConfig.setTagName("ansen");//打印log的tagname
        HTTPCaller.getInstance().setHttpConfig(httpConfig);
    }

    private View initView() {

        hideSystemUI();
        forum_context = (X5WebView) findViewById(R.id.forum_context1);
        forum_context.addJavascriptInterface(this, "androidObject");
        forum_context.loadUrl(url);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        forum_context.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        forum_context.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView arg0, String arg1, String arg2,
                                       JsResult arg3) {
                return super.onJsConfirm(arg0, arg1, arg2, arg3);
            }

            View myVideoView;
            View myNormalView;
            IX5WebChromeClient.CustomViewCallback callback;

            /**
             * 全屏播放配置
             */
            @Override
            public void onShowCustomView(View view,
                                         IX5WebChromeClient.CustomViewCallback customViewCallback) {
                FrameLayout normalView = (FrameLayout) findViewById(R.id.forum_context1);
                ViewGroup viewGroup = (ViewGroup) normalView.getParent();
                viewGroup.removeView(normalView);
                viewGroup.addView(view);
                myVideoView = view;
                myNormalView = normalView;
                callback = customViewCallback;
            }

            @Override
            public void onHideCustomView() {
                if (callback != null) {
                    callback.onCustomViewHidden();
                    callback = null;
                }
                if (myVideoView != null) {
                    ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
                    viewGroup.removeView(myVideoView);
                    viewGroup.addView(myNormalView);
                }
            }

            @Override
            public boolean onJsAlert(WebView arg0, String arg1, String arg2,
                                     JsResult arg3) {
                /**
                 * 这里写入你自定义的window alert
                 */
                return super.onJsAlert(null, arg1, arg2, arg3);
            }
        });
        return null;
    }

    @JavascriptInterface
    public void TestV() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forum_context.evaluateJavascript("getCurrentInfo()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("qcl0228", "js返回的数据" + value);
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public void addJavascriptInterface(Object object, String name) {
        Log.e("ADD JavaScript 接口数据", "" + object + "************" + name);

    }

    @JavascriptInterface
    public void StartVideo() {
        if (forum_context.getX5WebViewExtension() != null) {
            Log.e("X5", "页面启动全屏模式");
            Toast.makeText(this, "页面内全屏播放模式", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();
            data.putBoolean("standardFullScreen", false);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，
            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，
            data.putInt("DefaultVideoScreen", 1);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
            forum_context.getX5WebViewExtension().invokeMiscMethod("setVideoParams", data);
        } else {
            Log.e("X5", "页面启动全屏模式失败");
            Toast.makeText(this, "页面内全屏播放模式失败", Toast.LENGTH_LONG).show();
        }
    }

    public void WebSetting() {
        com.tencent.smtt.sdk.WebSettings webSettings = forum_context.getSettings();
        forum_context.getSettings().setBlockNetworkImage(false);
        webSettings.setJavaScriptEnabled(true); // 是否开启JS支持
//        webSettings.setPluginsEnabled(true); // 是否开启插件支持
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 是否允许JS打开新窗口

        webSettings.setPluginState(com.tencent.smtt.sdk.WebSettings.PluginState.ON);
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

        webSettings.setLayoutAlgorithm(com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 按规则重新布局
        webSettings.setLoadsImagesAutomatically(true); //// 是否自动加载图片
        webSettings.setDefaultTextEncodingName("UTF-8"); //// 设置编码格式
        webSettings.setNeedInitialFocus(true); // 是否需要获取焦点
        webSettings.setGeolocationEnabled(false); // 设置开启定位功能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            forum_context.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

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
        forum_context.onPause();
        forum_context.pauseTimers();
    }

    @Override
    public void onResume() {
        super.onResume();
        forum_context.resumeTimers();
        forum_context.onResume();
    }


    @Override
    protected void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;

        }
        forum_context.destroy();
        forum_context = null;
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        Log.d("home", "onUserLeaveHint");
        super.onUserLeaveHint();

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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        try {
            super.onConfigurationChanged(newConfig);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void onX5ButtonClicked() {
        Tenxun.this.enableX5FullscreenFunc();
    }

    public Bitmap base64ToPicture(String imgBase64) {
        String Cut = imgBase64.substring(imgBase64.indexOf(",") + 1, imgBase64.length());
        byte[] decode = Base64.decode(Cut, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        return bitmap;
    }

    public void savePictureToAlbum(Context mContext, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        // 把文件插入到系统图库
        MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                bitmap, null, null);
        // 通知图库更新
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + "/sdcard/namecard/")));
    }

    @JavascriptInterface
    public void onDonwload(String url) {
        savePictureToAlbum(Tenxun.this, base64ToPicture(url));
    }

    @JavascriptInterface
    public void onDonwloadVideo(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                ProgressDialog progressDialog = new ProgressDialog(Tenxun.this);
//                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                progressDialog.setMessage("执行下载任务中请稍后......");
//                progressDialog.setCancelable(false);//不能手动取消下载进度对话框
                Date date = new Date();
                String fileName = new Date().getTime() + "GuiZ_Monitor" + date.getTime() + ".mp4";
                String MYLOG_PATH_SDCARD_DIR = "/sdcard/LocalAppLogs/Monitor";
                File dirsFile = new File(MYLOG_PATH_SDCARD_DIR);
                if (!dirsFile.exists()) {
                    dirsFile.mkdirs();
                }
                String filePath = MYLOG_PATH_SDCARD_DIR + "/" + fileName;
                HTTPCaller.getInstance().downloadFile(url, filePath, null, new ProgressUIListener() {

                    //下载开始
                    @Override
                    public void onUIProgressStart(long totalBytes) {
                        StartTs("下载保存视频任务");
//                        progressDialog.setMax((int) totalBytes);
//                        progressDialog.show();

                    }

                    //更新进度
                    @Override
                    public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
//                        progressDialog.setProgress((int) numBytes);
                    }

                    //下载完成
                    @Override
                    public void onUIProgressFinish() {
//                        progressDialog.dismiss();
                        StartTs("保存完成");
                        Toast.makeText(Tenxun.this, "保存完成", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
//        ProgressDialog progressDialog = new ProgressDialog(Tenxun.this);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setMessage("保存视频中,请稍后");
//        progressDialog.setCancelable(false);//不能手动取消下载进度对话框

    }

    private boolean isNotificationEnabled(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //8.0手机以上
            if (((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).getImportance() == NotificationManager.IMPORTANCE_NONE) {
                return false;
            }
        }
        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null;
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void Task(String id, String msg) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        //这里判定 如果是 8.0以上,会出现无效的情况。那么单独额外处理一下
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(id, "notiChannelName2", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager =
                    (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        //点击通知栏跳转到指定界面
        Notification notification = new NotificationCompat.Builder(this, id)
                .setContentTitle("下载任务")//标题
                .setContentText(msg)//内容
                .setWhen(System.currentTimeMillis())//即可发送
                .setSmallIcon(R.drawable.search)//图标
                .setDefaults(NotificationCompat.DEFAULT_SOUND)//
                .setPriority(NotificationCompat.DEFAULT_SOUND)
                .setAutoCancel(false)//点击消失
                .build();
        manager.notify(5, notification);
    }

    public void createNotificationChanneler(String c_channelid, String c_channelname, int c_importance) {
        //检测 Channel是否已经被创建了，避免重复创建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//检测Android版本

            if (getNotificationManager().getNotificationChannel(c_channelid) != null) {
                return;
            }//要是没被创建那么
            NotificationChannel notificationChannel = new NotificationChannel(c_channelid, c_channelname, c_importance);
            notificationChannel.enableLights(true);//开启提示灯
            notificationChannel.enableVibration(true);//开启震动
            notificationChannel.setSound(Uri.fromFile(new File("/system/media/audio/ringtones/Luna.ogg")), null);
            notificationChannel.setBypassDnd(true);//可绕过免打扰模式
            notificationChannel.setImportance(c_importance);//设置优先级
            notificationChannel.setLightColor(Color.RED);//设置提示灯颜色
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);//设置锁屏界面图标可见
            notificationChannel.setShowBadge(true);//有图标
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            getNotificationManager().createNotificationChannel(notificationChannel);
        } else {
            return;
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void StartTs(String val) {
        createNotificationChanneler("one2", "notiChannelName2", 5);
        Task("one2", val);

    }

    @JavascriptInterface
    public void onCustomButtonClicked() {
        Tenxun.this.disableX5FullscreenFunc();
    }

    // /////////////////////////////////////////
    // 向webview发出信息
    private void enableX5FullscreenFunc() {
        if (forum_context.getX5WebViewExtension() != null) {
            Toast.makeText(this, "开启X5全屏播放模式", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", false);// true表示标准全屏，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            forum_context.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }

    private void disableX5FullscreenFunc() {
        if (forum_context.getX5WebViewExtension() != null) {
            Toast.makeText(this, "恢复webkit初始状态", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", true);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            forum_context.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }

    private void enableLiteWndFunc() {
        if (forum_context.getX5WebViewExtension() != null) {
            Toast.makeText(this, "开启小窗模式", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", false);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", true);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            forum_context.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }

    private void enablePageVideoFunc() {
        if (forum_context.getX5WebViewExtension() != null) {
            Toast.makeText(this, "页面内全屏播放模式", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", false);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 1);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            forum_context.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }


}