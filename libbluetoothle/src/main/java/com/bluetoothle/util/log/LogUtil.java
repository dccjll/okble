package com.bluetoothle.util.log;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.bluetoothle.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 带日志文件输入的，又可控开关的日志调试
 *
 * @author dessmann developper
 * @version 1.0
 */
public class LogUtil {
    private static final String tag = "LogUtil";
    public static Boolean LOG_SWITCH = true; // 日志控制总开关 true 在开发工具后台打印日志 false 不打印日志
    public static Boolean LOG_WRITE_TO_FILE = false;// 日志写入文件开关
    public static String LOG_FILEPATH;
    public static String LOG_FILEPATH_RELEASE;// 本类输出的日志文件名称
    public static String LOG_FILEPATH_WITH_NAME_RELEASE;
    public static String LOG_FILENAME;// 本类输出的日志文件名称
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.US);// 日志的输出格式
    public static final SimpleDateFormat log_sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);// 日志文件的输出格式

    public static void w(Object msg) { // 警告信息  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'w');
    }

    public static void e(Object msg) { // 错误信息  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'e');
    }

    public static void d(Object msg) {// 调试信息  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'd');
    }

    public static void i(Object msg) {//  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'i');
    }

    public static void v(Object msg) {
        log(tag, msg == null ? "empty msg" : msg.toString(), 'v');
    }

    public static void w(String text) {
        log(tag, text, 'w');
    }

    public static void e(String text) {
        log(tag, text, 'e');
    }

    public static void d(String text) {
        log(tag, text, 'd');
    }

    public static void i(String text) {
        log(tag, text, 'i');
    }

    public static void v(String text) {
        log(tag, text, 'v');
    }

    public static void w(String tag, Object msg) { // 警告信息  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'w');
    }

    public static void e(String tag, Object msg) { // 错误信息  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'e');
    }

    public static void d(String tag, Object msg) {// 调试信息  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'd');
    }

    public static void i(String tag, Object msg) {//  
        log(tag, msg == null ? "empty msg" : msg.toString(), 'i');
    }

    public static void v(String tag, Object msg) {
        log(tag, msg == null ? "empty msg" : msg.toString(), 'v');
    }

    public static void w(String tag, String text) {
        log(tag, text, 'w');
    }

    public static void e(String tag, String text) {
        log(tag, text, 'e');
    }

    public static void d(String tag, String text) {
        log(tag, text, 'd');
    }

    public static void i(String tag, String text) {
        log(tag, text, 'i');
    }

    public static void v(String tag, String text) {
        log(tag, text, 'v');
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     * @return void
     * @since v 1.0
     */
    private static void log(String tag, String msg, char level) {
        if (LOG_SWITCH&&!TextUtils.isEmpty(msg)) {
            for (int i = 0; i < msg.length(); i += 2000) {
                String str = msg.substring(i,i+2000>msg.length()?msg.length():i+2000);
                char LOG_TYPE = 'v';
                if ('e' == level) { // 输出错误信息
                    Log.e(tag, str);
                } else if ('w' == level) {
                    Log.w(tag, str);
                } else if ('d' == level) {
                    Log.d(tag, str);
                } else if ('i' == level) {
                    Log.i(tag, str);
                } else {
                    Log.v(tag, str);
                }
            }
        }
        writeLogtoFile(String.valueOf(level), tag, msg);
    }

    /**
     * 打开日志文件并写入日志
     *
     * @return
     **/
    private static void writeLogtoFile(String mylogtype, String tag, String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String msg = sdf.format(nowtime) + "    " + mylogtype + "    " + tag + "    " + text + "\n";
        try {
            if (LOG_WRITE_TO_FILE) {
                FileUtil.writeFile(LOG_FILEPATH + log_sdf.format(nowtime) + LOG_FILENAME, msg, true);
            } else {
                FileUtil.writeFile(LOG_FILEPATH_RELEASE + log_sdf.format(nowtime) + LOG_FILENAME, msg, true);
            }
        } catch (IOException e) {

        }
    }

    /**
     * 删除制定的日志文件
     */
    public static void delFile() {// 删除日志文件
        //删除SD卡日志
        List<String> list = FileUtil.getFileNameList(LOG_FILEPATH);
        Date nowtime = new Date();
        if (list == null) {
            list = new ArrayList<>();
        }
        for (int i = 0; i < list.size(); i++) {
            try {
                String filePath = LOG_FILEPATH + list.get(i);
                if (!list.get(i).equals(log_sdf.format(nowtime) + LOG_FILENAME)) {
                    FileUtil.deleteFile(filePath);
                    LogUtil.i(tag, "删除SD卡日志,file" + (i + 1) + ":" + list.get(i));
                }
            } catch (Exception e) {

            }
        }

        //删除data目录日志
        list = FileUtil.getFileNameList(LOG_FILEPATH_RELEASE);
        if (list == null)
            return;
        for (int i = 0; i < list.size(); i++) {
            LogUtil.i(tag, "data目录日志,file" + (i + 1) + ":" + list.get(i));
            try {
                String filePath = LOG_FILEPATH_RELEASE + list.get(i);
                if (!list.get(i).equals(log_sdf.format(nowtime) + LOG_FILENAME)) {
                    LogUtil.i(tag, "删除data目录日志，" + list.get(i));
                    FileUtil.deleteFile(filePath);
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * 上传日志
     */
    public static File getLogFile(Context context,String filename) {
        List<File> files = new ArrayList<>();
//      files.add(new File(DataPathConfig.ADVERTISE_PATH));
        if (LOG_WRITE_TO_FILE) {
            files.add(new File(LogUtil.LOG_FILEPATH));
        } else {
            files.add(new File(LogUtil.LOG_FILEPATH_RELEASE));
        }
        File file = new File(LOG_FILEPATH_RELEASE + filename);
        try {
            FileUtil.writeFile(file + "", "1", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ZipUtil.zipFiles(files, file, new ZipUtil.ZipListener() {
            @Override
            public void zipProgress(int zipProgress) {
                LogUtil.i(tag, "zipProgress:" + zipProgress);
            }
        });
        return file;
    }

    /**
     * 根据日志级别显示日志标签
     */
    public static String getLogTag(int loglevel) {
        if (loglevel == Log.ASSERT) {
            return "Log.ASSERT";
        } else if (loglevel == Log.ERROR) {
            return "Log.ERROR";
        } else if (loglevel == Log.WARN) {
            return "Log.WARN";
        } else if (loglevel == Log.INFO) {
            return "Log.INFO";
        } else if (loglevel == Log.DEBUG) {
            return "Log.DEBUG";
        } else if (loglevel == Log.VERBOSE) {
            return "Log.VERBOSE";
        }
        return "UNKNOWN LOG TAG";
    }
}  