package com.maatayim.acceleradio.log;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.LogFile;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Parameters;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.utils.MapUtils;

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
			String[] fullMacName = entry.split("0x");
			if (fullMacName.length> 2){
				String myMac = Prefs.getPreference(Prefs.USER_INFO,Prefs.MY_MAC_ADDRESS,mainActivity);

				String macAddress = fullMacName[1].substring(0,4);
				String subMac = fullMacName[2].substring(0,4);
				int currentMarkCount = Integer.decode("0x"+subMac);
				currentMarkCount++;


				macAddress = macAddress.replace(""+Parameters.CHECKSUM_PEDDING,"");

				// Check if the me is same as saved me or reset map and log file
				if (!TextUtils.isEmpty(myMac) && !TextUtils.isEmpty(macAddress) && !macAddress.equals(myMac)){
					MapUtils.clearMap(mainActivity);
					LogFile.resetInstance();
				}

				Prefs.setSharedPreferencesString(Prefs.USER_INFO,Prefs.MY_MAC_ADDRESS, macAddress,mainActivity);
				((MainActivity)mainActivity).setCurrentMarkCount(currentMarkCount);
			}else {
				android.util.Log.e("round error", "no sub mac in round");
				return;
			}
		}
	}

}
