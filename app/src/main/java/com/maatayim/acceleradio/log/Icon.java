package com.maatayim.acceleradio.log;

import android.app.Activity;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.callsign.CallSign;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.utils.FormatException;

import static com.maatayim.acceleradio.Parameters.DELIMITER_RX;

public class Icon extends LogEntry {

	private static final String ICON = "I";

	private String macAddress;
	private String iconNumber;
	private String iconName;
	private LatLng latlng;
	private String text;
	private boolean lIcon;
	private String connectivity;
	private String age;

	public Icon(String str) throws FormatException {
		super(str);
		parseStr();
		connectivity = "-";
		age = General.getNowTimeLong();
	}

	private void parseStr() throws FormatException {

		if (entry.endsWith(DELIMITER_RX)){
			entry = entry.substring(0,entry.length()-1);
		}
		String[] data = entry.split(",");
//		I,1,0050,01,00,+32.02743,+034.88190,zzxx����������������,@
		if (data.length < 8){
			throw new FormatException("wrong message size");
		}
		macAddress = data[2];
		iconNumber = data[3];
		iconName = data[4];
		try{
			double lat = Double.parseDouble(data[5].replace("+", ""));
			double lng = Double.parseDouble(data[6].replace("+", ""));
			if (lng > 180) {
				lng -= 671.08863d;
			} else { // check fake 0x0035 icon
				//lng += -0.1;
			}

			latlng = new LatLng(lat, lng);
		}catch(Exception e){
			e.printStackTrace();
			throw new FormatException("invalid lat/long format");
		}
		text = data[7] != "\n" ? data[7] : "";

		for (CallSign callSign: Prefs.getInstance().getCallSigns()) {

			if (callSign.getMac().equalsIgnoreCase(text)){
				text = callSign.getName();
				break;
			}

		}

		lIcon = false;
	}

	public void setLIcon(String age){
		lIcon = true;
		this.age = age;
	}

	public static String getIcon() {
		return ICON;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getIconNumber() {
		return iconNumber;
	}

	public String getIconName() {
		return iconName;
	}

	public LatLng getLatlng() {
		return latlng;
	}

	public String getText(){
		return text;
	}
	
	public void setConnectivity(String connectivity){
		this.connectivity = connectivity;
	}
	
	public String getAge(){
		return age;
	}
	
	public void setAge(String age){
		this.age = age;
	}

	@Override
	public void handle(Activity mainActivity, ImageView button) {
		String name = lIcon ? getMacAddress() : getIconName();
		LocationMarker lm = new LocationMarker(getLatlng(), name, ((MainActivity)mainActivity).map
				, ((MainActivity) mainActivity).getMarkersBitmap(this), getText(), MainActivity.markerIndex++
				,getMacAddress(), iconNumber,age);
		lm.setDraggable(false);
		lm.placeOnMap();
		if (lIcon){
			lm.setLMarker(connectivity);
		}
		String key = macAddress + ":" + iconNumber;
		LocationMarker lm2 = Prefs.myMarkers.get(key);
		if (lm2 != null){
			lm2.removeFromMap();
		}
		Prefs.myMarkers.put(key, lm);
		((MainActivity) mainActivity).addMarkerLocationToLocationList(lm, !lIcon);
	}

}
