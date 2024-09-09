package com.maatayim.acceleradio.callsign;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maatayim.acceleradio.LogFile;
import com.maatayim.acceleradio.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.maatayim.acceleradio.Parameters.ROOT_FOLDER;

import androidx.documentfile.provider.DocumentFile;

public class CallSignFile {

    public static final String CALL_SIGN_FOLDER = "CallSign";
    private static final String EXTENSION = ".txt";
    public static final String CALL_SIGN_FILE = "call_sign";
    public static final String TAG = "callSign";

    private static CallSignFile instance;
    private static File callSignFile;
    private static DocumentFile callSignDocumentFile;

    private CallSignFile() {
    }

    public static CallSignFile getInstance(Context context) {
        if (context!=null&&instance == null) {
            instance = new CallSignFile();
            getFile(context);
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private static void getFile(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!FileUtils.checkUriPermission(context)) {
                return;
            }
            DocumentFile logDirectory = FileUtils.getDirectoryDocument(context, CALL_SIGN_FOLDER);
            if (logDirectory != null) {
                callSignDocumentFile = logDirectory.findFile( CALL_SIGN_FILE+EXTENSION);
            } else {
                // Handle the case where the log directory couldn't be created
                Log.e("FileCreation", "Failed to create log directory");
                return;
            }
        } else if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "no SDCard!");
        } else {
            Log.d(TAG, "SDCard exists!");

            String directoryName = ROOT_FOLDER + File.separator;

            File dir = FileUtils.getRootDir();
            //create folder
            File folder = new File(dir, File.separator + directoryName + CALL_SIGN_FOLDER); //folder name
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //create file
            String filename = CALL_SIGN_FILE + EXTENSION;

            callSignFile = new File(folder, filename);

        }
    }

    public ArrayList<CallSign> readFromFile() {

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            Gson gson = new Gson();
            ArrayList<CallSign> callSigns;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "readFromFile: "+readRawContent(LogFile.context, callSignDocumentFile));
                InputStream inputStream = LogFile.context.getContentResolver().openInputStream(callSignDocumentFile.getUri());
                InputStreamReader reader = new InputStreamReader(inputStream);
                callSigns = gson.fromJson(reader, new TypeToken<ArrayList<CallSign>>() {
                }.getType());
                reader.close();

            } else {
                 callSigns = gson.fromJson(new FileReader(callSignFile), new TypeToken<ArrayList<CallSign>>() {
                }.getType());
            }
            return callSigns;
        } catch (Exception e) {
            Log.e(TAG, "readFile ERROR : " + e.getMessage());

        }
        return new ArrayList<>();
    }

    public String readRawContent(Context context, DocumentFile documentFile) {
        StringBuilder content = new StringBuilder();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(documentFile.getUri());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
