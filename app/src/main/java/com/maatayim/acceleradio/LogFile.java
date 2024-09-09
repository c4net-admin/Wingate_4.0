package com.maatayim.acceleradio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.maatayim.acceleradio.status.LogFragment;
import com.maatayim.acceleradio.utils.FileUtils;

import static com.maatayim.acceleradio.Parameters.ROOT_FOLDER;

import androidx.documentfile.provider.DocumentFile;

public class LogFile {

    private static final String LOG_EXTENSION = ".txt";

    private static LogFile instance;
    private static File logFile;
    private static DocumentFile logDocumentFile;
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

    public static void resetInstance() {
        instance = null;
    }

    private static void createLog() {
        String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault()).format(new Date());
        String mac = Prefs.getPreference(Prefs.USER_INFO,Prefs.MY_MAC_ADDRESS,context);
        String filename = "log_" + currentDateandTime+"_"+mac + LOG_EXTENSION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!checkUriPermission()) {
                return;
            }
            DocumentFile logDirectory = FileUtils.getDirectoryDocument(context, "Logs");
            if (logDirectory != null) {
                logDocumentFile = logDirectory.createFile("application/octet-stream", filename);
            } else {
                // Handle the case where the log directory couldn't be created
                Log.e("FileCreation", "Failed to create log directory");
                return;
            }
        } else if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("log", "no SDCard!");
            return;
        } else {
            Log.d("log", "SDCard exists!");

            String directoryName = ROOT_FOLDER + File.separator;

            File dir = FileUtils.getRootDir();
            //create folder
            File folder = new File(dir, File.separator + directoryName + "Logs"); //folder name
            if (!folder.exists()) {
                folder.mkdirs();
            }
            logFile = new File(folder, filename);
        }
        String log = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault()).format(new Date()) + " " + "File: " + filename;
        String version;
        try {
            version = "Android Version: " + General.getApkVersionName(context);
        } catch (PackageManager.NameNotFoundException e) {
            version = "";
            Log.e("LogFile", "getApkVersionName " + e.getMessage());
        }

        try {
            BufferedWriter buf = getBufferedWriter();
            if(buf==null)
                return;
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

    static boolean checkUriPermission() {
        String uriString = Prefs.getSharedPreferencesString(Prefs.USER_INFO,Prefs.TOP_URI, context);
        if(uriString==null)
            return false;
        Uri treeUri = Uri.parse(uriString);
        List<UriPermission> persistedUriPermissions = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : persistedUriPermissions) {
            if (permission.getUri().equals(treeUri) && permission.isReadPermission() && permission.isWritePermission()) {
                return true;
            }
        }
        return false;
    }

    private static BufferedWriter getBufferedWriter() throws IOException {
        BufferedWriter buf;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!checkUriPermission() || logDocumentFile==null) {
                return null;
            }
            OutputStream out = context.getContentResolver().openOutputStream(logDocumentFile.getUri(),"wa");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            buf = new BufferedWriter(outputStreamWriter);

        } else {
            //BufferedWriter for performance, true to set append to file flag
            buf = new BufferedWriter(new FileWriter(logFile, true));
        }
        return buf;
    }


    public void appendLog(String text) {
        String log = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault()).format(new Date()) + " " + text;

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = getBufferedWriter();
            if(buf==null)
                return;
            buf.append(log.trim());
            buf.append("\r\n"); //buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScannerIntent.setData(logDocumentFile.getUri());
            context.sendBroadcast(mediaScannerIntent);
        } else
            //update android files media scanner for the log files being visible without rebooting
            MediaScannerConnection.scanFile(context, new String[]{

                            logFile.getAbsolutePath()},

                    null, new MediaScannerConnection.OnScanCompletedListener() {

                        public void onScanCompleted(String path, Uri uri) {
                        }

                    });
    }


}
