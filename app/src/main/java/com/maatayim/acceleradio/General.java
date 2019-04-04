package com.maatayim.acceleradio;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.google.android.gms.maps.model.LatLng;

import static com.maatayim.acceleradio.Parameters.CHECKSUM_PEDDING;
import static com.maatayim.acceleradio.Parameters.SUB_DELIMITER;

public class General {

    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


    public static String convertLocationToString(LatLng point) {
        DecimalFormat df = new DecimalFormat(Parameters.FORMAT_ROUND_LOCATION_TO_SHOW);
        //String loc = getString(R.string.location_format);
        // TODO change text to string
        String message = (String.format("Location: (%s, %s)", "" + df.format(point.latitude), "" + df.format(point.longitude)));
        return message;
    }


    public static Location convertPointToLocation(LatLng point) {
        Location pressedLocation = new Location("");
        pressedLocation.setLatitude(point.latitude);
        pressedLocation.setLongitude(point.longitude);
        return pressedLocation;
    }


    public static double getAzimut(LatLng fromLocation, LatLng targetLocation) {
        double lat2 = targetLocation.latitude;
        double lon2 = targetLocation.longitude;
        double lat1 = fromLocation.latitude;
        double lon1 = fromLocation.longitude;

        double lat1Rad = lat1 * Math.PI / 180D;
        double lat2Rad = lat2 * Math.PI / 180D;

        double dLon = (lon2 - lon1) * Math.PI / 180D;

        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));

        while (bearing < 0) {
            bearing += 360;
        }
        return bearing;
    }

    public static double distance(LatLng p1, LatLng p2) {
        Location location1 = new Location("locationA");
        location1.setLatitude(p1.latitude);
        location1.setLongitude(p1.longitude);
        Location location2 = new Location("locationA");
        location2.setLatitude(p2.latitude);
        location2.setLongitude(p2.longitude);
        return location1.distanceTo(location2);
    }

    public static double truncDouble(double d, int n) {
        int x = (int) (d * Math.pow(10, n));
        return (double) x / Math.pow(10, n);
    }

    public static String precisionFormat(double p1, double p2) {
        String fLat = "" + p1;
        String fLon = "" + p2;
        int precision = fLat.substring(fLat.indexOf('.') + 1).length();
        for (int i = 0; i < 5 - precision; i++) {
            fLat += "0";
        }
        precision = fLon.substring(fLat.indexOf('.') + 1).length();
        for (int i = 0; i < 5 - precision; i++) {
            fLon += "0";
        }
        return fLat + "," + fLon;
    }

    public static String getAge(String age) {
        long time;
        try {
            time = Long.parseLong(age);
        } catch (Exception e) {
            return age;
        }
        long now = new Date().getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date((time )));
    }


    public static String getNowTimeLong() {
        return "" + new Date().getTime();
    }

    public static String getApkVersionName(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return pInfo.versionName;
    }

    public static String getCheckSum(String msg) {
        int check = 0x46;
        int sum = 0xed;
        int a, b;

        if (TextUtils.isEmpty(msg)) {
            return getStringFromHex(check) + getStringFromHex(sum);
        }

        msg = addParityChar(msg);

        for (int i = 0; i < msg.length(); i += 2) {
            a = msg.charAt(i);
            b = msg.charAt(i + 1);

            check = (char) (check ^ a);
            sum = (char) (sum ^ b);

        }
        return getStringFromHex(check) + getStringFromHex(sum);

    }

    @NonNull
    private static String addParityChar(String msg) {
        if (msg.length() % 2 == 1) {
            msg += CHECKSUM_PEDDING;
        }
        return msg;
    }

    public static boolean compareCheckSum(String msg,String checkSum){
        return getCheckSum(msg).equals(checkSum);
    }

    public static String addCheckSum(String msg) {
        msg = addParityChar(msg);
        return msg + SUB_DELIMITER + getCheckSum(msg);
    }

    @NonNull
    public static String getStringFromHex(int hex) {
        String s = Integer.toHexString(hex);
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s.toUpperCase();
    }


}
