package com.maatayim.acceleradio.maps;

public class MapFolder {
	
	private String name;
	private String extension;
	
	public MapFolder(String name, String extension){
		this.name = name;
		this.extension = extension;
	}
	
	public MapFolder(MapFolder mf){
		this.name = mf.getName();
		this.extension = mf.getExtension();
	}
	
	public String getName() {
		return name.substring(0, name.length());
	}


	public String getExtension() {
		return extension;
	}
	
}
