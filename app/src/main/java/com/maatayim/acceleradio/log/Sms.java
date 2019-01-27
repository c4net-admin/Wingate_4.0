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
		if (entry.endsWith("\n")){
			entry = entry.substring(0,entry.length()-1);
		}
		String[] data = entry.split(",");
		if (data.length < 4){
			throw new FormatException("wrong message size");
		}
		macAddresss = data[2];
		text = "";
		for (int i = 3; i<data.length; i++){
			if (data[i].equals("\n")) return;
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
		boolean me = macAddresss.equals("0000");
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
