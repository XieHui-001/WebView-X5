package com.example.my_webview;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logs {
    private static Boolean MYLOG_SWITCH = true; // 日志文件总开关
    private static Boolean MYLOG_WRITE_TO_FILE = true;// 日志写入文件开关
    private static char MYLOG_TYPE = 'v';// 输入日志类型，w代表只输出告警信息等，v代表输出所有信息
    private static String MYLOG_PATH_SDCARD_DIR = "/sdcard/webview";// 日志文件在sdcard中的路径
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 1;// sd卡中日志文件的最多保存天数
    private static String MYLOGFILEName = "webview.log";// 本类输出的日志文件名称
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-M-d");// 日志文件格式

    public static void w(String tag, Object msg) { // 警告信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, msg.toString(), 'w');
            }
        }).start();

    }

    public static void e(String tag, Object msg) { // 错误信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, msg.toString(), 'e');
            }
        }).start();
    }

    public static void d(String tag, Object msg) {// 调试信息

        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, msg.toString(), 'd');
            }
        }).start();
    }

    public static void i(String tag, Object msg) {//
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, msg.toString(), 'i');
            }
        }).start();

    }

    public static void v(String tag, Object msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, msg.toString(), 'v');
            }
        }).start();
    }

    public static void w(String tag, String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, text, 'w');
            }
        }).start();
    }

    public static void e(String tag, String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, text, 'e');
            }
        }).start();
    }

    public static void d(String tag, String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, text, 'd');
            }
        }).start();
    }

    public static void i(String tag, String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, text, 'i');
            }
        }).start();
    }

    public static void v(String tag, String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log(tag, text, 'v');
            }
        }).start();
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     */
    private static void log(String tag, String msg, char level) {
        if (MYLOG_SWITCH) {//日志文件总开关
            if ('e' == level && ('e' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) { // 输出错误信息
                Log.e(tag, msg);
            } else if ('w' == level && ('w' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
                Log.w(tag, msg);
            } else if ('d' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
                Log.d(tag, msg);
            } else if ('i' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
                Log.i(tag, msg);
            } else {
                Log.v(tag, msg);
            }
            if (MYLOG_WRITE_TO_FILE)//日志写入文件开关
                writeLogtoFile(String.valueOf(level), tag, msg);
        }
    }

    /**
     * 打开日志文件并写入日志
     *
     * @param mylogtype
     * @param tag
     * @param text
     */
    public static void writeLogtoFile(String mylogtype, String tag, String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
        String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype + "    " + tag + "    " + text;
        File dirPath = Environment.getExternalStorageDirectory();

        File dirsFile = new File(MYLOG_PATH_SDCARD_DIR);
        if (!dirsFile.exists()) {
            dirsFile.mkdirs();
        }
        //Log.i("创建文件","创建文件");
        File file = new File(dirsFile.toString(), needWriteFiel + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地 除当前天 的 所有日志
     */
    public static void delFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                String needWriteFiel = logfile.format(date);
                File dirPath = Environment.getExternalStorageDirectory();
                File Fils = new File(dirPath + "/TalkDooClassExe/log/" + needWriteFiel + "ClassExeLogs.log");
                File file = new File(dirPath + "/TalkDooClassExe/log");// MYLOG_PATH_SDCARD_DIR   //needDelFiel + MYLOGFILEName
                String s[] = file.list();
                if (Fils.exists()) {
                    for (int i = 0; i < s.length; i++) {
                        String sh = s[i].toString();
                        File file1 = new File(dirPath + "/TalkDooClassExe/log/" + sh);
                        if (!file1.getPath().contains(Fils.getPath())) {
                            file1.delete();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
}
