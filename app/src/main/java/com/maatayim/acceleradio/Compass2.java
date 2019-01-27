package com.maatayim.acceleradio;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class Compass2 implements SensorEventListener {

	interface CompassListener{
		void onCompassChanged(float degree);
	}
	
	private static final String TAG = "200apps.Compass2";
	private View arrow;
	private float currentDegree = 0f;
	// device sensor manager
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;

	private float[] mGravity;
	private float[] mGeomagnetic;
	private CompassListener listener;
	private float lastDegreeOnRotate;


	public Compass2(Context context, View arrow, CompassListener listener){
		this.arrow = arrow;
		this.listener = listener;
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}



	@SuppressWarnings("deprecation")
	public void startSensors(){
		Log.d(TAG,"start");

				mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
						SensorManager.SENSOR_DELAY_GAME);
				
//		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
//		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	public void stopSensors(){

		Log.d(TAG,"stop");
		mSensorManager.unregisterListener(this, accelerometer);
		mSensorManager.unregisterListener(this, magnetometer);
		mSensorManager.unregisterListener(this);
		rotateView(0);
	}


	private void rotateView(float degree){
		RotateAnimation ra = new RotateAnimation(
				currentDegree, 
				-degree,
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF,
				0.5f);

		// how long the animation will take place
		ra.setDuration(210);

		// set the animation after the end of the reservation status
		ra.setFillAfter(true);

		// Start the animation
		arrow.startAnimation(ra);

		currentDegree = -degree;
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {
		//old sensor
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
			float degree = Math.round(event.values[0]);
			degree += 90F;
			Log.d(TAG, "onSensorChanged 1 degree "+degree);
			rotateView(degree);
//			if(delta < DEGREES_DELTA)
//				return;
			
			
			listener.onCompassChanged(degree);
			return;
		}

		
		float azimut = 0;
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);

				azimut = orientation[0]; // orientation contains: azimut, pitch and roll
				//				Log.d(TAG,"onSensorChanged  before degree "+azimut);
				//		azimut = Math.round(azimut);
				//				if(counter ++ > 10){
				//					counter = 0;
				float degree = (float) Math.toDegrees(azimut);
				//		Log.d(TAG,"onSensorChanged  before degree "+degree);

				degree += 90F;
				if(degree < 0){
					degree = (360F + degree);
				}
				degree = Math.round(degree);
				Log.d(TAG,"onSensorChanged 2 degree "+degree);
				
				float delta = Math.abs(lastDegreeOnRotate - degree);
				
				Log.d(TAG,"delta "+lastDegreeOnRotate +" "+ degree+" = "+delta);
				
				
				rotateView(degree);
//				if(delta < DEGREES_DELTA)
//					return;
				
				
				listener.onCompassChanged(degree);
				
				lastDegreeOnRotate = degree;
				
				
				//				}
			}
		}

	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(TAG, "onAccuracyChanged "+accuracy);

	}
}
