package com.maatayim.acceleradio.log;

import java.util.Date;

import android.app.Activity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.chat.ChatFragment;
import com.maatayim.acceleradio.chat.ChatMessage;
import com.maatayim.acceleradio.utils.FormatException;

import static com.maatayim.acceleradio.Parameters.DELIMITER_RX;

public class Sms extends LogEntry {
	
	private static final String SMS = "T";
	private static final int ANIMATION_SPEED = 200; //blinking speed
	private static final int ANIMATION_DURATION = 5;//in seconds
	private String macAddresss;
	private String text;

	public Sms(String str) throws FormatException {
		super(str);
		parseStr();
	}

	private void parseStr() throws FormatException {
		if (entry.endsWith(DELIMITER_RX)){
			entry = entry.substring(0,entry.length()-1);
		}
		String[] data = entry.split(",");
		if (data.length < 4){
			throw new FormatException("wrong message size");
		}
		macAddresss = data[2];
		text = "";
		for (int i = 4; i<data.length; i++){
			if (data[i].equals(DELIMITER_RX)) return;
			text += data[i];
			if (i < data.length-1){
				text += ",";
			}
		}
	}

	public static String getSms() {
		return SMS;
	}

	public String getMacAddresss() {
		return macAddresss;
	}

	public String getText() {
		return text;
	}

	@Override
	public void handle(Activity mainActivity, ImageView button) {
		String myMac = Prefs.getPreference(Prefs.USER_INFO, Prefs.MY_MAC_ADDRESS, mainActivity);
		boolean me = macAddresss.equals(myMac);
		ChatMessage chatMessage = new ChatMessage(macAddresss, text, me, new Date().getTime());
		Prefs.messages.add(chatMessage);
		ChatFragment.adapter.notifyDataSetChanged();
		ChatFragment.layoutManager.scrollToPosition(ChatFragment.adapter.getItemCount() - 1);
		if (!me && !((MainActivity)mainActivity).chatFragmentOpened){
		    Animation mAnimation = new AlphaAnimation(1, 0);
		    mAnimation.setDuration(ANIMATION_SPEED);
		    mAnimation.setInterpolator(new LinearInterpolator());
		    mAnimation.setRepeatCount(ANIMATION_DURATION * (1000/ANIMATION_SPEED));
		    mAnimation.setRepeatMode(Animation.REVERSE); 
		    button.startAnimation(mAnimation);
		    button.setVisibility(View.VISIBLE);
		}
	}
	
	

}
