package com.maatayim.acceleradio;
import android.app.Application;

import com.maatayim.acceleradio.utils.FileUtils;


public class AcceleradioAplication extends Application {


	
	@Override
	public void onCreate() {
		super.onCreate();
		FileUtils.setRootDir(getFilesDir());
		Prefs.clearSms();
		Prefs.clearSharedPreferences(Prefs.CHAT, getApplicationContext());
		LogFile.getInstance(this);
	}

}
