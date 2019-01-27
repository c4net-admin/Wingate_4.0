package com.maatayim.acceleradio;
import android.app.Application;


public class AcceleradioAplication extends Application {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Prefs.clearSms();
		Prefs.clearSharedPreferences(Prefs.CHAT, getApplicationContext());
		LogFile.getInstance(this);
	}

}
