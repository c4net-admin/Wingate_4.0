package com.maatayim.acceleradio.maps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

public class MapsUtils {
	
	private static final String MAP_PATH = "sergey.png";
	private static TileOverlayOptions customTile;
	private static TileOverlay tileOverlay;

	
	protected static void addCustomMapLevelFromURL(GoogleMap map){
		TileProvider tileProvider = new UrlTileProvider(256,256) {

			@Override
			public URL getTileUrl(int x, int y, int zoom) {
				File f = new File("/mnt/sdcard/sergey/sergey.png");
				f.toURI();
				String s = String.format("http://a.tiles.mapbox.com/v3/examples.map-zr0njcqy/%d/%d/%d.png",zoom, x, y);
				try {
					return new URL(s);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				}
			}
		};
		customTile = new TileOverlayOptions();
		tileOverlay = map.addTileOverlay(customTile.tileProvider(tileProvider));
	}




	protected static void removeCustomMapLevel(){
		tileOverlay.remove();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void addCustomMapLevelSDCARD(GoogleMap map){
		TileProvider myTile = new TileProvider() {

			@Override
			public Tile getTile(int arg0, int arg1, int arg2) {

				File f = new File(Environment.getExternalStorageDirectory().getPath() + "\\" + MAP_PATH);
				Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				Tile temp = new Tile(256, 256, byteArray);
				return temp;
			}
		};
		tileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(myTile));
	}

}
