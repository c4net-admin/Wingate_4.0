package com.maatayim.acceleradio;



import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class FusedLocationService implements
LocationListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener {
	private static final long INTERVAL = 1000 * 10;
	private static final long FASTEST_INTERVAL = 1000 * 5;
	private static final long ONE_MIN = 1000 * 60;
	private static final long REFRESH_TIME = ONE_MIN * 5;
//	private static final float MINIMUM_ACCURACY = 50.0f;
	private static final String TAG = "200apps.AcceleRadio.FusedLocationService";

	private LocationRequest locationRequest;
	private GoogleApiClient googleApiClient;
	private Location location;
	private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
	private LocationListener locationListener;

	private boolean isListening = false;
	private boolean isConnected = false;

	public FusedLocationService(Context context, LocationListener locationListener) {
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);

		this.locationListener = locationListener;

		googleApiClient = new GoogleApiClient.Builder(context)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();

		if (googleApiClient != null) {
			googleApiClient.connect();
		}
		googleApiClient.registerConnectionFailedListener(this);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected ");
		Location currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
		fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
		isListening = true;
		isConnected = true;
		if (currentLocation != null && currentLocation.getTime() > REFRESH_TIME) {
			location = currentLocation;
			locationListener.onLocationChanged(location);
		}

	}



	@Override
	public void onLocationChanged(Location location) {
//		Log.d(TAG, "onLocationChanged ");
		locationListener.onLocationChanged(location);
		this.location = location;
	}


	public Location getLocation() {
		return this.location;
	}


	@Override
	public void onConnectionSuspended(int cause) {
		Log.d(TAG, "onConnectionSuspended ");
		isConnected = false;
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed ");
	}

	public void startListening(){
		if(!isListening && isConnected){
			fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
			isListening = true;
		}

	}
	public void stopListening(){
		if (fusedLocationProviderApi != null && googleApiClient != null && googleApiClient.isConnected()) {
			fusedLocationProviderApi.removeLocationUpdates(googleApiClient,
					FusedLocationService.this);
		}
		isListening = false;
	}
	
}