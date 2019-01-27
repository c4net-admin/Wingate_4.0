package com.maatayim.acceleradio.mapshapes;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;
import com.maatayim.acceleradio.utils.PolygonUtils;

public class MyPolygon {

	private ArrayList<LatLng> points;
	private ArrayList<Polyline> lines;
	private ArrayList<Marker> markers;
	private Marker first;
	private Marker last;
	private GoogleMap map;
	private Polygon polygon;
	private String title;
	private String isClosed = "true";
	private Marker titleMarker;
	
	public static final boolean EDIT_MODE_ON = true;
	public static final boolean EDIT_MODE_OFF = false;


	private static final int LINE_WIDTH = 10;



	public MyPolygon(GoogleMap map){
		points = new ArrayList<LatLng>();
		lines = new ArrayList<Polyline>();
		markers = new ArrayList<Marker>();
		first = null;
		last = null;
		this.map = map;
		polygon = null;
		title = "Polygon";
	}

	public MyPolygon(GoogleMap map, String pointsStr, Activity activity, Context context){
		points = new ArrayList<LatLng>();
		lines = new ArrayList<Polyline>();
		markers = new ArrayList<Marker>();
		this.map = map;
		polygon = null;
		title = pointsStr.substring(0, pointsStr.indexOf(":$:"));
		String[] coords = pointsStr.substring(pointsStr.lastIndexOf(":")+1).split(";");
		for (String s : coords){
			if (s.equals("")){
				break;
			}
			if (s.equals("false")){
				isClosed = "false";
				PolylineOptions options = new PolylineOptions()
						.width(LINE_WIDTH)
						.color(Color.BLACK);
				for (LatLng p : points){
					options.add(p);
				}
				Polyline polyline = map.addPolyline(options);
				lines.add(polyline);
				setTitle(title, activity, context);
				return;
			}
			if (s.equals("true")){
				isClosed = "true";
				break;
			}
			String[] coord = s.split(",");
			LatLng point = new LatLng(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
			points.add(point);
			
		}
		if (points.isEmpty()){
			return;
		}
		polygon = map.addPolygon(new PolygonOptions()
		.addAll(points)
		.strokeColor(Color.BLACK)
		.strokeWidth(10)
		.fillColor(Color.GREEN));
		
		setTitle(title, activity, context);
	}

	public void addPoint(LatLng point){
		
		if (points.isEmpty()){
			first = map.addMarker(new MarkerOptions()
			.position(point)
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_poly)));
			last = first;
		}
		else{
			if ( ! last.equals(first)){
				last.remove();
			}
			last = map.addMarker(new MarkerOptions()
			.position(point)
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_poly)));
		}
		
		points.add(point);
		Polyline polyline = map.addPolyline(new PolylineOptions()
		.addAll(points)
		.width(LINE_WIDTH)
		.color(Color.BLACK));
		lines.add(polyline);
	}

	public void closePolygon(){
		polygon = map.addPolygon(new PolygonOptions()
		.addAll(points)
		.strokeColor(Color.BLACK)
		.strokeWidth(10)
		.fillColor(Color.GREEN));
		for (Polyline p : lines){
			p.remove();
		}
		removeMarkers();
		Prefs.polygons.put(this, this);
		isClosed = "true";
	}


	public void clear(){
		if (lines != null){
			for (Polyline p : lines){
				p.remove();
			}
			lines.clear();
		}
		removeMarkers();
		if (polygon != null){
			polygon.remove();
		}
		if (points != null){
			points.clear();
		}
		if (titleMarker != null){
			titleMarker.remove();
		}
		
		if (!markers.isEmpty()){
			for(Marker m : markers){
				m.remove();
			}
			markers.clear();
		}
	}

	public Marker getFirstMarker(){
		return first;
	}

	public void removeMarkers(){
		if (first != null){
			first.remove();
		}
		if (last != null){
			last.remove();
		}
	}

	public Polygon getPolygon(){
		return polygon;
	}

	public String toString(){
		String str = title + ":$:";
		for (LatLng p : points){
			Double l1 = p.latitude;
			Double l2 = p.longitude;
			String coordl1 = l1.toString();
			String coordl2 = l2.toString();
			str += coordl1 + "," + coordl2 + ";";
		}
		if (str.lastIndexOf(";") >= 0){
			str = str.substring(0, str.lastIndexOf(";"));
		}
		str += ";" + isClosed;
		Log.d("toString " + title, isClosed);
		return str;
	}

	public void setUnclosedPolygon() {
		Prefs.polygons.put(this, this);
		isClosed = "false";
	}
	
	public ArrayList<LatLng> getPoints(){
		return points;
	}
	
	public void setTitle(String name, Activity activity, Context context){
		title = name;
		View view = activity.getLayoutInflater().inflate(R.layout.title_tag, new LinearLayout(context),false);
		View titleView = view.findViewById(R.id.title_layout);

		((TextView)view.findViewById(R.id.title_tag_text_view)).setText(name);
		Bitmap markerBitmap = MainActivity.getBitmapFromView(titleView);
        Log.d("setTitle " + name, String.valueOf(points.size()));

		double[] center = PolygonUtils.centroid(points);
        Log.d("addPoint", String.valueOf(points.size()));
		titleMarker = map.addMarker(new MarkerOptions()
		.position(new LatLng(center[0], center[1]))
		.draggable(true)
		.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));
	}
	
	public String getTitle(){
		return title;
	}
	
	public boolean isMarkerOnPolygon(Marker marker){
		for (Marker m : markers){
			if (m.getId().equals(marker.getId())){
				return true;
			}
		}
		return false;
	}
	
	
	public ArrayList<Marker> getMarkers(){
		return markers;
	}
	
	public void toggleEditMode(boolean on){
		if (on && markers.isEmpty()){
			for (LatLng p : points){
				Marker marker = map.addMarker(new MarkerOptions()
				.position(p)
				.draggable(true)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_poly)));
				markers.add(marker);
			}
		}
		if (!on){
			for (Marker m : markers){
				m.remove();
			}
			markers.clear();
		}
	}
	
	public Marker getTitleMarker(){
		return titleMarker;
	}

	public boolean isClosed() {
		return isClosed.equals("true");
	}

}
