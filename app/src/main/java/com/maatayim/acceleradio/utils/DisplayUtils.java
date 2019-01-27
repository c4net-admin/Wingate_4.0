package com.maatayim.acceleradio.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

public class DisplayUtils {

	private static Point screenSize;
	
	@SuppressLint("NewApi")
	public static Point getScreenSize(Activity activity) {
		
		 if(screenSize != null)
			 return screenSize;
		 
		// calculate status bar height
		int resourceId = activity.getResources().getIdentifier(
				"status_bar_height", "dimen", "android");
		int result = 0;
		if (resourceId > 0) {
			result = activity.getResources().getDimensionPixelSize(resourceId);
		}
 
		Point size = new Point();
		Display display = activity.getWindowManager().getDefaultDisplay();
		display.getSize(size);
		size.y -= result;
		
 
		screenSize = size;
		return size;
	}
	
	public static int getScreenWidth(Activity activity){
		if(screenSize != null)
			return screenSize.x;
		
		return getScreenSize(activity).x;
	}
	
	public static int getScreenHeight(Activity activity){
		if(screenSize != null)
			return screenSize.y;
		
		return getScreenSize(activity).y;
	}
	
//	public static int getDefaultActionBarHeight(Context context){
//		TypedValue tv = new TypedValue();
//		int actionBarHeight = 0;
//		if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)){
//		    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
//		}
//		return actionBarHeight;
//	}
}
