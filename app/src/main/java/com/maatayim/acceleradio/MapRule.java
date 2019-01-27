package com.maatayim.acceleradio;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

public class MapRule{
	
	private int ruleLastDistance;
	private int ruleTotalDistance;
	private ArrayList<Marker> ruleMarkers;
    private ArrayList<Polyline> ruleLines;
    private ArrayList<LatLng> rulePoints;
    private int pressCounter;
    
    
    
    
    
    
    
    public MapRule() {
    	setRuleLastDistanse(0);
    	setRuleTotalDistanse(0);
    	setRuleMarkers(new ArrayList<Marker>());
    	setLinesOfRule(new ArrayList<Polyline>());
    	setRulePoints(new ArrayList<LatLng>());
    	setPressCounter(0);
	}
    
    
    
    
    
    
    
    
    public void deleteAllLinesFromMap(){
    	
    	for (int i=0; i<ruleMarkers.size(); i++){
    		ruleMarkers.get(i).remove();
    	}
    	for (int i=0; i<ruleLines.size(); i++){
    		ruleLines.get(i).remove();
    	}
    	
    }
    
    
    
    
    
    
    
    
    
    
	public int getRuleLastDistance() {
		return ruleLastDistance;
	}
	public int getRuleTotalDistance() {
		return ruleTotalDistance;
	}
	public ArrayList<Marker> getRuleMarkers() {
		return ruleMarkers;
	}
	public ArrayList<Polyline> getLinesOfRule() {
		return ruleLines;
	}
	public ArrayList<LatLng> getRulePoints() {
		return rulePoints;
	}
	public int getPressCounter(){
		return pressCounter;
	}
	
	
	
	
	
	
	
	public void setRuleLastDistanse(int ruleLastDistanse) {
		this.ruleLastDistance = ruleLastDistanse;
	}
	public void setRuleTotalDistanse(int ruleTotalDistanse) {
		this.ruleTotalDistance = ruleTotalDistanse;
	}
	public void setRuleMarkers(ArrayList<Marker> ruleMarkers) {
		this.ruleMarkers = ruleMarkers;
	}
	public void setLinesOfRule(ArrayList<Polyline> linesOfRule) {
		this.ruleLines = linesOfRule;
	}
	public void setRulePoints(ArrayList<LatLng> rulePoints) {
		this.rulePoints = rulePoints;
	}
	public void setPressCounter(int tempPressCounter){
		pressCounter = tempPressCounter;
	}
	
	
	
	
	
	
	
	
	
	public void addMarkerToRule(Marker marker){
		ruleMarkers.add(marker);
	}
	public void addPointToRule(LatLng point){
		rulePoints.add(point);
	}
	public void addLineToRule(Polyline line){
		ruleLines.add(line);
	}
	public void addDistance(float distance){
		ruleTotalDistance = (int) (ruleTotalDistance + distance);
	}
	
	

    
    
}
