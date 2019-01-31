package com.maatayim.acceleradio;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.maatayim.acceleradio.chat.ChatFragment.OnSmsSent;
import com.maatayim.acceleradio.chat.ChatMessage;
import com.maatayim.acceleradio.log.Icon;
import com.maatayim.acceleradio.log.LogEntry;
import com.maatayim.acceleradio.log.LogEntryBuilder;
import com.maatayim.acceleradio.log.Sms;
import com.maatayim.acceleradio.maps.CustomMapTileProvider;
import com.maatayim.acceleradio.maps.MapFolder;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.mapshapes.MyLocationMarker;
import com.maatayim.acceleradio.mapshapes.MyPolygon;
import com.maatayim.acceleradio.mapshapes.Ruler;
import com.maatayim.acceleradio.status.LogFragment;
import com.maatayim.acceleradio.status.StatusActivity;
import com.maatayim.acceleradio.usbserial.UsbService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;


public class MainActivity extends Activity
implements OnMapClickListener, LocationListener, OnSmsSent,
OnMarkerDragListener, OnMarkerClickListener{

	private Animation trash_animation;
	private float[] results = new float[2];
	private SlidingMenu chatView, statusView;
	public GoogleMap map;
	private Crouton distanceNotif;
	private ImageView trash;
	private FusedLocationService fusedLocationService;
	private int currentMapType = 1;
	private TileOverlay tileOverlay;

	boolean ruleButtonPressed = false;
	boolean polygoneButtonPressed = false;
	boolean pressToClose = false;

	public static int allyCounter = 0;
	public static int enemyCounter = 0;
	public static int markerIndex = 1;
	private boolean service = true;
	public boolean chatFragmentOpened = false;

	private EditText edit_newMarkerName;
	private Button button_save;
	private Dialog markerName;
	private DrawState drawState = DrawState.None;
	private SparseArray<ImageView> buttons;

	private MyPolygon polygon;
	private MyPolygon currentEditablePolygon;
	private Ruler ruler;
	private Marker polygonStartOnMarker;

	private Dialog polygonName;
	protected TextView edit_newPolygoneName;

	private static final String MAP_EXTENSION = ".png";
	protected static final int MARKER_NAME_LENGTH = 10;
	public static final String MY_MAC_ADDRESS = "0000";
	private static final String MAPS_DIRECTORY = "Acceleradio"+File.separator+"Maps"+File.separator;

	boolean isFirstLocationChange = true;
	protected String markersName;
	protected String polyName;
	private boolean polygonDrawing = false;

	private UsbService usbService;
	private MyHandler mHandler;
	private File mapsDirectory;
	public static Set<String> markersToLoad;
	public static Set<String> polygonsToLoad;


	private enum DrawState{
		Enemy,
		Ally,
		Line,
		Polygon,
		None
	}




	/************************************************************************************
	 ******************************* Activity Initialization ****************************
	 ************************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar bar = getActionBar();
		bar.hide();

		initButtons();

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},100);

		}else {

            // FIXME: 12/14/16 Callback
            ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    if (map == null) {
                        finish();
                        return;
                    }

                    initMarkerEnum();
                    mapSettings();
                    initMapsDirectory();
                    initChatView();
                    initStatusView();
                    loadSavedMap();
                    initMapViewLocation();

                    //usb data income handler
                    mHandler = new MyHandler(MainActivity.this);
                }
            });

            fusedLocationService = new FusedLocationService(MainActivity.this.getApplicationContext(), MainActivity.this);

            Prefs.layoutInflater = getLayoutInflater();
        }
	}

	private void initMapViewLocation() {

		//TODO Open last known location
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.d("onResume", "start listening");
		setFilters();  // Start listening notifications from UsbService
		startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
		checkForLoadedMap();
	}



	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mUsbReceiver);
		unbindService(usbConnection);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (fusedLocationService !=null) {
            fusedLocationService.startListening();
        }
	}



	@Override
	protected void onStop() {
		super.onStop();
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		fusedLocationService.stopListening();
		Crouton.cancelAllCroutons();
		f.delete();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (drawState == DrawState.Polygon){
			polygon.setUnclosedPolygon();
		}
		saveMap();
	}


	/////////////////////////////////////////////USB///////////////////////////////////////////

	private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras)
	{
		if(UsbService.SERVICE_CONNECTED == false)
		{
			Intent startService = new Intent(this, service);
			if(extras != null && !extras.isEmpty())
			{
				Set<String> keys = extras.keySet();
				for(String key: keys)
				{
					String extra = extras.getString(key);
					startService.putExtra(key, extra);
				}
			}
			startService(startService);
		}
		Intent bindingIntent = new Intent(this, service);
		bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}


	private void setFilters()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
		filter.addAction(UsbService.ACTION_NO_USB);
		filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
		filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
		filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
		registerReceiver(mUsbReceiver, filter);
	}


	/*
	 * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
	 */
	private static class MyHandler extends Handler 
	{
		private final WeakReference<MainActivity> mActivity;
		private static String incomingData = "";

		public MyHandler(MainActivity activity) 
		{
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case UsbService.MESSAGE_FROM_SERIAL_PORT:
				String data = (String) msg.obj;
				incomingData += data;
				if (data.equals("\n")){
					mActivity.get().onDataReceived(incomingData);
					incomingData = "";
				}
				else if (data.contains("\n")){
					String[] buffer = incomingData.split("\n");
					incomingData = buffer[0];
					mActivity.get().onDataReceived(incomingData);
					incomingData = "";
					for (int i = 1; i<buffer.length; i++)
						incomingData += buffer[i];
				}
				break;
			}
		}
	}


	/*
	 * Notifications from UsbService will be received here.
	 */
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{
			String msg = "";
			if(arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB PERMISSION GRANTED
			{
				msg = "Cable connected";
			}else if(arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)) // USB PERMISSION NOT GRANTED
			{
				msg = "No permission granted";
			}else if(arg1.getAction().equals(UsbService.ACTION_NO_USB)) // NO USB CONNECTED
			{
				msg = "No USB connected";
			}else if(arg1.getAction().equals(UsbService.ACTION_USB_DISCONNECTED)) // USB DISCONNECTED
			{
				Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
				msg = "Cable disconnected";
			}else if(arg1.getAction().equals(UsbService.ACTION_USB_NOT_SUPPORTED)) // USB NOT SUPPORTED
			{
				Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
				msg = "USB device not supported";
			}

			showMessage(msg,msg.equals("Cable connected") ? 0 : 2);
			LogFile.getInstance(MainActivity.this).appendLog("COM: " + msg);
			Map<String, String> m;
			m = new HashMap<String, String>();
			m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "COM: " + msg);
			m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
			Prefs.getInstance(MainActivity.this).addStatusMessages(m);
			LogFragment.notifyChanges();
		}
	};


	private final ServiceConnection usbConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) 
		{
			usbService = ((UsbService.UsbBinder) arg1).getService();
			usbService.setHandler(mHandler);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) 
		{
			usbService = null;
		}
	};
	private File f;


	public void onDataReceived(String msg) {
		Map<String, String> m;
		m = new HashMap<String, String>();
		m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "RX: " + msg);
		m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
		Prefs.getInstance(this).addStatusMessages(m);

		LogFile.getInstance(this).appendLog("RX: " + (msg.contains("\n") ? msg.substring(0,msg.length()-1) : msg));
		String error = "";
		LogEntry le = null;
		try {
			le = LogEntryBuilder.build(msg);
		} catch (com.maatayim.acceleradio.utils.FormatException e) {
			e.printStackTrace();
			error = e.getMessage();
		}

		//error in message format
		if (le == null){
			error = error.equals("") ? "Unknown message type" : error;
			m = new HashMap<String, String>();
			m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "Error: " + error + msg);
			m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
			Prefs.getInstance(this).addStatusMessages(m);
			LogFile.getInstance(this).appendLog(error);
			return;
		}
		le.handle(this, (ImageView)findViewById(R.id.chat_btn));
		LogFragment.notifyChanges();
	}

	public void send(String msg){
		if(!msg.equals(""))
		{
			if(usbService != null){ // if UsbService was correctly binded, Send data
				usbService.write(msg.getBytes(Charset.forName("UTF-8")));
				Map<String, String> m;
				m = new HashMap<String, String>();
				m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "TX: " + msg.trim() + "\r\n");
				m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
				Prefs.getInstance(this).addStatusMessages(m);
				LogFragment.notifyChanges();
				LogFile.getInstance(this).appendLog("TX: " + msg);
			}
		}
	}


	@Override
	public void onSmsSent(String data) {
		String sms = "T,1," + MY_MAC_ADDRESS + "," + data + "\n";
		Sms msg = null;
		try {
			msg = new Sms(sms);
		} catch (com.maatayim.acceleradio.utils.FormatException e) {
			e.printStackTrace();
		}
		if (msg != null){
			msg.handle(this, (ImageView)findViewById(R.id.chat_btn));
			send(sms);
		}
	}



	////////////////////////////////////////////////init////////////////////////////////////////////////

	private void initButtons(){
		buttons = new SparseArray<ImageView>();
		buttons.append(R.id.enemy_btn, (ImageView) findViewById(R.id.enemy_btn));
		buttons.append(R.id.ally_btn, (ImageView) findViewById(R.id.ally_btn));
		buttons.append(R.id.ruller_btn, (ImageView) findViewById(R.id.ruller_btn));
		buttons.append(R.id.polygon_btn, (ImageView) findViewById(R.id.polygon_btn));
		buttons.append(R.id.map_mode, (ImageView) findViewById(R.id.map_mode));
		buttons.append(R.id.chat_btn, (ImageView) findViewById(R.id.chat_btn));
	}



	private void mapSettings(){
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.setOnMapClickListener(this);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMapToolbarEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(false);
		//		map.setPadding(0, 0, 1500, 0); convert dp to pixels dynamically
		map.setOnMarkerDragListener(this);
		map.setOnMarkerClickListener(this);
	}


	private void initMapsDirectory() {
		mapsDirectory = new File(Environment.getExternalStorageDirectory(),MAPS_DIRECTORY);
		mapsDirectory.mkdirs();
		
		f = new File(mapsDirectory, "a.a");
		FileWriter mapWriter = null;
		try {
			mapWriter = new FileWriter(f, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter out = new BufferedWriter(mapWriter); 
		try {
			out.append('a');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//update android files media scanner for the log files being visible without rebooting
		MediaScannerConnection.scanFile(this, new String[] {

				f.getAbsolutePath()},

				null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri){
			}

		});

	}


	private void initMarkerEnum() {
		Prefs.markersEnum.put("Enemy", "00");
		Prefs.markersEnum.put("Ally", "01");
		Prefs.markersEnum.put("Me", "10");
		Prefs.markersEnum.put("L", "11");
		Prefs.markersEnum.put("11", "L");
		Prefs.markersEnum.put("00", "Enemy");
		Prefs.markersEnum.put("01", "Ally");
		Prefs.markersEnum.put("10", "Me");	
	}



	///////////////////////////////////////////////////////Save load map////////////////////////////////////////////


	public void loadSavedMap(){
		clearData();
		loadMarkers();
		loadPolygons();
		loadChat();
	}


	private void clearData() {
		map.clear();
		Prefs.clearSms();
		Prefs.myMarkers.clear();
		Prefs.polygons.clear();
		Prefs.markerToKey.clear();
	}


	private void checkForLoadedMap() {
		if (markersToLoad != null){
			drawMarkers(markersToLoad, true);
			int num = markersToLoad.size();
			allyCounter = num / 2;
			enemyCounter = (num+1) / 2;
			markersToLoad = null;
		}
		if (polygonsToLoad != null){
			drawPolygons(polygonsToLoad);
			polygonsToLoad = null;
		}

	}


	private void loadChat() {
		Set<String> chat = Prefs.getSharedPreferences(Prefs.CHAT, Prefs.MESSAGES, getApplicationContext());
		if (chat == null){
			return;
		}
		String[] msgs = null;
		for (String s : chat){

			msgs = s.split("@");
		}


		Prefs.clearSms();
		for (String msg : msgs){
			String[] message = msg.split(":");
			long date = Long.parseLong(message[0]);
			boolean isMine = message[1].equals("true");
			ChatMessage cm = new ChatMessage(message[2], message[3], isMine, date);
			Prefs.messages.add(cm);
		}
	}



	private void loadPolygons() {
		Set<String> polygons = Prefs.getSharedPreferences(Prefs.SHAPES, Prefs.POLYGONS, getApplicationContext());

		drawPolygons(polygons);
	}




	private void drawPolygons(Set<String> polygons) {
		if (polygons == null){
			return;
		}

		for (String s : polygons){
			MyPolygon myPoly = new MyPolygon(map,s, this, getApplicationContext());
			Prefs.polygons.put(myPoly, myPoly);
		}
	}



	public void loadMarkers(){
		Set<String> markers = Prefs.getSharedPreferences(Prefs.SHAPES, Prefs.MARKERS, getApplicationContext());
		if (markers == null){
			return;
		}

		drawMarkers(markers, false);
		
		int num = Integer.parseInt(Prefs.getPreference(Prefs.MARKER_INDEX, Prefs.INDEX, getApplicationContext()));
		allyCounter = num/2;
		enemyCounter = (num + 1) / 2;
	}



	private void drawMarkers(Set<String> markers, boolean isLoaded) {
		ArrayList<String> sortedMarkers = new ArrayList<String>(markers);
		Collections.sort(sortedMarkers);

		Prefs.getInstance(this).initStatusLocations();
		for (String s : sortedMarkers){
			if (s.equals("")){
				return;
			}
			boolean isL = false;
			String index = "";
			String age = "";
			String connectivity = "";
			if (s.contains("#L#")){
				isL = true;
				age = s.substring(s.indexOf('$') + 1, s.lastIndexOf('~')-1);
				index = s.substring(0,s.indexOf("#"));
				connectivity = s.substring(s.indexOf('~') + 1, s.lastIndexOf('#'));
			}
			else{
				index = s.substring(0,s.indexOf("I"));
			}
			s = s.substring(s.indexOf("I"));
			Icon icon = null;
			try {
				icon = new Icon(s);
			} catch (com.maatayim.acceleradio.utils.FormatException e) {
				e.printStackTrace();
			}
			if (isL){
				icon.setLIcon(age);
			}

			Bitmap markerBitmap = getMarkersBitmap(icon);

			LocationMarker lm = new LocationMarker(icon.getLatlng(), icon.getIconName(), map, markerBitmap, icon.getText()
					, Integer.parseInt(index), icon.getMacAddress(), icon.getIconNumber(), icon.getAge());

			if (isL){
				lm.setLMarker(connectivity);
			}

			Marker m = lm.placeOnMap();
			Prefs.myMarkers.put(icon.getMacAddress() + ":" + icon.getIconNumber(), lm);
			Prefs.markerToKey.put(m, icon.getMacAddress() + ":" + icon.getIconNumber());

			if(!isLoaded){
				addMarkerLocationToLocationList(lm, !isL);
			}
		}

	}



	public void saveMap(){
		saveMarkers();
		savePolygons();
		saveChat();
	}


	private void saveChat() {
		Set<String> chat = new LinkedHashSet<String>();
		String chatMsgs = "";
		for (ChatMessage c : Prefs.messages){
			chatMsgs += c.toString() + "@";
		}
		if (!chatMsgs.isEmpty()){
			chatMsgs.substring(0, chatMsgs.length()-1);
			chat.add(chatMsgs);
			Prefs.setSharedPreferencesString(Prefs.CHAT, Prefs.MESSAGES, chat, getApplicationContext());
		}
	}



	private void savePolygons() {
		Set<String> markers = new LinkedHashSet<String>();
		for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
			markers.add(poly.getValue().toString());
		}
		if (polygonDrawing){
			markers.add(polygon.toString());
		}
		Prefs.setSharedPreferencesString(Prefs.SHAPES, Prefs.POLYGONS, markers, getApplicationContext());

	}




	public void saveMarkers(){
		Set<String> markers = new LinkedHashSet<String>();
		for (Entry<String, LocationMarker> lm : Prefs.myMarkers.entrySet()){
			markers.add(lm.getValue().toString());
		}
		Prefs.setSharedPreferencesString(Prefs.SHAPES, Prefs.MARKERS, markers, getApplicationContext());
		
		Prefs.setPreference(Prefs.MARKER_INDEX, Prefs.INDEX, ""+(allyCounter + enemyCounter), getApplicationContext());
	}



	////////////////////////////////////////////////////////map////////////////////////////////////////////////////



	@Override
	public boolean onMarkerClick(Marker marker) {

		if (currentEditablePolygon != null){
			currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
		}
		if (polygon != null && (marker.equals(polygon.getFirstMarker()) || marker.equals(polygonStartOnMarker))){
			polygon.closePolygon();
			polygonStartOnMarker = null;
			showPolygonNameDialog();
			setState(DrawState.None);
			clearSelection();
			return true;
		}

		if (drawState == DrawState.Line && ruler != null){
			drawRuler(marker.getPosition());
			return true;
		}

		if (drawState == DrawState.Polygon && polygon != null){
			if (polygon.getPoints().isEmpty()){
				polygonStartOnMarker = marker;
			}
			polygon.addPoint(marker.getPosition());
		}

		if (ruler != null && ruler.getFirstMarker() != null && ruler.getFirstMarker().equals(marker)){
			drawRuler(marker.getPosition());
			return true;
		}
		String mes = getString(R.string.rule_message_distanse);	
		Location myLocation = fusedLocationService.getLocation();
		LatLng point = marker.getPosition();
		if (myLocation != null){
			Location.distanceBetween(point.latitude, point.longitude, myLocation.getLatitude(), myLocation.getLongitude(), results);
			double azimut = General.getAzimut(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
			showMessage(General.convertLocationToString(point) + ", " + mes + " " + Math.round(results[0]) + 
					getString(R.string.unit_abbreviation) + ", " + getString(R.string.rule_message_angle) + Math.round(azimut) +".", 0);
		}
		else{
			showMessage("No Location Service", 2);
		}
		return false;
	}


	public Bitmap loadBitmapFromView(View v) {
		if (v.getMeasuredHeight() <= 0) {
			v.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
			v.draw(c);
			return b;
		}
		Bitmap b = Bitmap.createBitmap( v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);                
		Canvas c = new Canvas(b);
		v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
		v.draw(c);
		return b;
	}



	private void clearSelection(){
		int k = 0;
		for (int i = 0; i < buttons.size(); i++){
			k = buttons.keyAt(i);
			buttons.get(k).setSelected(false);
		}
		polygonDrawing = false;
	}




	//	@Override
	public void onLocationChanged(Location location) {
		if(isFirstLocationChange){
			isFirstLocationChange = false;
			initMapPosition(new LatLng(location.getLatitude(), location.getLongitude()));
		}
	}


	public void initMapPosition(LatLng mapCenter){
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, Parameters.ZOOM_LEVEL));
		map.setMyLocationEnabled(false);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.getUiSettings().setZoomControlsEnabled(false);

		int myLocationType = Prefs.getSharedPreferencesInt(Prefs.MARKERS,Prefs.MY_LOCATION_TYPE,this);
		if (myLocationType == MyLocationMarker.PHONE_LOCATION) {
			MyLocationMarker locationMarker = new MyLocationMarker(map, mapCenter);
		}
	}



	private void switchMaps(){

		currentMapType++;

		if(tileOverlay != null){
			tileOverlay.remove();
		}
		if (Prefs.SHOW_CUSTOM_MAP_MODE && currentMapType >= Parameters.SUPPORTED_MAPS.length){
			ArrayList<MapFolder> maps = new ArrayList<MapFolder>();
			loadMapFolders(maps);
			if (maps.isEmpty()){
				currentMapType = 0;
				map.setMapType(Parameters.SUPPORTED_MAPS[0]);
				return;
			}
			CustomMapTileProvider cmtp = new CustomMapTileProvider();
			MapFolder mapFolder = new MapFolder(maps.get(currentMapType - Parameters.SUPPORTED_MAPS.length));
			Toast.makeText(getApplicationContext(), mapFolder.getName(), Toast.LENGTH_SHORT).show();
			cmtp.setMapFolder(mapFolder.getName() + "//", mapFolder.getExtension());
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(cmtp));

			if (currentMapType == Parameters.SUPPORTED_MAPS.length + maps.size() - 1){
				currentMapType = -1;
			}
			return;
		}

		if (currentMapType == Parameters.SUPPORTED_MAPS.length || currentMapType == -1){
			currentMapType = 0;
		}

		map.setMapType(Parameters.SUPPORTED_MAPS[currentMapType]);

	}



	private void loadMapFolders(ArrayList<MapFolder> maps) {

		String[] folders = mapsDirectory.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		if (folders == null){
			return;
		}
		for (String s : folders){
			String extension = MAP_EXTENSION;
			maps.add(new MapFolder(s,extension));
		}
	}




	private void drawRuler(LatLng point) {
		Double dist = ruler.addPoint(point);
		if (dist == null){
			showMessage("No Location Service", 2);
			return;
		}
		int distance = (int)dist.doubleValue();

		String distanseText = getString(R.string.rule_message_distanse);
		String totalDistanseText = getString(R.string.rule_message_totalDistanse);
		String angleText = getString(R.string.rule_message_angle);

		int totalDistance = (int)ruler.getTotalDistance();
		String finalMessage;

		if (totalDistance == 0){
			finalMessage = General.convertLocationToString(point) +", "+ 
					distanseText +" "+ distance + getString(R.string.unit_abbreviation) + ", " +
					angleText + " " + Math.round(ruler.getAzimut()) +".";
		}

		else{
			finalMessage = General.convertLocationToString(point) +", "+ 
					distanseText +" "+ distance + getString(R.string.unit_abbreviation) + ", "+
					totalDistanseText+ " " + totalDistance + getString(R.string.unit_abbreviation) + ", " + 
					angleText + " " + Math.round(ruler.getAzimut()) +".";
		}

		showMessage(finalMessage, 1);

	}





	private void drawPoligon(LatLng point){
		polygon.addPoint(point);
		polygonDrawing  = true;
	}




	private void setState(DrawState state){
		if (drawState == state){
			drawState = DrawState.None;
			return;
		}
		drawState = state;
	}



	//button from the menu
	public void menuButtonAction(View view){
		boolean isSelected = view.isSelected();
		clearSelection();
		if (currentEditablePolygon != null){
			currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
		}
		if (polygon != null && !polygon.getPoints().isEmpty() && drawState == DrawState.Polygon){
			polygon.removeMarkers();
			polygon.setUnclosedPolygon();
			polygonStartOnMarker = null;
			showPolygonNameDialog();
		}

		if (ruler != null){
			ruler.clear();
		}

		if (distanceNotif != null){
			distanceNotif.cancel();
		}

		view.setSelected(!isSelected);

		switch(view.getId()){
		case R.id.map_mode :
			switchMaps();
			break;
		case R.id.enemy_btn :
			setState(DrawState.Enemy);
			break;
		case R.id.ally_btn :
			setState(DrawState.Ally);
			break;
		case R.id.polygon_btn :
			polygon = new MyPolygon(map);
			setState(DrawState.Polygon);
			break;
		case R.id.ruller_btn :
			ruler = new Ruler(map, fusedLocationService);
			setState(DrawState.Line);
			break;
		case R.id.chat_container :
			openChat();
			((ImageView)findViewById(R.id.chat_btn)).clearAnimation();
			((ImageView)findViewById(R.id.chat_btn)).setVisibility(View.INVISIBLE);
			break;	
		case R.id.chat_btn :
			openChat();
			((ImageView)findViewById(R.id.chat_btn)).clearAnimation();
			((ImageView)findViewById(R.id.chat_btn)).setVisibility(View.INVISIBLE);
			break;	
		}
	}



	public void addMarkerLocationToLocationList(LocationMarker lm, boolean icon){
		Map<String, String> m;
		m = new HashMap<String, String>();
		String connectivity = icon ? "-" : lm.getConnectivity();
		m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "I,1," + lm.getMac() + ","+ lm.getIconCounter() + "," + lm.getType()
				+ "," + lm.getLocation() +"," + lm.getAge() + "," + connectivity + ",\n");
		m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
		m.put(Prefs.ATTRIBUTE_MARKER_NAME, lm.getTitle());
		if (icon){
			Prefs.getInstance(this).addStatusLocations(m);
			//MyLocationsFragment.notifyChanges();
		}
		else{
			Prefs.addTheirStatusLocations(m, getApplicationContext());
			//TheirLocationsFragment.notifyChanges();
		}
	}




	private void drawMarkerOnMap(LatLng point){
		int res = 0;
		String marker = null;
		ruleButtonPressed = false;
		polygoneButtonPressed = false;
		boolean draggable = true;
		switch (drawState){
		case Enemy :
			res = R.drawable.ic_enemy;
			marker = "Enemy";
			showMarkerNameDialog(point, draggable, res, marker);
			break;
		case Ally :
			res = R.drawable.ic_ally;
			marker = "Ally";
			showMarkerNameDialog(point, draggable, res, marker);
			break;
		default:
			break;		
		}

	}


	@Override
	public void onMarkerDrag(Marker marker) {
		for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
			if(poly.getValue().getTitleMarker().equals(marker)){
				if (currentEditablePolygon != null){
					currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
				}
				currentEditablePolygon = poly.getValue();
				currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_ON);
				return;
			}
			if(poly.getValue().isMarkerOnPolygon(marker)){
				return;
			}
		}
		Projection projection = map.getProjection();
		LatLng markerLocation = marker.getPosition();
		Point screenPosition = projection.toScreenLocation(markerLocation);
		//checking place of marker and trash icon 
		if (screenPosition.x <= (trash.getWidth() + trash.getX()) && screenPosition.x >= trash.getX() && screenPosition.y <= (trash.getY()+trash.getHeight()) && screenPosition.y >= trash.getY()){
			trash.setBackgroundResource(R.drawable.ic_bin);
			marker.setAlpha(0.5f);
		}else{
			trash.setBackgroundResource(R.drawable.ic_bin_empty);
			marker.setAlpha(1.0f);
		}
	}




	@Override
	public void onMarkerDragEnd(Marker marker) {
		Projection projection = map.getProjection();
		LatLng markerLocation = marker.getPosition();
		Point screenPosition = projection.toScreenLocation(markerLocation);

		boolean isPolygon = false;

		for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
			if(poly.getValue().isMarkerOnPolygon(marker)){
				MyPolygon p = poly.getValue();
				ArrayList<Marker> markers = p.getMarkers();
				String title = p.getTitle();
				Prefs.polygons.remove(p);
				MyPolygon p2 = new MyPolygon(map);
				for (Marker m : markers){
					p2.addPoint(m.getPosition());
				}
				if (p.isClosed()){
					p2.closePolygon();
				}
				p2.setTitle(title, this, getApplicationContext());
				p2.toggleEditMode(MyPolygon.EDIT_MODE_ON);
				p.clear();
				return;
			}

			if(poly.getValue().getTitleMarker().equals(marker)){
				isPolygon = true;
				if (screenPosition.x <= (trash.getWidth() + trash.getX()) && screenPosition.x >= trash.getX() && screenPosition.y 
						<= (trash.getY()+trash.getHeight()) && screenPosition.y >= trash.getY()){
					MyPolygon mp = poly.getValue();
					Prefs.polygons.remove(mp);
					mp.clear();
					showMessage(getString(R.string.delete_polygon_message), 0);
				}

				else{
					MyPolygon mp = poly.getValue();
					mp.getTitleMarker().remove();
					mp.setTitle(mp.getTitle(), this, getApplicationContext());
				}
			}
		}

		if (!isPolygon){

			String key = Prefs.markerToKey.get(marker);
			String index = key.split(":")[1];

			//checking place of marker and trash icon 
			if (screenPosition.x <= (trash.getWidth() + trash.getX()) && screenPosition.x >= trash.getX() && screenPosition.y 
					<= (trash.getY()+trash.getHeight()) && screenPosition.y >= trash.getY()){

				Prefs.myMarkers.remove(key).removeFromMap();
				if (true) {
					Prefs.markerToKey.remove(marker);
				} else {
					// tal 180902 mark deleted icin as deleted
					// Prefs.markerToKey.put(m, icon.getMacAddress() + ":" + 0xDE); // tal 180902 DE for deleted
				} // the motivation is to remember that icon:mac was deleted, so, a feedback report from the net would not re-alive it
				// another ISSUE: we must increase the icon-mac numerator even if some icon were deleted and the numbers/mac are again free to re-use

				send("D,1," + MY_MAC_ADDRESS + "," + index + ",\n");
				showMessage(getString(R.string.delete_marker_message), 0);
			}else{
				LatLng point = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
				LocationMarker lm = Prefs.myMarkers.get(key);
				lm.move(marker.getPosition());
				send("I,1," + MY_MAC_ADDRESS + "," + index + "," + lm.getType() + "," + lm.getLocation() + "," + lm.getTitle() + ",\n");
				showMessage(General.convertLocationToString(point), 0);
			}
		}
		trash_animation = AnimationUtils.loadAnimation(this, R.anim.trash_marker_out);
		trash.startAnimation(trash_animation);
		trash_animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {		}

			@Override
			public void onAnimationRepeat(Animation animation) {	}

			@Override
			public void onAnimationEnd(Animation animation) {
				trash.setAlpha(0.0f);
			}
		});
	}



	@Override
	public void onMarkerDragStart(Marker marker) {
		for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
			if(poly.getValue().isMarkerOnPolygon(marker)){
				return;
			}
		}
		for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
			if(poly.getValue().getTitleMarker().equals(marker)){
				if (currentEditablePolygon != null){
					currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
				}
				currentEditablePolygon = poly.getValue();
				currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_ON);
			}
		}
		trash = (ImageView)findViewById(R.id.trash_marker_icon);
		trash.setAlpha(1.0f);
		trash_animation = AnimationUtils.loadAnimation(this, R.anim.trash_marker_in);
		trash.startAnimation(trash_animation);
	}



	private void showMessage(String messageText, int messageFrom){
		if (distanceNotif != null){
			distanceNotif.cancel();
		}

		if (messageFrom == 2){
			distanceNotif = Crouton.makeText(this, ""+messageText, de.keyboardsurfer.android.widget.crouton.Style.ALERT);
			distanceNotif.show();
		}
		else if (messageFrom == 1){
			Configuration config = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
			distanceNotif = Crouton.makeText(this, ""+messageText, de.keyboardsurfer.android.widget.crouton.Style.CONFIRM).setConfiguration(config);
			distanceNotif.show();
		}else{
			distanceNotif = Crouton.makeText(this, ""+messageText, de.keyboardsurfer.android.widget.crouton.Style.CONFIRM);
			distanceNotif.show();
		}

		distanceNotif.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openStatus();
			}
		});
	}



	@Override
	public void onMapClick(LatLng point){
		String mes = getString(R.string.rule_message_distanse);		
		Location myLocation = fusedLocationService.getLocation();
		if (myLocation == null){
			service = false;
		}

		for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
			poly.getValue().toggleEditMode(MyPolygon.EDIT_MODE_OFF);
		}

		switch (drawState) {
		case Ally:
		case Enemy:
			drawMarkerOnMap(point);
			setState(DrawState.None);
			clearSelection();
			if (service){
				Location.distanceBetween(point.latitude, point.longitude, myLocation.getLatitude(), myLocation.getLongitude(), results);
				double azimut = General.getAzimut(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
				showMessage(General.convertLocationToString(point) + ", " + mes + " " + Math.round(results[0]) + 
						getString(R.string.unit_abbreviation) + ", " + getString(R.string.rule_message_angle) + Math.round(azimut) + ".", 0);
			}
			else{
				showMessage("No Location Service", 2);
			}
			break;
		case Line:
			drawRuler(point);
			break;
		case Polygon:
			drawPoligon(point);
			break;
		case None:
			if (service){
				Location.distanceBetween(point.latitude, point.longitude, myLocation.getLatitude(), myLocation.getLongitude(), results);
				double azimut = General.getAzimut(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
				showMessage(General.convertLocationToString(point) + ", " + mes + " " + Math.round(results[0]) + 
						getString(R.string.unit_abbreviation) + ", " + getString(R.string.rule_message_angle) + Math.round(azimut) +".", 0);
			}
			else{
				showMessage("No Location Service", 2);
			}
			break;
		default:
			break;
		}
	}



	private void showPolygonNameDialog(){
		polygonName = createNewListDialog();
		polygonName.show();
		final Activity activity = this;
		final MyPolygon p = polygon;
		button_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				polyName = edit_newPolygoneName.getText().toString();
				polygonName.cancel();
				if (polyName.equals("")){
					polyName = edit_newPolygoneName.getHint().toString();
				}
				p.setTitle(polyName, activity, getApplicationContext());
			}
		});
		polygon = null;

	}



	private void showMarkerNameDialog(final LatLng point, final boolean draggable, final int res, final String markerStr){
		markerName = createNewMarkerListDialog(markerStr);
		if (markerName == null){
			return;
		}

		markerName.show();

		edit_newMarkerName.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE){
					markersName = edit_newMarkerName.getText().toString();
					Log.d("aaaaaaaaaaaaaa",markersName);
					setMarker(markerStr, point, draggable, MY_MAC_ADDRESS);
				}

				return false;
			}
		});

		button_save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				markersName =edit_newMarkerName.getText().toString();
				setMarker(markerStr, point, draggable, MY_MAC_ADDRESS);

			}
		});

	}



	protected void setMarker(String markerStr, LatLng point, boolean draggable, String mac) {
		if(markersName.trim().equals("")){
			markersName = edit_newMarkerName.getHint().toString();
		}

		double lat = General.truncDouble(point.latitude, 5);
		double lon = General.truncDouble(point.longitude, 5);

		// tal 180407
		//lon -= 671.08863; // 0.006598750076293946d;

		String id = generateId();
		if (id == null){
			Toast.makeText(getApplicationContext(), "Exceeded maximum markers", Toast.LENGTH_SHORT).show();
			return;
		}

		String s = "I,1," + mac +"," + id + "," + Prefs.markersEnum.get(markerStr) 
				+ "," + General.precisionFormat(lat, lon) + "," + this.markersName + "\n";
		Icon icon = null;
		try {
			icon = new Icon(s);
		} catch (com.maatayim.acceleradio.utils.FormatException e) {
			e.printStackTrace();
		}

		Bitmap markerBitmap = getMarkersBitmap(icon);

		markerName.cancel();
		LocationMarker lm = new LocationMarker(point, Prefs.markersEnum.get(markerStr), map, markerBitmap, 
				markersName, markerIndex++, mac, id, icon.getAge());
		if (markerIndex > 99){
			markerIndex = 1;
		}
		Marker m = lm.placeOnMap();
		LocationMarker lm2 = Prefs.myMarkers.put(icon.getMacAddress() + ":" + icon.getIconNumber(), lm);
		if (lm2 != null){
			lm2.removeFromMap();
		}
		Prefs.markerToKey.put(m, icon.getMacAddress() + ":" + icon.getIconNumber());
		send(s);
		addMarkerLocationToLocationList(lm, true);

	}
	
	public String generateId(){
		for (int i = 0; i< 100; i++){
			String index = i < 10 ? "0"+i : ""+i;
			String key = "0000:" + index;
			if (!Prefs.myMarkers.containsKey(key)){ // tal 180902 this return the first free
				return index; // now we just need to mark a deleted id = D
			}
		}
		return null;
	}


	public LocationMarker popMarker(String mac){
		for (Entry<String, LocationMarker> m : Prefs.myMarkers.entrySet()){
			if (m.getValue().getMac().equals(mac)){
				Prefs.markerToKey.remove(m.getValue().getMarker());
				return Prefs.myMarkers.remove(m.getKey());
			}
		}
		return null;
	}


	public Bitmap getMarkersBitmap(Icon icon){
		View view = getLayoutInflater().inflate(R.layout.marker_and_text, new LinearLayout(getApplicationContext()),false);

		View markerIconAndText = view.findViewById(R.id.marker_layout);
		TextView markerTitle = (TextView) view.findViewById(R.id.marker_text);
		ImageView markerIcon = (ImageView) view.findViewById(R.id.marker);

		if (icon.getIconName().equals(Prefs.markersEnum.get("Enemy"))){
			markerIcon.setImageResource(R.drawable.ic_enemy);
			markerTitle.setTextColor(getResources().getColor(R.color.red));
		}
		else if(icon.getIconName().equals(Prefs.markersEnum.get("Ally"))){
			markerIcon.setImageResource(R.drawable.ic_ally);
			markerTitle.setTextColor(getResources().getColor(R.color.blue));
		}
		else{
			markerIcon.setImageResource(R.drawable.ic_loc_ally_small);
			markerTitle.setTextColor(getResources().getColor(R.color.black));
		}

		String markersName = icon.getText();
		markersName = markersName.length() > MARKER_NAME_LENGTH ? markersName.substring(0, MARKER_NAME_LENGTH - 3) + "..." : markersName;
		markerTitle.setText(markersName);

		Bitmap markerBitmap = getBitmapFromView(markerIconAndText);
		return markerBitmap;
	}



	public static Bitmap getBitmapFromView(View v){

		v.setDrawingCacheEnabled(true);

		// this is the important code :)  
		// Without it the view will have a dimension of 0,0 and the bitmap will be null          
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));


		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight()); 

		v.buildDrawingCache(true);

		Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), 
				v.getMeasuredHeight(), Bitmap.Config.ARGB_8888); 
		Canvas bitmapHolder = new Canvas(b); 
		v.draw(bitmapHolder);
		v.setDrawingCacheEnabled(false); // clear drawing cache

		return b;

	}





	private Dialog createNewListDialog(){
		Dialog d = new Dialog(this);
		String hintText = getString(R.string.hint_new_polygone_name);
		String okText = getString(R.string.ok_button_new_marker);
		String title = getString(R.string.title_new_polygone);
		edit_newPolygoneName = new EditText(this);
		edit_newPolygoneName.setHint(hintText);
		button_save = new Button(this);
		button_save.setText(okText);
		LinearLayout lin_m = new LinearLayout(this);
		lin_m.setOrientation(LinearLayout.VERTICAL);
		lin_m.addView(edit_newPolygoneName);
		lin_m.addView(button_save);
		d.setTitle(title);
		d.setCanceledOnTouchOutside(true);
		d.setContentView(lin_m);
		return d;
	}




	private Dialog createNewMarkerListDialog(String markerStr){
		if (markerStr == null){
			return null;
		}
		int counter;
		if (markerStr.equals("Enemy")){
			counter = enemyCounter++;
		}
		else{
			counter = allyCounter++;
		}
		Dialog d = new Dialog(this);
		String hintText = markerStr + " " + String.valueOf(counter);
		String okText = getString(R.string.ok_button_new_marker);
		String title = getString(R.string.title_new_marker);
		edit_newMarkerName = new EditText(this);
		edit_newMarkerName.setHint(hintText);

		button_save = new Button(this);
		button_save.setText(okText);
		LinearLayout lin_m = new LinearLayout(this);
		lin_m.setOrientation(LinearLayout.VERTICAL);
		lin_m.addView(edit_newMarkerName);
		lin_m.addView(button_save);
		d.setTitle(title);
		d.setCanceledOnTouchOutside(false);
		d.setContentView(lin_m);
		return d;
	}


	/************************************************************************************
	 ******************************* Chat and Status view *******************************
	 ************************************************************************************/
	private void initStatusView(){
		statusView = new SlidingMenu(this);
		statusView.setMode(SlidingMenu.RIGHT);
		statusView.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

		statusView.setFadeDegree(0.35f);
		statusView.attachToActivity(this, SlidingMenu.SLIDING_WINDOW );
		statusView.setMenu(R.layout.status_container);

	}


	private void initChatView() {
		chatView = new SlidingMenu(this);
		chatView.setMode(SlidingMenu.LEFT);
		chatView.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		chatView.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		chatView.setFadeDegree(0.35f);
		chatView.attachToActivity(this, SlidingMenu.SLIDING_WINDOW );
		chatView.setMenu(R.layout.chat_container);

	}

	private void openChat(){
		chatView.toggle();
		chatFragmentOpened = !chatFragmentOpened;
	}

	private void openStatus(){
		Intent i = new Intent(this, StatusActivity.class);
		startActivity(i);
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(chatView.isMenuShowing()){
				chatView.toggle();
				chatFragmentOpened = !chatFragmentOpened;
				return true;
			}
			if (statusView.isMenuShowing()){
				statusView.toggle();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}


}
