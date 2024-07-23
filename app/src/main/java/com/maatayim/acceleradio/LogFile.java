package com.maatayim.acceleradio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.maatayim.acceleradio.status.LogFragment;
import com.maatayim.acceleradio.utils.FileUtils;

import static com.maatayim.acceleradio.Parameters.ROOT_FOLDER;

public class LogFile {

    private static final String LOG_EXTENSION = ".txt";

    private static LogFile instance;
    private static File logFile;
    private static Context context;

    private LogFile(Context context) {
        this.context = context;
    }

    public static LogFile getInstance(Context context) {
        if (instance == null) {
            instance = new LogFile(context);
            createLog();
        }
        return instance;
    }

    public static void resetInstance(){
        instance = null;
    }

    private static void createLog() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("log", "no SDCard!");
        } else {
            Log.d("log", "SDCard exists!");

            String directoryName = ROOT_FOLDER + File.separator;

            File dir = FileUtils.getRootDir();
            //create folder
            File folder = new File(dir, File.separator + directoryName + "Logs"); //folder name
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //create file
            String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault()).format(new Date());
            String filename = "log_" + currentDateandTime + LOG_EXTENSION;

            logFile = new File(folder, filename);

            String log = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault()).format(new Date()) + " " + "File: " + filename;
            String version;
            try {
                 version  = "Android Version: "+ General.getApkVersionName(context);
            } catch (PackageManager.NameNotFoundException e) {
                version = "";
                Log.e("LogFile","getApkVersionName "+e.getMessage());
            }

            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(log);
                buf.newLine();
                buf.append(version);
                buf.newLine();
                buf.flush();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Map<String, String> m;
            m = new HashMap<String, String>();
            m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "File: 180408_0202 " + filename);
            m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
            Prefs.getInstance().addStatusMessages(m);
            LogFragment.notifyChanges();
        }
    }


    public void appendLog(String text) {
        String log = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault()).format(new Date()) + " " + text;

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(log.trim());
            buf.append("\r\n"); //buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //update android files media scanner for the log files being visible without rebooting
        MediaScannerConnection.scanFile(context, new String[]{

                        logFile.getAbsolutePath()},

                null, new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                    }

                });
    }


}
