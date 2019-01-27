package com.maatayim.acceleradio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.google.android.gms.maps.model.Marker;
import com.maatayim.acceleradio.chat.ChatMessage;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.mapshapes.MyPolygon;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class Prefs {
	
	public final static String ATTRIBUTE_STATUS_TEXT = "text";
	public final static String ATTRIBUTE_STATUS_TIME = "time";
	public static final String ATTRIBUTE_MARKER_NAME = "name";
	public static final String SHAPES = "shapes";
	public static final String MARKERS = "markers";
	public static final String POLYGONS = "polygons";
	public static final String CHAT = "chat";
	public static final String MESSAGES = "messages";
	public static final String MARKER_INDEX = "marker_index";
	public static final String INDEX = "index";
	private static final String TAG = "200apps.Prefs";

	
	private static ArrayList<Map<String, String>> statusMessages;
	private ArrayList<Map<String, String>> statusLocations;
	private static ArrayList<Map<String, String>> theirStatusLocations;

	private static String[] from = { ATTRIBUTE_STATUS_TEXT, ATTRIBUTE_STATUS_TIME};
	private static int[] toMessages = { R.id.statusText, R.id.statusTime};
	private static int[] toLocations = { R.id.statusText, R.id.statusTime};
	
	public static Map<String, LocationMarker> myMarkers = new ConcurrentHashMap<String, LocationMarker>();
	public static Map<MyPolygon, MyPolygon> polygons = new ConcurrentHashMap<MyPolygon, MyPolygon>();
	public static Map<Marker, String> markerToKey = new ConcurrentHashMap<Marker, String>();
	
	public static Vector<ChatMessage> messages = new Vector<ChatMessage>();
	
	public static Map<String, String> markersEnum = new HashMap<String, String>();
	
	static Prefs instance = new Prefs();
	public static boolean SHOW_CUSTOM_MAP_MODE = false;
	public static View tableView;
	public static LayoutInflater layoutInflater;
	
	private Prefs(){
		Log.d(TAG, "Prefs cons");
		statusMessages = new ArrayList<Map<String, String>>();
		statusLocations = new ArrayList<Map<String, String>>();
		theirStatusLocations = new ArrayList<Map<String, String>>();
	}
	
	public static Prefs getInstance(Context context){
		return instance;
	}
	
	
	public void addStatusMessages(Map<String, String> m){
		statusMessages.add(m);
	}
	
	public  void addStatusLocations(Map<String, String> m){
		String icon = m.get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + m.get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
		for (int i = 0; i<statusLocations.size(); i++){
			String icon2 = statusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + statusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
			if (icon.equals(icon2)){
				statusLocations.remove(i);
				break;
			}
		}
		statusLocations.add(m);
		Log.d("added to list: ", ""+statusLocations.size());
		Log.d("Prefs statusLocations toString: ", statusLocations.toString());
	}
	
	public void initStatusLocations(){
		//statusLocations.clear();
	}
	
	public static void addTheirStatusLocations(Map<String, String> m, Context context){
		String icon = m.get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + m.get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
		for (int i = 0; i<theirStatusLocations.size(); i++){
			String icon2 = theirStatusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + theirStatusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
			Log.d("locations:", icon + ", " + icon2);
			if (icon.equals(icon2)){
				theirStatusLocations.remove(i);
				break;
			}
		}
		theirStatusLocations.add(m);
	}
	
	public static ArrayList<Map<String, String>> getStatusMessages(){
		return statusMessages;
	}
	
	public ArrayList<Map<String, String>> getMyStatusLocations(){
		return statusLocations;
	}
	
	public ArrayList<Map<String,String>> getTheirStatusLocations(){
		return theirStatusLocations;
	}
	
	public static String[] getFrom(){
		return from;
	}
	
	public static int[] getToMessages(){
		return toMessages;
	}
	
	public static int[] getToLocations(){
		return toLocations;
	}
	
	public static void setSharedPreferences(String pref, String key, Set<String> values, Context context){
		SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putStringSet(key, values);
		editor.commit();
	}
	
	public static Set<String> getSharedPreferences(String pref, String key, Context context){
		SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
		return sp.getStringSet(key, null);
	}
	
	public static void clearSharedPreferences(String pref, Context context){
		context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit().clear().commit();
	}

	public static void clearSms() {
		messages.clear();
		
	}
	
	public static void setPreference(String pref, String key, String value, Context context){
		SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static String getPreference(String pref, String key, Context context){
		SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
		return sp.getString(key, "1");
	}

}
