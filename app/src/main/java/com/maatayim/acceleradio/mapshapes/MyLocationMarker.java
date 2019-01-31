package com.maatayim.acceleradio.mapshapes;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maatayim.acceleradio.R;

public class MyLocationMarker {

    public static final int C4NET_LOCATION = 0;
    public static final int DEVICE_LOCATION = 1;
    public static final int AVRAGE_LOCATION = 2;
    private static Marker c4netMarker;
    private static Marker deviceMarker;
    private static Marker avgMarker;


    public static void setC4NetMarker(GoogleMap map, LatLng location) {
        if (c4netMarker == null) {
            c4netMarker = getMarker(map, location);
        } else {
            c4netMarker.setPosition(location);
        }
    }

    public static void setDeviceMarker(GoogleMap map, LatLng location) {
        if (deviceMarker == null) {
            deviceMarker = getMarker(map, location);
        } else {
            deviceMarker.setPosition(location);
        }
    }

    public static void setAvgMarker(GoogleMap map) {
        LatLng latLng = getAvreage();
        if (latLng == null) return;

        if (avgMarker == null) {
            avgMarker = getMarker(map, latLng);
        } else {
            deviceMarker.setPosition(latLng);
        }
    }

    public static void setC4netMarkerVisible(boolean visebility) {
        if (c4netMarker == null) return;

        c4netMarker.setVisible(visebility);
    }

    public static void setDeviceMarkerVisible(boolean visebility) {
        if (deviceMarker == null) return;

        deviceMarker.setVisible(visebility);
    }

    public static void setAvgMarkerVisible(boolean visebility) {
        if (avgMarker == null) return;

        avgMarker.setVisible(visebility);
    }

    private static Marker getMarker(GoogleMap map, LatLng location) {
        return map.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_me))
                .draggable(false));
    }

    private static LatLng getAvreage() {
        if (deviceMarker == null && c4netMarker == null) {
            return null;
        }
        if (deviceMarker == null) {
            return c4netMarker.getPosition();
        }
        if (c4netMarker == null) {
            return deviceMarker.getPosition();
        }
        double latAvg = (c4netMarker.getPosition().latitude + deviceMarker.getPosition().latitude) / (double) 2;
        double lonAvg = (c4netMarker.getPosition().longitude + deviceMarker.getPosition().longitude) / (double) 2;
        return new LatLng(latAvg, lonAvg);
    }

}
