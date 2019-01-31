package com.maatayim.acceleradio.mapshapes;

import java.text.DecimalFormat;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maatayim.acceleradio.General;

public class LocationMarker {
	
	
	private LatLng location;
	private String iconType;
	private Marker marker;
	private GoogleMap map;
	private Bitmap icon;
	private boolean draggable;
	private String title;
	private int index;
	private String mac;
	private String age;
	private String iconCounter;
	private boolean lMarker;
	private String connectivity;
	
	
	public LocationMarker(LatLng point, String markerType, GoogleMap map, Bitmap bitmap, String title, int index, String mac, String iconCounter, String age){
		this.map = map;
		location = point;
		iconType = markerType;
		draggable = ! iconType.equals(MapShapes.ARROW);
		icon = bitmap;
		this.title = title;
		marker = null;
		this.index = index;
		this.mac = mac;
		this.iconCounter = iconCounter;
		lMarker = false;
		connectivity = "-";
		this.setAge(age);
	}
	
	public void setLMarker(String connectivity){
		lMarker = true;
		this.connectivity = connectivity;
	}
	
	public Marker placeOnMap(){
		marker = map.addMarker(new MarkerOptions()
		.position(location)
		.icon(BitmapDescriptorFactory.fromBitmap(icon))
		.draggable(draggable));
		return marker;
	}
	
	public void removeFromMap(){
		marker.remove();
	}
	
	public String getTitle(){
		return title;
	}
	
	public Marker getMarker(){
		return marker;
	}
	
	public void move(LatLng point){
		location = point;
		marker.setPosition(location);
	}
	
	public String getType(){
		return iconType;
	}
	
	public String getMac(){
		return mac;
	}
	
	public String getIconCounter(){
		return iconCounter;
	}
	
	public String getLocation(){
		double lat = General.truncDouble(location.latitude, 5);
		double lng = General.truncDouble(location.longitude, 5);
		
		return General.precisionFormat(lat, lng);
	}
	
	public String toString(){
		String str = String.valueOf(index);
		String locationStr = getLocation();
		if (lMarker){
			str += "#L#$" + age + "#" +"~" + connectivity + "#";
		}
		str += "I,1," + mac + "," + iconCounter + "," + iconType + "," + locationStr + "," + title + ",\n";
		return str;
		
	}
	
	public boolean isL(){
		return lMarker;
	}
	
	public void setDraggable(boolean draggable){
		this.draggable = draggable;
	}
		
	public String getConnectivity(){
		return connectivity;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}
	
}