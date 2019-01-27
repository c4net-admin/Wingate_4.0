package com.maatayim.acceleradio;

import java.util.ArrayList;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

public class MapPolygone{

	private String polegoneName;
	private ArrayList<Marker> polygoneMarkers;
	private ArrayList<Polyline> polygoneLines;
	private ArrayList<LatLng> polygoneCorners;
	private ArrayList<Polygon> polygoneList;
	private int pressCounter;
	
	
	
	public MapPolygone(){
		polegoneName = "";
		pressCounter = 0;
		polygoneCorners = new ArrayList<LatLng>();
		polygoneList = new ArrayList<Polygon>();
		polygoneMarkers = new ArrayList<Marker>();
		polygoneLines = new ArrayList<Polyline>();
	}
	
	
	
	public ArrayList<Polygon> getPolygone(){
		return polygoneList;
	}
	public ArrayList<Marker> getPolygoneMarkets(){
		return polygoneMarkers;
	}
	public String getMapPolygoneName(){
		return polegoneName;
	}
	public ArrayList<Polyline> getAllPolygoneLines(){
		return polygoneLines;
	}
	public ArrayList<LatLng> getAllPolygoneCorners(){
		return polygoneCorners;
	}
	public int getPolygonePressCounter(){
		return pressCounter;
	}
	
	
	
	
	
	
	
	
	
	public void setPolygone(Polygon polygone){
		this.polygoneList.set(0, polygone);
	}
	public void setPolygoneName(String tempPollygoneName){
		polegoneName = tempPollygoneName;
	}
	public void setPolygonePressCounter(int tempPressCounter){
		pressCounter = tempPressCounter;
	}
	public void setPolygoneMarker(Marker marker, int index){
		polygoneMarkers.set(index, marker);
	}
	public void setPoligoneLine(Polyline tempLine, int index){
		polygoneLines.set(index, tempLine);
	}
	public void setPoligoneCorner(LatLng point, int index){
		polygoneCorners.set(index, point);
	}
	
	
	
	
	
	
	public void addPolygoneToList(Polygon polygone){
		polygoneList.add(polygone);
	}
	public void addLineToPolygone(Polyline line){
		polygoneLines.add(line);
	}
	public void addMarkerToPolygone(Marker marker){
		polygoneMarkers.add(marker);
	}
	public void addCornerToPolygone(LatLng point){
		polygoneCorners.add(point);
	}
	
	
	
	
	public int getCountOfCorners(){
		return polygoneCorners.size()-1;
	}
	
	
	
	

	public void removePolygoneFromMap(){
		for (int i=0; i<polygoneMarkers.size(); i++){
			polygoneMarkers.get(i).remove();
		}
		for (int i=0; i<polygoneLines.size(); i++){
			polygoneLines.get(i).remove();
    	}
		for (int i=0; i<polygoneList.size(); i++){
			polygoneList.get(i).remove();
		}
		polygoneCorners.clear();
		polygoneMarkers.clear();
		polygoneLines.clear();
		polygoneList.clear();
	}
	
	
	
}
