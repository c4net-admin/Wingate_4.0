package com.maatayim.acceleradio.log;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.widget.ImageView;

import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;

public class Log extends LogEntry {
	
	private String text;
	
	public Log(String str) {
		super(str);
		parseStr();
	}

	private void parseStr() {
		text = "";
		entry = entry.trim();
		String[] data = entry.split(",");
		for (int i = 2; i<data.length; i++){
			text += data[i];
			if (i < data.length-1){
				text += ",";
			}
		}


	}

	@Override
	public void handle(Activity mainActivity, ImageView button) {

		if (entry.contains("round")){
			String[] buffer = entry.split("0x");
			if (buffer.length> 1){
				Prefs.setSharedPreferencesString(Prefs.USER_INFO,Prefs.MY_MAC_ADDRESS,buffer[1],mainActivity);

			}
		}
	}

}
