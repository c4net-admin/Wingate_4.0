package com.jeremyfeinstein.slidingmenu.lib;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.InvocationTargetException;

public class UIUtils {
    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the side
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics currentWindowMetrics = windowManager.getCurrentWindowMetrics();
            Insets insets = currentWindowMetrics.getWindowInsets().getInsets(WindowInsetsCompat.Type.systemBars());
            size.x = insets.right;
            size.y = insets.bottom;
        }  else { display.getSize(size);}
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics maximumWindowMetrics = windowManager.getMaximumWindowMetrics();
            size.x = maximumWindowMetrics.getBounds().width();
            size.y = maximumWindowMetrics.getBounds().height();
        }  else {
            display.getRealSize(size);
        }


        return size;
    }

    public static int getNavBarHeight(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            WindowManager windowManager = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            WindowMetrics currentWindowMetrics = windowManager.getCurrentWindowMetrics();
            Insets insets = currentWindowMetrics.getWindowInsets().getInsets(WindowInsetsCompat.Type.systemBars());
            return insets.bottom;
        }

        //The device has a navigation bar
        Resources resources = c.getResources();
        int orientation = resources.getConfiguration().orientation;
        Point size = getNavigationBarSize(c);
        return isTablet(c) ? size.y : size.x;

    }


    private static boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}


