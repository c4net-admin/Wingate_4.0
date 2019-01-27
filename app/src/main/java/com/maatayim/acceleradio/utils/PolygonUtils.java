package com.maatayim.acceleradio.utils;

import java.util.List;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class PolygonUtils {
	
	public static double[] centroid(List<LatLng> points) {
        double[] centroid = { 0.0, 0.0 };

        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).latitude;
            centroid[1] += points.get(i).longitude;
        }

        int totalPoints = points.size();
        Log.d("centroid", String.valueOf(points.size()));
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return centroid;
    }

}
