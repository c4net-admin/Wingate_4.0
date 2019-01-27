package com.maatayim.acceleradio;

import java.util.ArrayList;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

public class MapShapes {

	ArrayList<Polyline> lines = new ArrayList<Polyline>();
	ArrayList<MapPolygone> polygons = new ArrayList<MapPolygone>();
	
	Marker firstMarker;
	Marker lastMarker;
	
	
	
	
	
	
	
	public void addLineToShapes(Polyline line){
		lines.add(line);
	}
	public void addPolygoneToShapes(MapPolygone polygone){
		polygons.add(polygone);
	}
	
	
	public void cleanAllLines(){
		if (lines.size() > 0){
			lines.clear();
			firstMarker.remove();
			lastMarker.remove();
		}
	}
	public void cleanAllPolygones(){
		if (polygons.size() > 0)
			polygons.clear();
	}
	public void cleanLine(int index){
		lines.remove(index);
		firstMarker.remove();
		lastMarker.remove();
	}
	public void cleanPolygone(int index){
		polygons.remove(index);
	}
}
