package com.maatayim.acceleradio.utils;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.maatayim.acceleradio.Prefs;

import java.io.File;
import java.util.List;

public class FileUtils {
    private static File rootDir;

    public static File getRootDir() {
        return rootDir;
    }

    public static void setRootDir(File rootDir) {
        FileUtils.rootDir = rootDir;
    }

  @Nullable
    public static DocumentFile getTopDocumentFile(Context context) {
        String uriString = Prefs.getSharedPreferencesString(Prefs.USER_INFO, Prefs.TOP_URI, context);
       if(uriString==null)
           return null;
        Uri uri = Uri.parse(uriString);
        DocumentFile treeDocumentFile = DocumentFile.fromTreeUri(context, uri);
        DocumentFile topDirectory = treeDocumentFile.findFile("c4net");

        if (topDirectory == null || !topDirectory.exists()) {
            topDirectory = treeDocumentFile.createDirectory("c4net");
        }
        return topDirectory;
    }

    @Nullable
    public static DocumentFile getDirectoryDocument(Context context, String directoryName) {
        DocumentFile treeDocumentFile = FileUtils.getTopDocumentFile(context);
        if(treeDocumentFile==null)
            return null;
        DocumentFile logDirectory = treeDocumentFile.findFile(directoryName);
        if (logDirectory == null || !logDirectory.exists()) {
            logDirectory = treeDocumentFile.createDirectory(directoryName);
        }
        return logDirectory;
    }

    public static boolean checkUriPermission(Context context) {
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
}
