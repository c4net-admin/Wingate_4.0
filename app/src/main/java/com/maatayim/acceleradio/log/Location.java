package com.maatayim.acceleradio.log;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.maatayim.acceleradio.General;
import com.maatayim.acceleradio.LogFile;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.callsign.CallSign;
import com.maatayim.acceleradio.mapshapes.MyLocationMarker;
import com.maatayim.acceleradio.utils.FormatException;
import com.maatayim.acceleradio.utils.MapUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.maatayim.acceleradio.Parameters.CHECKSUM_PEDDING;
import static com.maatayim.acceleradio.Parameters.DELIMITER_RX;

public class Location extends LogEntry {

    private static final String LOCATION = "L";
    private String macAddress;
    private String slotIndex;
    private String icon; //refer to icon enumeration table
    private LatLng latlng;
    private String age;
    private String connectivity;
    private boolean me; //optional


    public Location(String str) throws FormatException {
        super(str);
        parseStr();
    }

    private void parseStr() throws FormatException {
        while (entry.endsWith(DELIMITER_RX) || entry.endsWith(String.valueOf(CHECKSUM_PEDDING))) {
            entry = entry.substring(0, entry.length() - 1);
        }
        String[] data = entry.split(",");
        if (data.length < 9 || data.length > 10) {
            throw new FormatException("wrong message size");
        }
        macAddress = data[2]; // if [2]+2 was 0x00352 means that it is a text, and + is concat
        slotIndex = data[3]; // [3]+10 was not obeyed MAC 0035:07
        icon = data[4];
        try {
            double lat = Double.parseDouble(data[5].replace("+", ""));
            double lng = Double.parseDouble(data[6].replace("+", ""));
            // tal 180405 added grennwich bug skew
            //	if (true) { //lng > 180 || lng < 0) {
            //lng -= 670d; // 0.006598750076293946d; // lng += -671.08863;
            //	} else { // check fake 0x0035 icon
            //lng += -0.1;
            //	}

            this.latlng = new LatLng(lat, lng); // eval(lng - 671.08863) );
            this.age = data[7];
            this.connectivity = data[8];
            if (data.length == 10) {
                this.me = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new FormatException("invalid lat/long format");
        }

        //me = data.length == 10;
    }


    public static String getLocation() {
        return LOCATION;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getSlotIndex() {
        return slotIndex;
    }

    public String getIcon() {
        return icon;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public String getAge() {
        return age;
    }

    public String getConnectivity() {
        return connectivity;
    }

    public boolean isMe() {
        return me;
    }

    @Override
    public void handle(Activity mainActivity, ImageView button) {
        String myMac = Prefs.getPreference(Prefs.USER_INFO, Prefs.MY_MAC_ADDRESS, mainActivity);

        if (me) {

            MyLocationMarker.setC4NetMarker(((MainActivity) mainActivity).map, latlng);
            MyLocationMarker.setMylocationTitle(getNameFromCallSign(macAddress));
            MapUtils.addMyCurrentLocation(latlng, mainActivity);

            int myLocationType = Prefs.getSharedPreferencesInt(Prefs.USER_INFO, Prefs.MY_LOCATION_TYPE, mainActivity);
            double nowTime = System.currentTimeMillis();
            switch (myLocationType) {
                case MyLocationMarker.C4NET_LOCATION:
                    Prefs.setSharedPreferencesDouble(Prefs.USER_INFO, Prefs.LOCATION_TIME, nowTime, mainActivity);
                    activeC4netLocation();
                    break;

                case MyLocationMarker.AVRAGE_LOCATION:
                    activeAvrageLocation((MainActivity) mainActivity);

                    break;
                case MyLocationMarker.DEVICE_LOCATION:
                    double lastUpdateDeviceLocation = Prefs.getSharedPreferencesDouble(Prefs.USER_INFO, Prefs.LOCATION_TIME, mainActivity);
                    if ((nowTime - lastUpdateDeviceLocation) > TimeUnit.MINUTES.toMillis(1)) {
                        activeC4netLocation();
                    } else {
                        activeDeviceLocation();
                    }
                    break;
            }

            HashMap<String, String> m = new HashMap<String, String>();
            m.put(Prefs.ATTRIBUTE_STATUS_TEXT, "I,1," + macAddress + "," + getIcon() + "," + getVersion()
                    + "," + getLocation() + "," + getAge() + "," + connectivity + ",\n");
            m.put(Prefs.INDEX, getIcon());
            m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
            m.put(Prefs.ATTRIBUTE_MARKER_NAME, getNameFromCallSign(macAddress));
            m.put(Prefs.ATTRIBUTE_AGE, General.getAge(System.currentTimeMillis() + ""));

            Prefs.addTheirStatusLocations(m, mainActivity);


        } else {

            // Check if the location is not me from other device
            if (!TextUtils.isEmpty(myMac) && macAddress.equals(myMac)) {
                HashMap<String, String> m = new HashMap<String, String>();
                String error = "Error: Location Of Me from External src";
                m.put(Prefs.ATTRIBUTE_STATUS_TEXT, error);
                m.put(Prefs.ATTRIBUTE_STATUS_TIME, General.getDate());
                Prefs.getInstance().addStatusMessages(m);
                LogFile.getInstance(mainActivity).appendLog(error);
                return;
            }

            String[] s = entry.split(",");
            entry = "I";
            for (int i = 1; i < 7; i++) {
                if (i == 4 && !me) {
                    entry += "," + Prefs.markersEnum.get("L");
                    continue;
                }
                entry += "," + s[i];
            }
            entry += "," + macAddress + ",\n";
            Icon icon = null;
            try {
                icon = new Icon(entry);
                icon.setConnectivity(connectivity);
            } catch (FormatException e) {
                e.printStackTrace();
            }
            icon.setLIcon(s[7]);
            icon.handle(mainActivity, button);
        }
    }

    private void activeAvrageLocation(MainActivity mainActivity) {
        MyLocationMarker.setC4netMarkerVisible(false);
        MyLocationMarker.setDeviceMarkerVisible(false);

        MyLocationMarker.setAvgMarker(mainActivity.map);
        MyLocationMarker.setAvgMarkerVisible(true);
    }

    private void activeDeviceLocation() {
        MyLocationMarker.setC4netMarkerVisible(false);
        MyLocationMarker.setAvgMarkerVisible(false);

        MyLocationMarker.setDeviceMarkerVisible(true);
    }

    private void activeC4netLocation() {
        MyLocationMarker.setDeviceMarkerVisible(false);
        MyLocationMarker.setAvgMarkerVisible(false);
        MyLocationMarker.setC4netMarkerVisible(true);
    }


    private String getNameFromCallSign(String mac) {
        for (CallSign callSign : Prefs.getInstance().getCallSigns()) {

            if (callSign.getMac().equalsIgnoreCase(mac)) {
                return callSign.getName();
            }
        }
        return mac;
    }

}