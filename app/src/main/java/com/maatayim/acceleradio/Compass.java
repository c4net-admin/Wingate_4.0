package com.maatayim.acceleradio;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


interface CompassListener{
	void onCompassChanged(float azimut);
}

public class Compass implements SensorEventListener {
	
	CompassListener compassListener;
	
	private float ALPHA = 0.75f;
	
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private float azimut;
	
	public Compass(Context context, CompassListener cl) {
		
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		compassListener = cl;
	}
	
	
	public void startSensors(){
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	
	public void stopSensors(){
		mSensorManager.unregisterListener(this, accelerometer);
		mSensorManager.unregisterListener(this, magnetometer);
	}
	
	
	public void setCompassSensitivity(float alpha){
		ALPHA = alpha;
	}
	
	
	protected float[] lowPass( float[] input, float[] output ) {
		if ( output == null ) return input;

		for ( int i=0; i<input.length; i++ ) {
			output[i] = ALPHA * output[i] + (1 - ALPHA) * (input[i]);
		}
		return output;
	}
	

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = lowPass(event.values.clone(),mGravity);
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = lowPass(event.values.clone(),mGeomagnetic);
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimuthInRadians = orientation[0];
				float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
				if (Math.abs(azimut - azimuthInDegress) > 6){
//					float delta = (azimut - azimuthInDegress) / 50.0f;
//					for (int i = 0; i < 50; i++){
//						azimut = azimut - delta;
						compassListener.onCompassChanged(azimuthInDegress);
						azimut = azimuthInDegress;
//					}
//				compassListener.onCompassChanged(azimuthInDegress);

				}
				Log.d("sensor changed", "!!!");


			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//not used
		
	}
	
	

}
