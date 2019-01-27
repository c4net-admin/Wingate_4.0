package com.maatayim.acceleradio.mapshapes;

import java.util.ArrayList;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.maatayim.acceleradio.FusedLocationService;
import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.R;

public class Ruler {

	private ArrayList<LatLng> points;
	private ArrayList<Polyline> lines;
	private Marker first;
	private Marker last;
	private GoogleMap map;
	private double totalDistance;
	private double azimut;
	private FusedLocationService fusedLocationService;
	
	private static final int LINE_WIDTH = 12;



	public Ruler(GoogleMap map, FusedLocationService fusedLocationService){
		points = new ArrayList<LatLng>();
		lines = new ArrayList<Polyline>();
		first = null;
		last = null;
		this.map = map;
		totalDistance = 0.0f;
		this.fusedLocationService = fusedLocationService;

	}
	
	public Double addPoint(LatLng point){
		Double distance = new Double(0);
		if (points.isEmpty()){
			first = map.addMarker(new MarkerOptions()
			.position(point)
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_ruler)));
			last = first;
			if (fusedLocationService.getLocation() == null){
				distance = null;
			}
			distance = General.distance(new LatLng(fusedLocationService.getLocation().getLatitude()
					, fusedLocationService.getLocation().getLongitude()),point);
			azimut = General.getAzimut(new LatLng(fusedLocationService.getLocation().getLatitude()
					, fusedLocationService.getLocation().getLongitude()), point);
			totalDistance = 0;
		}
		else{
			if ( ! last.equals(first)){
				last.remove();
			}
			last = map.addMarker(new MarkerOptions()
			.position(point)
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_ruler)));
			distance += General.distance(points.get(points.size()-1), point);
			azimut = General.getAzimut(points.get(points.size()-1), point);
			totalDistance += distance;
		}
		points.add(point);
		Polyline polyline = map.addPolyline(new PolylineOptions()
		.addAll(points)
		.width(LINE_WIDTH)
		.color(Color.BLACK));
		lines.add(polyline);
		Log.d("$$$$$$$$$$$$$$$$$$$$$$$$$$$", "" + distance);
		return distance;
	}
	
	public void clear(){
		for (Polyline p : lines){
			p.remove();
		}
		if (first != null){
			first.remove();
		}
		if (last != null){
			last.remove();
		}
		lines.clear();
		points.clear();
	}
	
	public Marker getFirstMarker(){
		return first;
	}
	
	public double getAzimut(){
		return azimut;
	}
	
	public double getTotalDistance(){
		return totalDistance;
	}
	
	
}