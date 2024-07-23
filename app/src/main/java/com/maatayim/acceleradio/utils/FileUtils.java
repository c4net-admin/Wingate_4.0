package com.maatayim.acceleradio.utils;

import java.io.File;

public class FileUtils {
    private static File rootDir;

    public static File getRootDir() {
        return rootDir;
    }

    public static void setRootDir(File rootDir) {
        FileUtils.rootDir = rootDir;
    }
}
