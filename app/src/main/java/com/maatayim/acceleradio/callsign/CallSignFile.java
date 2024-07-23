package com.maatayim.acceleradio.callsign;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maatayim.acceleradio.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.maatayim.acceleradio.Parameters.ROOT_FOLDER;

public class CallSignFile {

    public static final String CALL_SIGN_FOLDER = "CallSign";
    private static final String EXTENSION = ".txt";
    public static final String CALL_SIGN_FILE = "call_sign";
    public static final String TAG = "callSign";

    private static CallSignFile instance;
    private static File callSignFile;

    private CallSignFile() {
    }

    public static CallSignFile getInstance() {
        if (instance == null) {
            instance = new CallSignFile();
            getFile();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private static void getFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
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


    public void writeToFile(ArrayList<CallSign> callSigns) {


        try {
            if (callSignFile.exists()){
                callSignFile.delete();
            }
            Gson gson = new Gson();
            String json = gson.toJson(callSigns, new TypeToken<ArrayList<CallSign>>() {
            }.getType());
//            gson.toJson(callSigns,new TypeToken<ArrayList<CallSign>>() {}.getType(), new FileWriter(callSignFile));

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(callSignFile, true));
            buf.append(json);
            buf.newLine();
            buf.flush();
            buf.close();

        } catch (Exception e) {
            Log.e(TAG, "writeToFile ERROR : " + e.getMessage());
        }

    }

    public ArrayList<CallSign> readFromFile() {

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            Gson gson = new Gson();
            ArrayList<CallSign> callSigns = gson.fromJson(new FileReader(callSignFile), new TypeToken<ArrayList<CallSign>>() {
            }.getType());
            return callSigns;
        } catch (Exception e) {
            Log.e(TAG, "readFile ERROR : " + e.getMessage());

        }
        return new ArrayList<>();
    }


}
