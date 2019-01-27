package com.maatayim.acceleradio.log;

import android.app.Activity;
import android.widget.ImageView;

public abstract class LogEntry {
	
	
	private static final String VERSION = "1";
	protected String entry;
	
	public LogEntry(String str) {
		entry = str.trim();
	}
	
	
	public static String getVersion() {
		return VERSION;
	}
	
	public String toString(){
		return entry;
	}
	
	public abstract void handle(Activity mainActivity, ImageView button);

}
