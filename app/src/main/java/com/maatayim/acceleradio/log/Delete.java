package com.maatayim.acceleradio.log;

import android.app.Activity;
import android.widget.ImageView;

import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.utils.FormatException;

public class Delete extends LogEntry {
	
	private static final String DELETE = "D";
	private String macAddress;
	private String iconCounter;

	public Delete(String str) throws FormatException {
		super(str);
		parseStr();
	}

	private void parseStr() throws FormatException {
		if (entry.endsWith("\n")){
			entry = entry.substring(0,entry.length()-1);
		}
		String[] data = entry.split(",");
		if (data.length != 4){
			throw new FormatException("wrong message size");
		}
		macAddress = data[2];
		iconCounter = data[3];	
	}

	public static String getDelete() {
		return DELETE;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getIconCounter() {
		return iconCounter;
	}

	@Override
	public void handle(Activity mainActivity, ImageView button) {
		String key = macAddress + ":" + iconCounter;
		LocationMarker m = Prefs.myMarkers.remove(key);
		if (m != null){
			m.removeFromMap();
		}
		
	}

}
