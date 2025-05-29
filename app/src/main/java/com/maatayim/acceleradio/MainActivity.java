package com.maatayim.acceleradio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.provider.DocumentsContract;
import android.text.TextUtils;
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
import com.maatayim.acceleradio.utils.FileUtils;
import com.maatayim.acceleradio.utils.MapUtils;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import static com.maatayim.acceleradio.Parameters.ACK;
import static com.maatayim.acceleradio.Parameters.DELIMITER_RX;
import static com.maatayim.acceleradio.Parameters.DELIMITER_TX;
import static com.maatayim.acceleradio.Parameters.ROOT_FOLDER;
import static com.maatayim.acceleradio.Parameters.SUB_DELIMITER;
import static com.maatayim.acceleradio.Parameters.TIME_OUT_MSEC;
import static com.maatayim.acceleradio.usbserial.UsbService.getMessageCounter;


public class MainActivity extends FragmentActivity
        implements OnMapClickListener, LocationListener, OnSmsSent,
        OnMarkerDragListener, OnMarkerClickListener {

    public static final String ICON_MAX_COUNT = "Max_Count";
    public static final String NO_MAC_ID = "no_mac";
    private static final long DELAY_MESSAGE_RESEND_MSEC = 5000;
    private static final long MSG_TIME_OUT = TimeUnit.MINUTES.toMillis(15);
    private static final long DELAY_FOR_TEST_CONNECTION = 5000;
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
    private static final String MAPS_DIRECTORY = ROOT_FOLDER + File.separator + "Maps" + File.separator;

    boolean isFirstLocationChange = true;
    protected String markersName;
    protected String polyName;
    private boolean polygonDrawing = false;

    private UsbService usbService;
    private MyHandler mHandler;
    private File mapsDirectory;
    public static Set<String> markersToLoad;
    public static Set<String> polygonsToLoad;
    private int currentMarkCount = 1;
    private TreeMap<Integer, Packet> messageBuffer = new TreeMap<>();
    private int messageLivesCounter;
    private final int MAX_LIVES = 3;
    private Timer resendMessageTimer;
    private TimerTask resendMessageTimerTask;
    private boolean isUsbConnected;
    private int counterofPoint=0;//maor changed 27042025

    double lat = 32.02124;


    private String testConnectionMessageCounter;
    public boolean isTestConnection = false;
    private ActivityResultLauncher<Intent> storageActivityResultLauncher;
    private ActivityResultLauncher<String[]> locationActivityResultLauncher;


    private enum DrawState {
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
        storageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the result here
                        Uri uri = null;
                        if (data != null) {
                            uri = data.getData();

                            final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);

                            String uriString = uri.toString();
                            Prefs.setSharedPreferencesString(Prefs.USER_INFO, Prefs.TOP_URI, uriString, MainActivity.this);
                            Log.d("TAG", "onCreate: path: " + uriString + " uri:" + Uri.parse(uriString));

                        }
                    }
                }
        );

        locationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        onLocationPermission();
                    }
                    launchUriPermissionWhenNeed();

                }
        );

        ActionBar bar = getActionBar();
        bar.hide();

        initButtons();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationActivityResultLauncher.launch(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE});

        } else {

            onLocationPermission();
            launchUriPermissionWhenNeed();

            //test
//			L,1,0048,06,00,+32.02142,+034.86126,00,4000,

//			final Handler handler = new Handler();
//			handler.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					try {
//
//                        String myLoc = "L,1,004C,14,01,+"+lat+",+034.86096,00,0040,!,";
//                        lat += 0.001;
//						com.maatayim.acceleradio.log.Location l = new com.maatayim.acceleradio.log.Location(myLoc);
//						l.handle(MainActivity.this,null);
//                        handler.postDelayed(this,1000);
//					} catch (FormatException e) {
//						e.printStackTrace();
//					}
//				}
//			}, 2000);

//			ArrayList<CallSign> callSigns1 = new ArrayList<>();
//			callSigns1.add(new CallSign("vv","0048"));
//			callSigns1.add(new CallSign("bb","004c"));
//			 CallSignFile.getInstance().writeToFile(callSigns1);
//			ArrayList<CallSign> a = CallSignFile.getInstance().readFromFile();
//			callSigns1.add(new CallSign("33","cc"));

//            Prefs.setSharedPreferencesString(Prefs.USER_INFO,Prefs.MY_MAC_ADDRESS, "vvvv",this);


            //test

        }
    }

    private void launchUriPermissionWhenNeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !FileUtils.checkUriPermission(this)) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            String documents = "content://com.android.externalstorage.documents/document/primary%3ADocuments";
            Uri initialUri = Uri.parse(documents);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
            storageActivityResultLauncher.launch(intent);
        }
    }


    private void onLocationPermission() {
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

    private void initMapViewLocation() {

        //Open last known location
        double lat = Prefs.getSharedPreferencesDouble(Prefs.USER_INFO, Prefs.LAST_LATITUDE, this);
        double lng = Prefs.getSharedPreferencesDouble(Prefs.USER_INFO, Prefs.LAST_LONGITUDE, this);

        // check its not null
        if ((lat + lng) != 0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), Parameters.ZOOM_LEVEL));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "start listening");
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
        checkForLoadedMap();

        resendMessageTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isUsbConnected) {
                    resendFifoMessage();
                    if (messageLivesCounter == MAX_LIVES) {
                        messageLivesCounter = 0;
                        if (messageBuffer != null && messageBuffer.size() > 0) {
                            messageBuffer.remove(messageBuffer.firstKey());
                        }
                    }
                    messageLivesCounter++;

                }
            }
        };
        startResendMessageTimer();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

        stopResendMessageTimer();

    }

    private void startResendMessageTimer() {
        resendMessageTimer = new Timer();
        resendMessageTimer.schedule(resendMessageTimerTask, DELAY_MESSAGE_RESEND_MSEC, DELAY_MESSAGE_RESEND_MSEC);
    }

    private void stopResendMessageTimer() {
        if (resendMessageTimerTask != null) {
            resendMessageTimerTask.cancel();
            resendMessageTimerTask = null;
        }
        if (resendMessageTimer != null) {
            resendMessageTimer.cancel();
            resendMessageTimer.purge();
            resendMessageTimer = null;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (fusedLocationService != null) {
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
        if (fusedLocationService != null) {
            fusedLocationService.stopListening();
        }
        Crouton.cancelAllCroutons();
        if (f != null)
            f.delete();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (drawState == DrawState.Polygon) {
            polygon.setUnclosedPolygon();
        }
        saveMap();
    }


    /////////////////////////////////////////////USB///////////////////////////////////////////

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (UsbService.SERVICE_CONNECTED == false) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mUsbReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mUsbReceiver, filter);

        }
    }


    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        private static String incomingData = "";
        private long receivedTime = -1;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    if (receivedTime < 0) {
                        receivedTime = System.currentTimeMillis();
                    }
                    String data = (String) msg.obj;
                    incomingData += data;
                    Log.d("Tal", incomingData);

                    if (incomingData.contains(DELIMITER_RX)) {
                        if (System.currentTimeMillis() - receivedTime > TIME_OUT_MSEC) { // if more then time out ignore previuse data
                            incomingData = data;
                        }
                        receivedTime = -1; // data recived
                        String[] buffer = incomingData.split(DELIMITER_RX);
                        incomingData = "";
                        if (buffer.length == 0) {
                            return;
                        }
                        String line;
                        for (int i = 0; i < buffer.length; i++) {
                            line = buffer[i];
                            if (!TextUtils.isEmpty(line)) {
                                String[] b = line.split(SUB_DELIMITER);
                                if (b.length > 0 && b[0].equals(Parameters.ACK)) {
                                    mActivity.get().onDataReceived(line);
                                    return;
                                }
                                if ((b.length > 1 && General.compareCheckSum(b[0], b[1])) || (b.length > 0 && b[0].contains(ACK))) {
                                    mActivity.get().onDataReceived(b[0]);
                                } else {
                                    Log.e("vova ", "checksum failed " + b[0]);
                                    Log.e("CHECKSUM ", "checksum failed" + b[0]);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }


    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String msg = "";
            int type = 0;
            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB PERMISSION GRANTED
            {
                msg = "Cable connected";
                type = 2;
                isUsbConnected = true;
                checkConnetion();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)) // USB PERMISSION NOT GRANTED
            {
                msg = "No permission granted";
            } else if (arg1.getAction().equals(UsbService.ACTION_NO_USB)) // NO USB CONNECTED
            {
                msg = "No USB connected";
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_DISCONNECTED)) // USB DISCONNECTED
            {
                Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
                msg = "Cable disconnected";
                isUsbConnected = false;
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_NOT_SUPPORTED)) // USB NOT SUPPORTED
            {
                Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
                msg = "USB device not supported";
            }

            showMessage(msg, type);
            LogFile.getInstance(MainActivity.this).appendLog("COM: " + msg);
            Map<String, String> m;
            m = new HashMap<String, String>();
            m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "COM: " + msg);
            m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
            Prefs.getInstance().addStatusMessages(m);
            LogFragment.notifyChanges();
        }
    };


    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    private File f;


    public void onDataReceived(String msg) {
        Log.d("Vova " + "USB", "Log: " + msg);

        Map<String, String> m;
        m = new HashMap<String, String>();
        m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "RX: " + msg);
        m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
        Prefs.getInstance().addStatusMessages(m);

        LogFile.getInstance(this).appendLog("RX: " + msg);
        String error = "";
        LogEntry le = null;
        try {
            le = LogEntryBuilder.build(msg); // pars message
        } catch (com.maatayim.acceleradio.utils.FormatException e) {
            e.printStackTrace();
            error = e.getMessage();
        }

        //error in message format
        if (le == null) {
            error = error.equals("") ? "Unknown message type" : error;
            m = new HashMap<String, String>();
            m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "Error: " + error + msg);
            m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
            Prefs.getInstance().addStatusMessages(m);
            LogFile.getInstance(this).appendLog(error);
            return;
        }
        le.handle(this, (ImageView) findViewById(R.id.chat_btn));
        LogFragment.notifyChanges();
    }

    public void testConnectionReciver(int num) {
        if (testConnectionMessageCounter != null) {
            int testMassageNum = Integer.parseInt(testConnectionMessageCounter);
            if (testMassageNum == num) {
                isTestConnection = true;
                showMessage("serial test successes", 2);
            }
        }
    }

    public void getAckUpdateMessageBuffer(int num) {
        messageBuffer.remove(num);
        resendFifoMessage();
    }

    private void resendFifoMessage() {
        if (messageBuffer.size() > 0) {
            Integer key = messageBuffer.firstKey();
            Packet msg = messageBuffer.get(key);
            if (System.currentTimeMillis() - msg.getTimeMS() > MSG_TIME_OUT) {
                messageBuffer.remove(key);
            } else {
                send(msg.getMsg());
            }
        }
    }


    public void checkConnetion() {
        String messageCounter = getMessageCounter();
        testConnectionMessageCounter = messageCounter;
        String msg = General.addCheckSum("serial test") + SUB_DELIMITER + messageCounter + DELIMITER_TX;
        messageBuffer.put(Integer.parseInt(messageCounter), new Packet(msg, System.currentTimeMillis()));
        resendFifoMessage();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            String message = "serial test failed";

            @Override
            public void run() {

                if (!isTestConnection) {

                    Map<String, String> m;
                    m = new HashMap<String, String>();
                    m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "COM: " + message);
                    m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
                    Prefs.getInstance().addStatusMessages(m);
                    LogFragment.notifyChanges();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage(message, 2);
                        }
                    });
                }

            }
        }, DELAY_FOR_TEST_CONNECTION);
    }

    public void prepareForSending(String msg) {
        msg = msg.replace("~", " ");
        if (!TextUtils.isEmpty(msg)) {
            String messageCounter = getMessageCounter();
            msg = General.addCheckSum(msg) + SUB_DELIMITER + messageCounter + DELIMITER_TX;
        final Handler handler = new Handler(Looper.getMainLooper());
        final String msgToSend = msg;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    messageBuffer.put(Integer.parseInt(messageCounter), new Packet(msgToSend, System.currentTimeMillis()));
                    resendFifoMessage();
                }
        }, Prefs.getSharedPreferencesLong(Prefs.USER_INFO,Prefs.DELAY,this)); // 5000 milliseconds = 5 seconds
    }
    }

    public void send(String msg) {
        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService.write(msg.getBytes(Charset.forName("UTF-8")));
            Map<String, String> m;
            m = new HashMap<String, String>();
            m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "TX: " + msg.trim() + "\r\n");
            m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
            Prefs.getInstance().addStatusMessages(m);
            LogFragment.notifyChanges();
            LogFile.getInstance(this).appendLog("TX: " + msg);
        }
    }


    public void sendAck(String num) {
        if (!TextUtils.isEmpty(num)) {
            if (usbService != null) { // if UsbService was correctly binded, Send data
                String msg = ACK + SUB_DELIMITER + num + DELIMITER_TX;
                usbService.write(msg.getBytes(Charset.forName("UTF-8")));
                Map<String, String> m;
                m = new HashMap<String, String>();
                m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "TX: " + msg.trim() + "\r\n");
                m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
                Prefs.getInstance().addStatusMessages(m);
                LogFragment.notifyChanges();
                LogFile.getInstance(this).appendLog("TX: " + msg);
            }
        }
    }


    @Override
    public void onSmsSent(String data) {
        String id = generateId();
        String myMac = Prefs.getPreference(Prefs.USER_INFO, Prefs.MY_MAC_ADDRESS, this);

        if (id.equals(ICON_MAX_COUNT)) {
            Toast.makeText(getApplicationContext(), "Exceeded maximum sms", Toast.LENGTH_SHORT).show();
            return;
        }
        if (id.equals(NO_MAC_ID)) {
            Toast.makeText(getApplicationContext(), "No Mac address wait for next round", Toast.LENGTH_SHORT).show();
            return;
        }
        String sms = "T,1," + myMac + "," + id + "," + data + "\n";
        Sms msg = null;
        try {
            msg = new Sms(sms);
        } catch (com.maatayim.acceleradio.utils.FormatException e) {
            e.printStackTrace();
        }
        if (msg != null) {
            msg.handle(this, (ImageView) findViewById(R.id.chat_btn));
            prepareForSending(sms);
        }
    }


    ////////////////////////////////////////////////init////////////////////////////////////////////////

    private void initButtons() {
        buttons = new SparseArray<ImageView>();
        buttons.append(R.id.enemy_btn, (ImageView) findViewById(R.id.enemy_btn));
        buttons.append(R.id.ally_btn, (ImageView) findViewById(R.id.ally_btn));
        buttons.append(R.id.ruller_btn, (ImageView) findViewById(R.id.ruller_btn));
        buttons.append(R.id.polygon_btn, (ImageView) findViewById(R.id.polygon_btn));
        buttons.append(R.id.map_mode, (ImageView) findViewById(R.id.map_mode));
        buttons.append(R.id.chat_btn, (ImageView) findViewById(R.id.chat_btn));
    }


    @SuppressLint("MissingPermission")
    private void mapSettings() {
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
        mapsDirectory = new File(FileUtils.getRootDir(), MAPS_DIRECTORY);
        mapsDirectory.mkdirs();

        f = new File(mapsDirectory, "a.a");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileWriter mapWriter = null;
        try {
            mapWriter = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter out = new BufferedWriter(mapWriter);
        try {
            out.append('a');
        } catch (IOException e) {
            e.printStackTrace();
        }
        //update android files media scanner for the log files being visible without rebooting
        MediaScannerConnection.scanFile(this, new String[]{

                        f.getAbsolutePath()},

                null, new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
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


    public void loadSavedMap() {
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
        if (markersToLoad != null) {
            drawMarkers(markersToLoad, true);
            int num = markersToLoad.size();
            allyCounter = num / 2;
            enemyCounter = (num + 1) / 2;
            markersToLoad = null;
        }
        if (polygonsToLoad != null) {
            drawPolygons(polygonsToLoad);
            polygonsToLoad = null;
        }

    }


    private void loadChat() {
        Set<String> chat = Prefs.getSharedPreferencesStringSet(Prefs.CHAT, Prefs.MESSAGES, getApplicationContext());
        if (chat == null) {
            return;
        }
        String[] msgs = null;
        for (String s : chat) {

            msgs = s.split("@");
        }


        Prefs.clearSms();
        for (String msg : msgs) {
            String[] message = msg.split(":");
            long date = Long.parseLong(message[0]);
            boolean isMine = message[1].equals("true");
            ChatMessage cm = new ChatMessage(message[2], message[3], isMine, date);
            Prefs.messages.add(cm);
        }
    }


    private void loadPolygons() {
        Set<String> polygons = Prefs.getSharedPreferencesStringSet(Prefs.SHAPES, Prefs.POLYGONS, getApplicationContext());

        drawPolygons(polygons);
    }


    private void drawPolygons(Set<String> polygons) {
        if (polygons == null) {
            return;
        }

        for (String s : polygons) {
            MyPolygon myPoly = new MyPolygon(map, s, this, getApplicationContext());
            Prefs.polygons.put(myPoly, myPoly);
        }
    }


    public void loadMarkers() {
        Set<String> markers = Prefs.getSharedPreferencesStringSet(Prefs.SHAPES, Prefs.MARKERS, getApplicationContext());
        if (markers == null) {
            return;
        }

        drawMarkers(markers, false);

        int num = Integer.parseInt(Prefs.getPreference(Prefs.MARKER_INDEX, Prefs.INDEX, getApplicationContext()));
        allyCounter = num / 2;
        enemyCounter = (num + 1) / 2;
    }


    private void drawMarkers(Set<String> markers, boolean isLoaded) {
        ArrayList<String> sortedMarkers = new ArrayList<String>(markers);
        Collections.sort(sortedMarkers);

        Prefs.getInstance().initStatusLocations();
        for (String s : sortedMarkers) {
            if (s.equals("")) {
                return;
            }
            boolean isL = false;
            String index = "";
            String age = "";
            String connectivity = "";
            if (s.contains("#L#")) {
                isL = true;
                age = s.substring(s.indexOf('$') + 1, s.lastIndexOf('~') - 1);
                index = s.substring(0, s.indexOf("#"));
                connectivity = s.substring(s.indexOf('~') + 1, s.lastIndexOf('#'));
            } else {
                index = s.substring(0, s.indexOf("I"));
            }
            s = s.substring(s.indexOf("I"));
            Icon icon = null;
            try {
                icon = new Icon(s);
            } catch (com.maatayim.acceleradio.utils.FormatException e) {
                e.printStackTrace();
            }
            if (isL) {
                icon.setLIcon(age);
            }

            Bitmap markerBitmap = getMarkersBitmap(icon);

            LocationMarker lm = new LocationMarker(icon.getLatlng(), icon.getIconName(), map, markerBitmap, icon.getText()
                    , Integer.parseInt(index), icon.getMacAddress(), icon.getIconNumber(), icon.getAge());


            if (isL) {
                lm.setLMarker(connectivity);
            }

            Marker m = lm.placeOnMap();
            Prefs.myMarkers.put(icon.getMacAddress() + ":" + icon.getIconNumber(), lm);
            Prefs.markerToKey.put(m, icon.getMacAddress() + ":" + icon.getIconNumber());

            if (!isLoaded) {
                addMarkerLocationToLocationList(lm, !isL);
            }
        }

    }


    public void saveMap() {
        saveMarkers();
        savePolygons();
        saveChat();
    }


    private void saveChat() {
        Set<String> chat = new LinkedHashSet<String>();
        String chatMsgs = "";
        for (ChatMessage c : Prefs.messages) {
            chatMsgs += c.toString() + "@";
        }
        if (!chatMsgs.isEmpty()) {
            chatMsgs.substring(0, chatMsgs.length() - 1);
            chat.add(chatMsgs);
            Prefs.setSharedPreferencesStringSet(Prefs.CHAT, Prefs.MESSAGES, chat, getApplicationContext());
        }
    }


    private void savePolygons() {
        Set<String> markers = new LinkedHashSet<String>();
        for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()) {
            markers.add(poly.getValue().toString());
        }
        if (polygonDrawing) {
            markers.add(polygon.toString());
        }
        Prefs.setSharedPreferencesStringSet(Prefs.SHAPES, Prefs.POLYGONS, markers, getApplicationContext());

    }


    public void saveMarkers() {
        Set<String> markers = new LinkedHashSet<String>();
        for (Entry<String, LocationMarker> lm : Prefs.myMarkers.entrySet()) {
            markers.add(lm.getValue().toString());
        }
        Prefs.setSharedPreferencesStringSet(Prefs.SHAPES, Prefs.MARKERS, markers, getApplicationContext());

        Prefs.setPreference(Prefs.MARKER_INDEX, Prefs.INDEX, "" + (allyCounter + enemyCounter), getApplicationContext());
    }


    ////////////////////////////////////////////////////////map////////////////////////////////////////////////////


    @Override
    public boolean onMarkerClick(Marker marker)
   //this gunction called when  polygon is closed or  marker is clicked
    {
        counterofPoint=0;//maor k changed 290525-intialized points ruler polygon
         Log.d("Marker Clicked", "Marker: " + marker.getTitle() + ", Position: " + marker.getPosition());
        if (currentEditablePolygon != null) {
            currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
        }
        if (polygon != null && (marker.equals(polygon.getFirstMarker()) || marker.equals(polygonStartOnMarker))) {
            polygon.closePolygon();
            polygonStartOnMarker = null;
            showPolygonNameDialog();
            setState(DrawState.None);
            clearSelection();
            return true;
        }

        if (drawState == DrawState.Line && ruler != null) {
            drawRuler(marker.getPosition());
            return true;
        }

        if (drawState == DrawState.Polygon && polygon != null) {
            if (polygon.getPoints().isEmpty()) {
                polygonStartOnMarker = marker;
            }
            polygon.addPoint(marker.getPosition());
        }

        if (ruler != null && ruler.getFirstMarker() != null && ruler.getFirstMarker().equals(marker)) {
            drawRuler(marker.getPosition());
            return true;
        }
        String mes = getString(R.string.rule_message_distanse);
        Location myLocation = fusedLocationService.getLocation();
        LatLng point = marker.getPosition();
        if (myLocation != null) {
            Location.distanceBetween(point.latitude, point.longitude, myLocation.getLatitude(), myLocation.getLongitude(), results);
            double azimut = General.getAzimut(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
            showMessage(General.convertLocationToString(point) + ", " + mes + " " + Math.round(results[0]) +
                    getString(R.string.unit_abbreviation) + ", " + getString(R.string.rule_message_angle) + Math.round(azimut) + ".", 0);
        } else {
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
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }


    private void clearSelection() {
        int k = 0;
        for (int i = 0; i < buttons.size(); i++) {
            k = buttons.keyAt(i);
            buttons.get(k).setSelected(false);
        }
        polygonDrawing = false;
    }


    //	@Override
    public void onLocationChanged(Location location) {
        if (isFirstLocationChange) {
            isFirstLocationChange = false;
            initMapPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }


    public void initMapPosition(LatLng mapCenter) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(false);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(false);
        MyLocationMarker.setDeviceMarker(map, mapCenter);

        int myLocationType = Prefs.getSharedPreferencesInt(Prefs.USER_INFO, Prefs.MY_LOCATION_TYPE, this);
        MapUtils.addMyCurrentLocation(mapCenter, this);
        double nowTime = System.currentTimeMillis();
        switch (myLocationType) {
            case MyLocationMarker.C4NET_LOCATION:
                double lastUpdateDeviceLocation = Prefs.getSharedPreferencesDouble(Prefs.USER_INFO, Prefs.LOCATION_TIME, this);
                if ((nowTime - lastUpdateDeviceLocation) > TimeUnit.MINUTES.toMillis(1)) {
                    activeDeviceLocation();
                } else {

                    activeC4netLocation();
                }
                break;

            case MyLocationMarker.AVRAGE_LOCATION:
                activeAvrageLocation();

                break;
            case MyLocationMarker.DEVICE_LOCATION:
                Prefs.setSharedPreferencesDouble(Prefs.USER_INFO, Prefs.LOCATION_TIME, nowTime, this);
                activeDeviceLocation();
                break;
        }
//      L,1,0034,00,01,+32.02124,+034.86126,00,4000,!,
        String myMac = Prefs.getPreference(Prefs.USER_INFO, Prefs.MY_MAC_ADDRESS, this);
        String msg = "L,1," + myMac + ",00,01,+" + mapCenter.latitude + ",+" + mapCenter.longitude + ",00,zzzz,!,";
        LogFile.getInstance(this).appendLog("RX: " + msg);

    }

    private void activeAvrageLocation() {
        MyLocationMarker.setC4netMarkerVisible(false);
        MyLocationMarker.setDeviceMarkerVisible(false);

        MyLocationMarker.setAvgMarker(map);
        MyLocationMarker.setAvgMarkerVisible(true);
    }

    private void activeC4netLocation() {
        MyLocationMarker.setDeviceMarkerVisible(false);
        MyLocationMarker.setAvgMarkerVisible(false);

        MyLocationMarker.setC4netMarkerVisible(true);
    }

    private void activeDeviceLocation() {
        MyLocationMarker.setC4netMarkerVisible(false);
        MyLocationMarker.setAvgMarkerVisible(false);

        MyLocationMarker.setDeviceMarkerVisible(true);
    }


    private void switchMaps() {

        currentMapType++;

        if (tileOverlay != null) {
            tileOverlay.remove();
        }
        if (Prefs.SHOW_CUSTOM_MAP_MODE && currentMapType >= Parameters.SUPPORTED_MAPS.length) {
            ArrayList<MapFolder> maps = new ArrayList<MapFolder>();
            loadMapFolders(maps);
            if (maps.isEmpty()) {
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

            if (currentMapType == Parameters.SUPPORTED_MAPS.length + maps.size() - 1) {
                currentMapType = -1;
            }
            return;
        }

        if (currentMapType == Parameters.SUPPORTED_MAPS.length || currentMapType == -1) {
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

        if (folders == null) {
            return;
        }
        for (String s : folders) {
            String extension = MAP_EXTENSION;
            maps.add(new MapFolder(s, extension));
        }
    }


    private void drawRuler(LatLng point) {
        Double dist = ruler.addPoint(point);
        if (dist == null) {
            showMessage("No Location Service", 2);
            return;
        }
        int distance = (int) dist.doubleValue();

        String distanseText = getString(R.string.rule_message_distanse);
        String totalDistanseText = getString(R.string.rule_message_totalDistanse);
        String angleText = getString(R.string.rule_message_angle);

        int totalDistance = (int) ruler.getTotalDistance();
        String finalMessage;

        if (totalDistance == 0) {
            finalMessage = General.convertLocationToString(point) + ", " +
                    distanseText + " " + distance + getString(R.string.unit_abbreviation) + ", " +
                    angleText + " " + Math.round(ruler.getAzimut()) + ".";
        } else {
            finalMessage = General.convertLocationToString(point) + ", " +
                    distanseText + " " + distance + getString(R.string.unit_abbreviation) + ", " +
                    totalDistanseText + " " + totalDistance + getString(R.string.unit_abbreviation) + ", " +
                    angleText + " " + Math.round(ruler.getAzimut()) + ".";
        }

        showMessage(finalMessage, 1);

    }


    private void drawPoligon(LatLng point) {
        polygon.addPoint(point);
        // Log.d("drawPoligon", " point added 2");
        polygonDrawing = true;
        counterofPoint++;
         Log.d("drawPoligon 4", " counterofPoint " + counterofPoint);
        if (counterofPoint > 10) {//maor k changed 270425
            showPolygonNameDialog();//show dialog/pop-up window( Area name) after 10 points
            counterofPoint = 0;
        }
    }


    private void setState(DrawState state) {
        if (drawState == state) {
            drawState = DrawState.None;
            return;
        }
        drawState = state;
    }


    //button from the menu
    public void menuButtonAction(View view) {
        boolean isSelected = view.isSelected();
        clearSelection();
        if (currentEditablePolygon != null) {
            currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
        }
        if (polygon != null && !polygon.getPoints().isEmpty() && drawState == DrawState.Polygon) {
            polygon.removeMarkers();
            polygon.setUnclosedPolygon();
            polygonStartOnMarker = null;
            showPolygonNameDialog();
        }

        if (ruler != null) {
            ruler.clear();
        }

        if (distanceNotif != null) {
            distanceNotif.cancel();
        }

        view.setSelected(!isSelected);

        switch (view.getId()) {
            case R.id.map_mode:
                switchMaps();
                break;
            case R.id.enemy_btn:
                setState(DrawState.Enemy);
                break;
            case R.id.ally_btn:
                setState(DrawState.Ally);
                break;
            case R.id.polygon_btn:
                polygon = new MyPolygon(map);
                setState(DrawState.Polygon);
                break;
            case R.id.ruller_btn:
                ruler = new Ruler(map, fusedLocationService);
                setState(DrawState.Line);
                break;
            case R.id.chat_container:
                openChat();
                ((ImageView) findViewById(R.id.chat_btn)).clearAnimation();
                ((ImageView) findViewById(R.id.chat_btn)).setVisibility(View.INVISIBLE);
                break;
            case R.id.chat_btn:
                openChat();
                ((ImageView) findViewById(R.id.chat_btn)).clearAnimation();
                ((ImageView) findViewById(R.id.chat_btn)).setVisibility(View.INVISIBLE);
                break;
            case R.id.locate_me:
                initMapViewLocation();

                break;

        }
    }


    public void addMarkerLocationToLocationList(LocationMarker lm, boolean icon) {
        Map<String, String> m;
        m = new HashMap<String, String>();
        String connectivity = icon ? "-" : lm.getConnectivity();
        m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "I,1," + lm.getMac() + "," + lm.getIconCounter() + "," + lm.getType()
                + "," + lm.getLocation() + "," + lm.getAge() + "," + connectivity + ",\n");
        m.put(Prefs.INDEX, lm.getIconCounter());
        m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
        m.put(Prefs.ATTRIBUTE_MARKER_NAME, lm.getTitle());
        m.put(Prefs.ATTRIBUTE_AGE, General.getAge(lm.getAge()));

        if (icon) {
            Prefs.getInstance().addStatusLocations(m);
            //MyLocationsFragment.notifyChanges();
        } else {
            Prefs.addTheirStatusLocations(m, getApplicationContext());
            //TheirLocationsFragment.notifyChanges();
        }
    }


    private void drawMarkerOnMap(LatLng point) {
        int res = 0;
        String marker = null;
        ruleButtonPressed = false;
        polygoneButtonPressed = false;
        boolean draggable = true;
        switch (drawState) {
            case Enemy:
                res = R.drawable.ic_enemy;
                marker = "Enemy";
                showMarkerNameDialog(point, draggable, res, marker);
                break;
            case Ally:
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
        for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()) {
            if (poly.getValue().getTitleMarker().equals(marker)) {
                if (currentEditablePolygon != null) {
                    currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
                }
                currentEditablePolygon = poly.getValue();
                currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_ON);
                return;
            }
            if (poly.getValue().isMarkerOnPolygon(marker)) {
                return;
            }
        }
        Projection projection = map.getProjection();
        LatLng markerLocation = marker.getPosition();
        Point screenPosition = projection.toScreenLocation(markerLocation);
        //checking place of marker and trash icon
        if (screenPosition.x <= (trash.getWidth() + trash.getX()) && screenPosition.x >= trash.getX() && screenPosition.y <= (trash.getY() + trash.getHeight()) && screenPosition.y >= trash.getY()) {
            trash.setBackgroundResource(R.drawable.ic_bin);
            marker.setAlpha(0.5f);
        } else {
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

                for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()) {
                    if (poly.getValue().isMarkerOnPolygon(marker)) {
                        MyPolygon p = poly.getValue();
                        ArrayList<Marker> markers = p.getMarkers();
                        String title = p.getTitle();
                        Prefs.polygons.remove(p);
                        MyPolygon p2 = new MyPolygon(map);
                        for (Marker m : markers) {
                            p2.addPoint(m.getPosition());
                        }
                        if (p.isClosed()) {
                            p2.closePolygon();
                        }
                        p2.setTitle(title, MainActivity.this, getApplicationContext());
                        p2.toggleEditMode(MyPolygon.EDIT_MODE_ON);
                        p.clear();
                        return;
                    }

                    if (poly.getValue().getTitleMarker().equals(marker)) {
                        isPolygon = true;
                        if (screenPosition.x <= (trash.getWidth() + trash.getX()) && screenPosition.x >= trash.getX() && screenPosition.y
                                <= (trash.getY() + trash.getHeight()) && screenPosition.y >= trash.getY()) {
                            MyPolygon mp = poly.getValue();
                            Prefs.polygons.remove(mp);
                            mp.clear();
                            showMessage(getString(R.string.delete_polygon_message), 0);
                        } else {
                            MyPolygon mp = poly.getValue();
                            mp.getTitleMarker().remove();
                            mp.setTitle(mp.getTitle(), MainActivity.this, getApplicationContext());
                        }
                    }
                }

                if (!isPolygon) {

                    String myMac = Prefs.getPreference(Prefs.USER_INFO, Prefs.MY_MAC_ADDRESS, MainActivity.this);

                    if (TextUtils.isEmpty(myMac)) {
                        Toast.makeText(getApplicationContext(), "No Mac address wait for next round", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String key = Prefs.markerToKey.get(marker);
                    String index = key.split(":")[1];

                    //checking place of marker and trash icon
                    if (screenPosition.x <= (trash.getWidth() + trash.getX()) && screenPosition.x >= trash.getX() && screenPosition.y
                            <= (trash.getY() + trash.getHeight()) && screenPosition.y >= trash.getY()) {

                        Prefs.myMarkers.remove(key).removeFromMap();
                        String[] buffer = key.split(":");
                        if (buffer.length > 1) {
                            String iconCounter = buffer[1];
                            Prefs.getInstance().removeStatusLocation(iconCounter);
                        }
                        if (true) {
                            Prefs.markerToKey.remove(marker);
                        } else {
                            // tal 180902 mark deleted icin as deleted
                            // Prefs.markerToKey.put(m, icon.getMacAddress() + ":" + 0xDE); // tal 180902 DE for deleted
                        } // the motivation is to remember that icon:mac was deleted, so, a feedback report from the net would not re-alive it
                        // another ISSUE: we must increase the icon-mac numerator even if some icon were deleted and the numbers/mac are again free to re-use

                        prepareForSending("D,1," + myMac + "," + index + ",\n");
                        showMessage(getString(R.string.delete_marker_message), 0);
                    } else {
                        LatLng point = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                        LocationMarker lm = Prefs.myMarkers.get(key);
                        lm.move(marker.getPosition());
                        prepareForSending("I,1," + myMac + "," + index + "," + lm.getType() + "," + lm.getLocation() + "," + lm.getTitle() + ",\n");
                        showMessage(General.convertLocationToString(point), 0);
                    }
                }
                trash_animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.trash_marker_out);
                trash.startAnimation(trash_animation);
                trash_animation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

            @Override
            public void onAnimationEnd(Animation animation) {
                trash.setAlpha(0.0f);
            }
        });
    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()) {
            if (poly.getValue().isMarkerOnPolygon(marker)) {
                return;
            }
        }
        for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()) {
            if (poly.getValue().getTitleMarker().equals(marker)) {
                if (currentEditablePolygon != null) {
                    currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_OFF);
                }
                currentEditablePolygon = poly.getValue();
                currentEditablePolygon.toggleEditMode(MyPolygon.EDIT_MODE_ON);
            }
        }
        trash = (ImageView) findViewById(R.id.trash_marker_icon);
        trash.setAlpha(1.0f);
        trash_animation = AnimationUtils.loadAnimation(this, R.anim.trash_marker_in);
        trash.startAnimation(trash_animation);
    }


    private void showMessage(String messageText, int messageFrom) {
        if (distanceNotif != null) {
            distanceNotif.cancel();
        }

        if (messageFrom == 2) {
            distanceNotif = Crouton.makeText(this, "" + messageText, de.keyboardsurfer.android.widget.crouton.Style.ALERT);
            distanceNotif.show();
        } else if (messageFrom == 1) {
            Configuration config = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
            distanceNotif = Crouton.makeText(this, "" + messageText, de.keyboardsurfer.android.widget.crouton.Style.CONFIRM).setConfiguration(config);
            distanceNotif.show();
        } else {
            distanceNotif = Crouton.makeText(this, "" + messageText, de.keyboardsurfer.android.widget.crouton.Style.CONFIRM);
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
    public void onMapClick(LatLng point) {
        String mes = getString(R.string.rule_message_distanse);
        Location myLocation = fusedLocationService.getLocation();
        if (myLocation == null) {
            service = false;
        }

        for (Entry<MyPolygon, MyPolygon> poly : Prefs.polygons.entrySet()) {
            poly.getValue().toggleEditMode(MyPolygon.EDIT_MODE_OFF);
        }

        switch (drawState) {
            case Ally:
            case Enemy:
                drawMarkerOnMap(point);
                setState(DrawState.None);
                clearSelection();
                if (service) {
                    Location.distanceBetween(point.latitude, point.longitude, myLocation.getLatitude(), myLocation.getLongitude(), results);
                    double azimut = General.getAzimut(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
                    showMessage(General.convertLocationToString(point) + ", " + mes + " " + Math.round(results[0]) +
                            getString(R.string.unit_abbreviation) + ", " + getString(R.string.rule_message_angle) + Math.round(azimut) + ".", 0);
                } else {
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
                if (service) {
                    Location.distanceBetween(point.latitude, point.longitude, myLocation.getLatitude(), myLocation.getLongitude(), results);
                    double azimut = General.getAzimut(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), point);
                    showMessage(General.convertLocationToString(point) + ", " + mes + " " + Math.round(results[0]) +
                            getString(R.string.unit_abbreviation) + ", " + getString(R.string.rule_message_angle) + Math.round(azimut) + ".", 0);
                } else {
                    showMessage("No Location Service", 2);
                }
                break;
            default:
                break;
        }
    }


    private void showPolygonNameDialog() {

        polygonName = createNewListDialog();
        polygonName.show();
        final Activity activity = this;
        final MyPolygon p = polygon;
        button_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                polyName = edit_newPolygoneName.getText().toString();
                polygonName.cancel();
                if (polyName.equals("")) {
                    polyName = edit_newPolygoneName.getHint().toString();
                }
                p.setTitle(polyName, activity, getApplicationContext());
            }
        });
        polygon = null;

    }


    private void showMarkerNameDialog(final LatLng point, final boolean draggable, final int res, final String markerStr) {
        markerName = createNewMarkerListDialog(markerStr);
        if (markerName == null) {
            return;
        }

        markerName.show();

        edit_newMarkerName.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    markersName = edit_newMarkerName.getText().toString();
                    Log.d("aaaaaaaaaaaaaa", markersName);
                    setMarker(markerStr, point, draggable);
                }

                return false;
            }
        });

        button_save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                markersName = edit_newMarkerName.getText().toString();
                setMarker(markerStr, point, draggable);

            }
        });

    }


    protected void setMarker(String markerStr, LatLng point, boolean draggable) {
        if (markersName.trim().equals("")) {
            markersName = edit_newMarkerName.getHint().toString();
        }
        markersName += ",";

        double lat = General.truncDouble(point.latitude, 5);
        double lng = General.truncDouble(point.longitude, 5);

        // tal 180407
        //lon -= 671.08863; // 0.006598750076293946d;

        String id = generateId();
        if (id.equals(ICON_MAX_COUNT)) {
            Toast.makeText(getApplicationContext(), "Exceeded maximum markers", Toast.LENGTH_SHORT).show();
            return;
        }
        if (id.equals(NO_MAC_ID)) {
            Toast.makeText(getApplicationContext(), "No Mac address wait for next round", Toast.LENGTH_SHORT).show();
            return;
        }

        String myMac = Prefs.getPreference(Prefs.USER_INFO, Prefs.MY_MAC_ADDRESS, this);

        String s = "I,1," + myMac + "," + id + "," + Prefs.markersEnum.get(markerStr)
                + "," + General.precisionFormat(lat, lng) + "," + this.markersName + "\n";
        Icon icon = null;
        try {
            icon = new Icon(s);
        } catch (com.maatayim.acceleradio.utils.FormatException e) {
            e.printStackTrace();
        }

        Bitmap markerBitmap = getMarkersBitmap(icon);

        markerName.cancel();
        LocationMarker lm = new LocationMarker(point, Prefs.markersEnum.get(markerStr), map, markerBitmap,
                markersName, 0, myMac, id, icon.getAge());

        Marker m = lm.placeOnMap();
        LocationMarker lm2 = Prefs.myMarkers.put(icon.getMacAddress() + ":" + icon.getIconNumber(), lm);
        if (lm2 != null) {
            lm2.removeFromMap();
        }
        Prefs.markerToKey.put(m, icon.getMacAddress() + ":" + icon.getIconNumber());
        prepareForSending(s);
        addMarkerLocationToLocationList(lm, true);

    }

    public String generateId() {
        for (int i = currentMarkCount; i < 240; i++) {
            currentMarkCount = currentMarkCount < 1 ? 1 : currentMarkCount;
            String index = General.getStringFromHex(i);
            String myMac = Prefs.getPreference(Prefs.USER_INFO, Prefs.MY_MAC_ADDRESS, this);
            if (TextUtils.isEmpty(myMac)) {
                return NO_MAC_ID;
            }

            String key = myMac + ":" + index;
            if (!Prefs.myMarkers.containsKey(key)) {
                currentMarkCount = i + 1;
                return index;
            }
        }
        return ICON_MAX_COUNT;
    }


    public LocationMarker popMarker(String mac) {
        for (Entry<String, LocationMarker> m : Prefs.myMarkers.entrySet()) {
            if (m.getValue().getMac().equals(mac)) {
                Prefs.markerToKey.remove(m.getValue().getMarker());
                return Prefs.myMarkers.remove(m.getKey());
            }
        }
        return null;
    }


    public Bitmap getMarkersBitmap(Icon icon) {
        View view = getLayoutInflater().inflate(R.layout.marker_and_text, new LinearLayout(getApplicationContext()), false);

        View markerIconAndText = view.findViewById(R.id.marker_layout);
        TextView markerTitle = (TextView) view.findViewById(R.id.marker_text);
        ImageView markerIcon = (ImageView) view.findViewById(R.id.marker);

        if (icon.getIconName().equals(Prefs.markersEnum.get("Enemy"))) {
            markerIcon.setImageResource(R.drawable.ic_enemy);
            markerTitle.setTextColor(getResources().getColor(R.color.red));
        } else if (icon.getIconName().equals(Prefs.markersEnum.get("Ally"))) {
            markerIcon.setImageResource(R.drawable.ic_ally);
            markerTitle.setTextColor(getResources().getColor(R.color.blue));
        } else {
            markerIcon.setImageResource(R.drawable.ic_loc_ally_small);
            markerTitle.setTextColor(getResources().getColor(R.color.black));
        }

        String markersName = icon.getText();
        markersName = markersName.length() > MARKER_NAME_LENGTH ? markersName.substring(0, MARKER_NAME_LENGTH - 3) + "..." : markersName;
        markerTitle.setText(markersName);

        Bitmap markerBitmap = getBitmapFromView(markerIconAndText);
        return markerBitmap;
    }


    public static Bitmap getBitmapFromView(View v) {

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


    private Dialog createNewListDialog() {
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


    private Dialog createNewMarkerListDialog(String markerStr) {
        if (markerStr == null) {
            return null;
        }
        int counter;
        if (markerStr.equals("Enemy")) {
            counter = enemyCounter++;
        } else {
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
    private void initStatusView() {
        statusView = new SlidingMenu(this);
        statusView.setMode(SlidingMenu.RIGHT);
        statusView.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        statusView.setFadeDegree(0.35f);
        statusView.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        statusView.setMenu(R.layout.status_container);

    }


    private void initChatView() {
        chatView = new SlidingMenu(this);
        chatView.setMode(SlidingMenu.LEFT);
        chatView.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        chatView.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        chatView.setFadeDegree(0.35f);
        chatView.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        chatView.setMenu(R.layout.chat_container);

    }

    private void openChat() {
        chatView.toggle();
        chatFragmentOpened = !chatFragmentOpened;
    }

    private void openStatus() {
        Intent i = new Intent(this, StatusActivity.class);
        startActivity(i);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (chatView.isMenuShowing()) {
                chatView.toggle();
                chatFragmentOpened = !chatFragmentOpened;
                return true;
            }
            if (statusView.isMenuShowing()) {
                statusView.toggle();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setCurrentMarkCount(int currentMarkCount) {
        this.currentMarkCount = currentMarkCount;
    }
}
