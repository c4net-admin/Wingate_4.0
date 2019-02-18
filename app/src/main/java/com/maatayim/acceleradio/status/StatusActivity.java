package com.maatayim.acceleradio.status;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.mapshapes.MyPolygon;
import com.maatayim.acceleradio.utils.DisplayUtils;
import com.maatayim.acceleradio.utils.SlidingTabLayout;

public class StatusActivity extends AppCompatActivity implements FileChooserFragment.OnFileSelectedListener {

	private static final String TAG = "200apps.StatusActivity";
	private ViewPager mViewPager;
	private SlidingTabLayout mSlidingTabLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_container);
		initViewPager();
		initTabView();
		Log.d(TAG, "onCreate "+Prefs.getInstance().getMyStatusLocations());
	}
	
	
	private void initViewPager() {
		
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager(), this);
		mViewPager.setAdapter(adapter);
	}
	

	private void initTabView() {
		mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

		
		//mSlidingTabLayout.setTabTextSize(getResources().getDimensionPixelSize(R.dimen.tab_text));
		mSlidingTabLayout.setTabWidth(DisplayUtils.getScreenWidth(this)
				/ TabPagerAdapter.TABS);

		mSlidingTabLayout.setTextColors(getResources().getColor(R.color.tab_text_selected),
				getResources().getColor(R.color.tab_text_unselected));


		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
		// it's PagerAdapter set.
		mSlidingTabLayout.setViewPager(mViewPager);


		mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

			@Override
			public int getIndicatorColor(int position) {
				return getResources().getColor(R.color.tab_indicator);
			}

			@Override
			public int getDividerColor(int position) {
				return Color.TRANSPARENT;
			}

		});
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	@Override
	public void onFileSelected(String file) {
		Log.d("file name: ", file);
		clearMap();
		importMap(file);
		Map<String, String> m;
		m = new HashMap<String, String>();
		m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "UTIL: " + "Map imported");
		m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
		Prefs.getInstance().addStatusMessages(m);
		LogFragment.notifyChanges();
		
	}
	
	protected void clearMap() {
		Prefs.clearSharedPreferences(Prefs.SHAPES, this);
		for (Entry<String, LocationMarker> m : Prefs.myMarkers.entrySet()){
			LocationMarker lm = Prefs.myMarkers.remove(m.getKey());
			lm.removeFromMap();
		}
		Prefs.myMarkers.clear();
		for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
			MyPolygon p = Prefs.polygons.remove(poly.getKey());
			p.clear();
		}
		Prefs.polygons.clear();
		Prefs.getInstance().getMyStatusLocations().clear();
		Prefs.getInstance().getTheirStatusLocations().clear();
		MyLocationsFragment.notifyChanges();
		TheirLocationsFragment.notifyChanges();
		MainActivity.allyCounter = 1;
		MainActivity.enemyCounter = 1;
		
	}
	
	
	public static BufferedReader in;
	public void importMap(String fileName){
		ReadFileFromDevice(fileName);
	}
	
	public void ReadFileFromDevice(String filename){
		filename = SettingsFragment.EXPORTED_MAPS_DIRECTORY + filename;
		File Root = Environment.getExternalStorageDirectory();
		File mapFile = new File(Root, filename);
		try {
			FileReader mapReader = new FileReader(mapFile);
			in = new BufferedReader(mapReader);
			String s;
			ArrayList<String> markers = new ArrayList<String>();
			ArrayList<String> polygons = new ArrayList<String>();
			
			String mode = "";

			while ((s = in.readLine()) != null){
				if (s.equals("markers")){
					mode = "markers";
					continue;
				}
				if (s.equals("polygons")){
					mode = "polygons";
					continue;
				}
				if (mode.equals("markers")){
					s = addMarkerToList(s);
					markers.add(s);
				}
				else{
					polygons.add(s);
				}
				addMarkers(markers);
				addPolygons(polygons);
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Error Loading Map! File not found", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, "Error Loading Map!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		Toast.makeText(this, "Map Loaded!", Toast.LENGTH_SHORT).show();
	}
	
	
	private String addMarkerToList(String s){
		String[] data = s.split(",");
		Map<String, String> m;
		m = new HashMap<String, String>();
		String connectivity = "-";
		m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "I,1," + data[2] + ","+ data[3] + "," + data[4]
				+ "," + data[5] + "," + data[6] +"," + General.getAge(data[8]) + "," + connectivity + ",\n");
		m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
		m.put(Prefs.ATTRIBUTE_MARKER_NAME, data[7]);
		Prefs.getInstance().addStatusLocations(m);
		Log.d(TAG, "addMarkerToList "+Prefs.getInstance().getMyStatusLocations());
		Log.d("StatusActivity  ","statusLocations "+ Prefs.getInstance().getMyStatusLocations().toString());
		Log.d("addMarkerToList ", m.get(Prefs.ATTRIBUTE_STATUS_TEXT));
		MyLocationsFragment.notifyChanges();
		String sNoAge = s.substring(0, s.lastIndexOf(","));
		return sNoAge.substring(0, sNoAge.lastIndexOf(",")+1);
	}
	

	private void addPolygons(ArrayList<String> polygons) {
		MainActivity.polygonsToLoad = new HashSet<String>(polygons);
		
	}

	private void addMarkers(ArrayList<String> markers) {
		
		MainActivity.markersToLoad = new HashSet<String>(markers);
	}



}
