package com.maatayim.acceleradio.utils;

import java.util.Comparator;

import com.google.android.gms.maps.model.LatLng;

public class ComperatorByCoordinate implements Comparator<LatLng> {

	public enum Axis{
		Lat,
		Lan
	}
	
	public enum Order{
		Inc,
		Dec
	}
	
	private Order order;
	private Axis axis;

	public ComperatorByCoordinate(Order order, Axis axis){
		this.order = order;
		this.axis = axis;
	}
	

	@Override
	public int compare(LatLng arg0, LatLng arg1) {
		
		double pointA = (axis == Axis.Lat ? arg0.latitude : arg0.longitude);
		double pointB = (axis == Axis.Lat ? arg1.latitude : arg1.longitude);
		
		return (int) (Math.signum(pointA - pointB) * (order == Order.Inc ? 1 : -1));
	}

}
