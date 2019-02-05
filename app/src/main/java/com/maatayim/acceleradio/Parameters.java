package com.maatayim.acceleradio;

import com.google.android.gms.maps.GoogleMap;

public class Parameters {

	public static final String ROOT_FOLDER = "WinGate";
	public static final String FORMAT_ROUND_LOCATION_TO_SHOW = "0.00000";
	public static final int ZOOM_LEVEL = 10; // tal 180902 was 15
	public static final int TIME_OUT_MSEC = 1000; // tal 180902 was 15
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	public static final int[] SUPPORTED_MAPS = {GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_TERRAIN};
	public static final int MAX_COUNTER_LENGTH = 89;
	public static final String DELIMITER_RX = "~<";
	public static final String DELIMITER_TX = "~>";
	public static final String SUB_DELIMITER = "~&";
	public static final String ACK = "ACK";

}
