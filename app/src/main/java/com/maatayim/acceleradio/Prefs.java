package com.maatayim.acceleradio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.google.android.gms.maps.model.Marker;
import com.maatayim.acceleradio.callsign.CallSign;
import com.maatayim.acceleradio.callsign.CallSignFile;
import com.maatayim.acceleradio.chat.ChatMessage;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.mapshapes.MyPolygon;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class Prefs {
    public static final String TOP_URI = "top_uri";

    public final static String ATTRIBUTE_STATUS_TEXT = "text";
    public final static String ATTRIBUTE_STATUS_TIME = "time";
    public static final String ATTRIBUTE_MARKER_NAME = "name";
    public static final String ATTRIBUTE_AGE = "age";
    public static final String SHAPES = "shapes";
    public static final String MY_LOCATION_TYPE = "my_location_type";
    public static final String MY_MAC_ADDRESS = "my_mac_address";
    public static final String LOCATION_TIME = "location_time";
    public static final String LAST_LATITUDE = "last_latitude";
    public static final String LAST_LONGITUDE = "last_longitude";
    public static final String USER_INFO = "user_info";
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

    private static String[] from = {ATTRIBUTE_STATUS_TEXT, ATTRIBUTE_STATUS_TIME};
    private static int[] toMessages = {R.id.statusText, R.id.statusTime};
    private static int[] toLocations = {R.id.statusText, R.id.statusTime};

    public static Map<String, LocationMarker> myMarkers = new ConcurrentHashMap<String, LocationMarker>();
    public static Map<MyPolygon, MyPolygon> polygons = new ConcurrentHashMap<MyPolygon, MyPolygon>();
    public static Map<Marker, String> markerToKey = new ConcurrentHashMap<Marker, String>();

    public static Vector<ChatMessage> messages = new Vector<ChatMessage>();

    public static Map<String, String> markersEnum = new HashMap<String, String>();

    private static  ArrayList<CallSign> callSigns;

    static Prefs instance;
    public static boolean SHOW_CUSTOM_MAP_MODE = false;
    public static View tableView;
    public static LayoutInflater layoutInflater;

    private Prefs() {
        Log.d(TAG, "Prefs cons");
        statusMessages = new ArrayList<Map<String, String>>();
        statusLocations = new ArrayList<Map<String, String>>();
        theirStatusLocations = new ArrayList<Map<String, String>>();
        callSigns = CallSignFile.getInstance().readFromFile();


    }

    public static Prefs getInstance() {
        if (instance == null){
            instance = new Prefs();
        }

        return instance;
    }


    public  ArrayList<CallSign> getCallSigns() {
        return callSigns;
    }

    public void addStatusMessages(Map<String, String> m) {
        statusMessages.add(m);
    }

    public void addStatusLocations(Map<String, String> m) {
        String icon = m.get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + m.get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
        for (int i = 0; i < statusLocations.size(); i++) {
            String icon2 = statusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + statusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
            if (icon.equals(icon2)) {
                statusLocations.remove(i);
                break;
            }
        }
        statusLocations.add(m);
        Log.d("added to list: ", "" + statusLocations.size());
        Log.d("Prefs statusLocations: ", statusLocations.toString());
    }

    public void initStatusLocations() {
        //statusLocations.clear();
    }

    public static void addTheirStatusLocations(Map<String, String> m, Context context) {
        String icon = m.get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + m.get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
        for (int i = 0; i < theirStatusLocations.size(); i++) {
            String icon2 = theirStatusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[2] + ":" + theirStatusLocations.get(i).get(ATTRIBUTE_STATUS_TEXT).split(",")[3];
            Log.d("locations:", icon + ", " + icon2);
            if (icon.equals(icon2)) {
                theirStatusLocations.remove(i);
                break;
            }
        }
        theirStatusLocations.add(m);
    }

    public  void removeStatusLocation(String key){
        for ( Map<String, String> location : statusLocations) {
            if (location.get(Prefs.INDEX).equals(key)){
                statusLocations.remove(location);
                return;
            }
        }
    }

    public static ArrayList<Map<String, String>> getStatusMessages() {
        return statusMessages;
    }

    public ArrayList<Map<String, String>> getMyStatusLocations() {
        return statusLocations;
    }

    public ArrayList<Map<String, String>> getTheirStatusLocations() {
        return theirStatusLocations;
    }

    public static String[] getFrom() {
        return from;
    }

    public static int[] getToMessages() {
        return toMessages;
    }

    public static int[] getToLocations() {
        return toLocations;
    }

    public static void setSharedPreferencesString(String pref, String key, String value, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static void setSharedPreferencesStringSet(String pref, String key, Set<String> values, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(key, values);
        editor.commit();
    }

    public static void setSharedPreferencesDouble(String pref, String key, double value, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.commit();
    }

    public static void setSharedPreferencesInt(String pref, String key, int value, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getSharedPreferencesString(String pref, String key, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        return sp.getString(key, null);
    }
    public static Set<String> getSharedPreferencesStringSet(String pref, String key, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        return sp.getStringSet(key, null);
    }

    public static int getSharedPreferencesInt(String pref, String key, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }

    public static double getSharedPreferencesDouble(String pref, String key, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(sp.getLong(key, Double.doubleToLongBits(0)));
    }

    public static void clearSharedPreferences(String pref, Context context) {
        context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit().clear().commit();
    }

    public static void clearSms() {
        messages.clear();

    }

    public static void setPreference(String pref, String key, String value, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getPreference(String pref, String key, Context context) {
        SharedPreferences sp = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

}
