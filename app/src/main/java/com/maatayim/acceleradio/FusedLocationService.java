package com.maatayim.acceleradio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class FusedLocationService {
	private static final long INTERVAL = 1000 * 10;
	private static final long FASTEST_INTERVAL = 1000 * 5;
	private static final long ONE_MIN = 1000 * 60;
	private static final long REFRESH_TIME = ONE_MIN * 5;
	private static final String TAG = "FusedLocationService";

	private final FusedLocationProviderClient fusedLocationClient;
	private final LocationRequest locationRequest;
	private final LocationCallback locationCallback;
	private final LocationListener locationListener;
	private final Context context;

	private Location lastLocation;
	private boolean isListening = false;


	public FusedLocationService(Context context, LocationListener locationListener) {
		this.context = context;
		this.locationListener = locationListener;
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

		locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL)
				.setMinUpdateIntervalMillis(FASTEST_INTERVAL)
				.build();

		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(@NonNull LocationResult locationResult) {
				for (Location location : locationResult.getLocations()) {
					onLocationChanged(location);
				}
			}
		};
	}

	private void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		lastLocation = location;
		locationListener.onLocationChanged(location);
	}

	public void startListening() {
		if (!isListening) {
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
					&& ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Log.e(TAG, "Location permission not granted");
				return;
			}
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
			isListening = true;

			fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
				if (location != null && System.currentTimeMillis() - location.getTime() < REFRESH_TIME) {
					onLocationChanged(location);
				}
			});
		}
	}

	public void stopListening() {
		if (isListening) {
			fusedLocationClient.removeLocationUpdates(locationCallback);
			isListening = false;
		}
	}

	public Location getLocation() {
		return lastLocation;
	}
}