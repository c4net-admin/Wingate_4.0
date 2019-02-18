package com.maatayim.acceleradio.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.mapshapes.MyPolygon;
import com.maatayim.acceleradio.status.MyLocationsFragment;
import com.maatayim.acceleradio.status.TheirLocationsFragment;

import java.util.Map;

public class MapUtils {

    public static void clearMap(Context context) {
        Prefs.clearSharedPreferences(Prefs.SHAPES, context);
        for (Map.Entry<String, LocationMarker> m : Prefs.myMarkers.entrySet()){
            LocationMarker lm = Prefs.myMarkers.remove(m.getKey());
            lm.removeFromMap();
        }
        Prefs.myMarkers.clear();
        for (Map.Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
            MyPolygon p = Prefs.polygons.remove(poly.getKey());
            p.clear();
        }
        Prefs.polygons.clear();
        Prefs.getInstance().getMyStatusLocations().clear();
        Prefs.getInstance().getTheirStatusLocations().clear();
        MyLocationsFragment.notifyChanges();
        TheirLocationsFragment.notifyChanges();
        MainActivity.allyCounter = 1;
        MainActivity.enemyCounter = 1;
        Toast.makeText(context, "Map Cleared!", Toast.LENGTH_SHORT).show();
    }

    public static void addMyCurrentLocation(LatLng latLng, Context context){
        Prefs.setSharedPreferencesDouble(Prefs.USER_INFO,Prefs.LAST_LATITUDE,latLng.latitude,context);
        Prefs.setSharedPreferencesDouble(Prefs.USER_INFO,Prefs.LAST_LONGITUDE,latLng.longitude,context);
    }
}
