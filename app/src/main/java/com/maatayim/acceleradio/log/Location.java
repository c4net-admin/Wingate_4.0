package com.maatayim.acceleradio.log;

import android.app.Activity;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.Prefs;
import com.maatayim.acceleradio.mapshapes.MyLocationMarker;
import com.maatayim.acceleradio.utils.FormatException;

public class Location extends LogEntry {

    private static final String LOCATION = "L";
    private String macAddress;
    private String slotIndex;
    private String icon; //refer to icon enumeration table
    private LatLng latlng;
    private String age;
    private String connectivity;
    private boolean me; //optional
    private static MyLocationMarker marker;


    public Location(String str) throws FormatException {
        super(str);
        parseStr();
    }

    private void parseStr() throws FormatException {
        if (entry.endsWith("\n")) {
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
        if (me) {
            int myLocationType = Prefs.getSharedPreferencesInt(Prefs.MARKERS, Prefs.MY_LOCATION_TYPE, mainActivity);
            if (myLocationType == MyLocationMarker.C4NET_LOCATION) {

                if (marker == null) {
                    marker = new MyLocationMarker(((MainActivity) mainActivity).map, latlng);
                } else {
                    marker.updateLocation(latlng);
                }

            }
        } else {
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            icon.setLIcon(s[7]);
            icon.handle(mainActivity, button);
        }
    }

}