package com.example.my_webview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet.BottomListSheetBuilder;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MIN_HOME extends AppCompatActivity implements  QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener {

    private TextView default_data;
    private Button Update;
    private RelativeLayout home_rel;
    private EditText update_data;
    private Button affirm_update;
    public String Url = null;
    private Button href_url;
    public static MIN_HOME Instance = null;
    private QMUIBottomSheet qmuiBottomSheet;
    private QMUIDialog qmuiDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_min_home);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
//        isNotificationEnabled(this);
        verifyStoragePermissions(this);
        Logs.e("", "日志");
        CreatFile();
        initView();
        qmuiBottomSheet = new QMUIBottomSheet(this,R.style.QMUI_BottomSheet);
        qmuiBottomSheet.show();
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

    private void Task() {

        NotificationManagerCompat manager = NotificationManagerCompat.from(MIN_HOME.this);

        //这里判定 如果是 8.0以上,会出现无效的情况。那么单独额外处理一下
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("2", "DonwloadTask", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager =
                    (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        //点击通知栏跳转到指定界面
//        Intent intent = new Intent(Tenxun.this, Tenxun.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(Tenxun.this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(MIN_HOME.this, "one")
                .setContentTitle("下载任务")//标题
                .setContentText("点击查看详细")//内容
                .setWhen(System.currentTimeMillis())//即可发送
                .setDefaults(NotificationCompat.DEFAULT_ALL)//
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.search)
                .setAutoCancel(true)//点击消失
                .build();
//.setContentIntent(pendingIntent)
//           //图标
        manager.notify(1, notification);
    }


    public MIN_HOME() {
        MIN_HOME.Instance = this;
    }

    boolean start = false;

    private View initView() {
        default_data = findViewById(R.id.default_data);
        Update = findViewById(R.id.Update);
        home_rel = findViewById(R.id.home_rel);
        update_data = findViewById(R.id.update_data);
        affirm_update = findViewById(R.id.affirm_update);
        href_url = findViewById(R.id.href_url);
        href_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!start) {
                    start = true;
//                    Task();
                    startActivity(new Intent(MIN_HOME.this, Tenxun.class));
                    start = false;
                }

            }
        });
        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                home_rel.setVisibility(View.VISIBLE);
            }
        });

        affirm_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!update_data.getText().toString().isEmpty()) {
                    CreaURLXml(update_data.getText().toString());
                }
            }
        });
        return null;
    }


    private void CreaURLXml(String val) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(Environment.getExternalStorageDirectory(),
                            "webview/Url.xml");
                    if (file.exists()) {
                        file.delete();
                        FileOutputStream fos = new FileOutputStream(file);
                        XmlSerializer serializer = Xml.newSerializer();
                        serializer.setOutput(fos, "UTF-8");
                        Map<String, Object> json = new HashMap<>();
                        json.put("url", val);
                        com.alibaba.fastjson.JSONObject jsonObject;
                        jsonObject = new com.alibaba.fastjson.JSONObject(json);
                        serializer.text(jsonObject.toString());
                        serializer.endDocument();
                        fos.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Url = val;
                                default_data.setText(Url);
                                home_rel.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        FileOutputStream fos = new FileOutputStream(file);
                        XmlSerializer serializer = Xml.newSerializer();
                        serializer.setOutput(fos, "UTF-8");
                        Map<String, Object> json = new HashMap<>();
                        json.put("url", "http://192.168.1.111:8081");
                        com.alibaba.fastjson.JSONObject jsonObject;
                        jsonObject = new com.alibaba.fastjson.JSONObject(json);
                        serializer.text(jsonObject.toString());
                        serializer.endDocument();
                        fos.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Url = "http://192.168.1.111:8080";
                                default_data.setText(Url);
                                Toast.makeText(MIN_HOME.this, "请使用默认地址    " + Url + "   ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    public void CreatFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String MYLOG_PATH_SDCARD_DIR = "/sdcard/webview";
                File dirsFile = new File(MYLOG_PATH_SDCARD_DIR);
                if (!dirsFile.exists()) {
                    dirsFile.mkdirs();
                    CreaPassowrdXml();
                } else {
                    try {
                        GetPassowrdXml();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private void CreaPassowrdXml() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(Environment.getExternalStorageDirectory(),
                            "webview/Url.xml");
                    if (!file.exists()) {
                        FileOutputStream fos = new FileOutputStream(file);
                        XmlSerializer serializer = Xml.newSerializer();
                        serializer.setOutput(fos, "UTF-8");
                        Map<String, Object> json = new HashMap<>();
                        json.put("url", "http://192.168.1.111:8081");
                        JSONObject jsonObject;
                        jsonObject = new JSONObject(json);
                        serializer.text(jsonObject.toString());
                        serializer.endDocument();
                        fos.close();
                        Url = "http://192.168.1.111:8081";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                default_data.setText(Url);
                            }
                        });
                    } else {
                        GetPassowrdXml();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void GetPassowrdXml() throws FileNotFoundException {
        try {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "webview/Url.xml");
            File dirs_ = new File(file.getPath());
            if (dirs_.exists()) {
                FileInputStream fileIS = null;
                try {
                    fileIS = new FileInputStream(dirs_.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuffer sb = new StringBuffer();
                BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
                String readString = new String();
                while (true) {
                    try {
                        if (!((readString = buf.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sb.append(readString);
                }
                String GetXmlValues = sb.toString();
                com.alibaba.fastjson.JSONObject jsonObject = null;
                jsonObject = JSON.parseObject(GetXmlValues);
                //
                String GetUrl = jsonObject.getString("url");
//                String Get_url = jsonObject.toJSONString(GetUrl).replace("\"", "");
//                Pattern pattern = Pattern.compile("\\s\"");
//                Matcher matcher = pattern.matcher(Get_url);
//                String dest = matcher.replaceAll("");
                String Get_url = jsonObject.toJSONString(GetUrl).replaceAll("\\s|\"", "");
                Url = Get_url;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        default_data.setText(Url);
                    }
                });
            } else {

            }
        } catch (Exception exception) {
            Log.e("初始化 获取数据失败", exception.toString());
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
        dialog.show();
    }
}