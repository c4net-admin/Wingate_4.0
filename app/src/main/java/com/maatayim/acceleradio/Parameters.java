package com.maatayim.acceleradio;

import com.google.android.gms.maps.GoogleMap;

public class Parameters {

	public static final String ROOT_FOLDER = "Acceleradio";
	public static final String FORMAT_ROUND_LOCATION_TO_SHOW = "0.00000";
	public static final int ZOOM_LEVEL = 10; // tal 180902 was 15
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	public static final int[] SUPPORTED_MAPS = {GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_TERRAIN};
}
