package com.maatayim.acceleradio.status;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.LogFile;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.R;
import com.maatayim.acceleradio.callsign.CallSignFragment;
import com.maatayim.acceleradio.mapshapes.LocationMarker;
import com.maatayim.acceleradio.mapshapes.MyPolygon;
import com.maatayim.acceleradio.usbserial.UsbService;
import com.maatayim.acceleradio.utils.FileUtils;
import com.maatayim.acceleradio.utils.MapUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.maatayim.acceleradio.General.getApkVersionName;
import static com.maatayim.acceleradio.Parameters.DELIMITER_TX;
import static com.maatayim.acceleradio.Parameters.ROOT_FOLDER;
import static com.maatayim.acceleradio.Parameters.SUB_DELIMITER;
import static com.maatayim.acceleradio.usbserial.UsbService.getMessageCounter;

public class SettingsFragment extends Fragment {

	private UsbService usbService;
	private String lastText="";
	private ServiceConnection usbConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			usbService = ((UsbService.UsbBinder) arg1).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			usbService = null;
		}
	};


	protected static final String EXPORTED_MAPS_DIRECTORY = ROOT_FOLDER + File.separator + "ExportedMaps" + File.separator;
	private File mapFile;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings_fragment, container, false);
		initStatusDataSettings(view);

		startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it

		return view;
	}

	private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
		if (UsbService.SERVICE_CONNECTED == false) {
			Intent startService = new Intent(getActivity(), service);
			if (extras != null && !extras.isEmpty()) {
				Set<String> keys = extras.keySet();
				for (String key : keys) {
					String extra = extras.getString(key);
					startService.putExtra(key, extra);
				}
			}
			getActivity().startService(startService);
		}
		Intent bindingIntent = new Intent(getActivity(), service);
		getActivity().bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private void initStatusDataSettings(View view) {
		Switch customMapMode = (Switch) view.findViewById(R.id.customMapMode);
		customMapMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					Prefs.SHOW_CUSTOM_MAP_MODE = true;
				}else{
					Prefs.SHOW_CUSTOM_MAP_MODE = false;
				}
			}
		});

		Button clearMap = (Button) view.findViewById(R.id.clearMap);
		clearMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MapUtils.clearMap(getContext());
			}


		});

		Button saveMap = (Button) view.findViewById(R.id.saveMap);
		saveMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault()).format(new Date());
				exportMap(EXPORTED_MAPS_DIRECTORY + "map_"+currentDateandTime + ".gil");
				Toast.makeText(getActivity(), "Map Saved!", Toast.LENGTH_SHORT).show();
			}

		});

		Button loadMap = (Button) view.findViewById(R.id.loadMap);
		loadMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FileChooserFragment fileFragment = new FileChooserFragment();
				fileFragment.show(getFragmentManager(), "fileChooser");
			}
		});
		Button terminalButton = (Button) view.findViewById(R.id.terminal_button);
        terminalButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

				final EditText edittext = new EditText(getContext());
				edittext.setText(lastText);

				alert.setMessage("Only admin can use this, May cause system bugs");
				alert.setTitle("System Terminal");

				alert.setView(edittext);

				alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						lastText = edittext.getText().toString();
						String text = edittext.getText().toString();
						text = General.addCheckSum(text) + SUB_DELIMITER + 99 + DELIMITER_TX;
						if (usbService != null) { // if UsbService was correctly binded, Send data
							usbService.write(text.getBytes(Charset.forName("UTF-8")));
							Map<String, String> m;
							m = new HashMap<String, String>();
							m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "TX: " + text.trim() + "\r\n");
							m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
							Prefs.getInstance().addStatusMessages(m);
							LogFragment.notifyChanges();
							LogFile.getInstance(getContext()).appendLog("TX: " + text);
						}
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// what ever you want to do with No option.
					}
				});

				alert.show();
			}
		});

		TextView versionNum = (TextView) view.findViewById(R.id.version_number);
		try {
			versionNum.setText(getApkVersionName(getContext()));
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("getApkVersionName","Version NameNotFoundException "+e.getMessage());
		}


//		try{
//			ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), 0);
//			ZipFile zf = new ZipFile(ai.sourceDir);
//			ZipEntry ze = zf.getEntry("classes.dex");
//			long time = ze.getTime();
//			String currentDateandTime = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(time);
//			zf.close();
//			versionNum.setText(currentDateandTime);
//		}catch(Exception e){
//		}

		Spinner spinner = view.findViewById(R.id.my_location_type_spinner);
		int myLocationType = Prefs.getSharedPreferencesInt(Prefs.USER_INFO,Prefs.MY_LOCATION_TYPE,getContext());
		spinner.setSelection(myLocationType);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Prefs.setSharedPreferencesInt(Prefs.USER_INFO,Prefs.MY_LOCATION_TYPE,position,getContext());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

	}



	public void exportMap(String fileName){
		try {
			createFileOnDevice(fileName,false);
			writeToFile("markers\n");
			for (Entry<String, LocationMarker> lm : Prefs.myMarkers.entrySet()){
				if (!lm.getValue().isL()){
					String s = lm.getValue().toString().substring(0,lm.getValue().toString().length()-1);
					writeToFile(s + lm.getValue().getAge() + ",\n");
				}
			}
			writeToFile("polygons\n");
			for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()){
				writeToFile(poly.getValue().toString());
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> m;
		m = new HashMap<String, String>();
		m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "UTIL: " + "Map exported");
		m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
		Prefs.getInstance().addStatusMessages(m);

		LogFragment.notifyChanges();
	}


	public static BufferedWriter out;
	private void createFileOnDevice(String filename, Boolean append) throws IOException {
		/*
		 * Function to initially create the log file and it also writes the time of creation to file.
		 */
		File Root = FileUtils.getRootDir();
		if(Root.canWrite()){
			new File(Root, EXPORTED_MAPS_DIRECTORY).mkdirs();
			mapFile = new File(Root, filename);
			FileWriter mapWriter = new FileWriter(mapFile, append);
			out = new BufferedWriter(mapWriter); 
		}
	}

	public void writeToFile(String message){
		try {
			out.write(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//update android files media scanner for the log files being visible without rebooting
		MediaScannerConnection.scanFile(getActivity(), new String[] {

			mapFile.getAbsolutePath()},

			null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri){
			}

		});
	}


}
