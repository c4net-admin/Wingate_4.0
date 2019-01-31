package com.maatayim.acceleradio.mapshapes;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maatayim.acceleradio.R;

public class MyLocationMarker {

	public static final int C4NET_LOCATION = 0;
	public static final int PHONE_LOCATION = 1;
	public static final int AVRAGE_LOCATION = 2;
	private Marker marker;
	
	public MyLocationMarker(GoogleMap map, LatLng location){
		marker = map.addMarker(new MarkerOptions()
		.position(location)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_me))
		.draggable(false));
	}
	
	public void updateLocation(LatLng location){
		marker.setPosition(location);
	}
	
	public void removeFromMap(){
		marker.remove();
	}

}
