package com.maatayim.acceleradio.usbserial;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import static com.maatayim.acceleradio.Parameters.MAX_COUNTER_LENGTH;

public class UsbService extends Service
{
	public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
	public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
	public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
	public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
	public static final String ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
	public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
	public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
	public static final String ACTION_CDC_DRIVER_NOT_WORKING ="com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
	public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";

	private static int messageCounter = 0;
	
	private static final int BAUD_RATE = 57600; // BaudRate. Change this value if you need
	public static final int MESSAGE_FROM_SERIAL_PORT = 0;
	
	public static boolean SERVICE_CONNECTED = false;
	
	private IBinder binder = new UsbBinder();
	
	private Context context;
	private Handler mHandler;
	private UsbManager usbManager;
	private UsbDevice device;
	private UsbDeviceConnection connection;
	private UsbSerialDevice serialPort;
	
	/*
	 * onCreate will be executed when service is started. It configures an IntentFilter to listen for
	 * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
	 */
	@Override
	public void onCreate()
	{
		this.context = this;
		UsbService.SERVICE_CONNECTED = true;
		setFilter();
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		findSerialPortDevice();
	}
	
	/* MUST READ about services
	 * http://developer.android.com/guide/components/services.html
	 * http://developer.android.com/guide/components/bound-services.html
	 */
	@Override
	public IBinder onBind(Intent intent) 
	{
		return binder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		UsbService.SERVICE_CONNECTED = false;
	}
	
	/*
	 * This function will be called from MainActivity to write data through Serial Port
	 */
	public void write(byte[] data)
	{
		if(serialPort != null) {
			serialPort.write(data);
			String dataFormated = new String(data, Charset.forName("UTF-8"));
			Log.d("Vova "+"write", "data: "+dataFormated);
		}
	}
	
	public void setHandler(Handler mHandler)
	{
		this.mHandler = mHandler;
	}
	
	private void findSerialPortDevice()
	{
		Log.d("usbService", "find");
		// This snippet will try to open the first encountered usb device connected, excluding usb root hubs
		HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
		if(!usbDevices.isEmpty())
		{
			boolean keep = true;
			for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
			{
				device = entry.getValue();
				int deviceVID = device.getVendorId();
				int devicePID = device.getProductId();
				if((deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003) && deviceVID < 0x8000 ))
				{
					// There is a device connected to our Android device. Try to open it as a Serial Port.
					requestUserPermission();
					keep = false;  
				}else
				{
					connection = null;
					device = null;
				}

				if(!keep)
					break;
			}
			if(!keep)
			{
				// There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
				Intent intent = new Intent(ACTION_NO_USB);
				sendBroadcast(intent);
			}
		}else
		{
			// There is no USB devices connected. Send an intent to MainActivity
			Intent intent = new Intent(ACTION_NO_USB);
			sendBroadcast(intent);
		}
	}

	private void setFilter()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(ACTION_USB_DETACHED);
		filter.addAction(ACTION_USB_ATTACHED);
		registerReceiver(usbReceiver , filter);
	}
	
	/*
	 * Request user permission. The response will be received in the BroadcastReceiver
	 */
	private void requestUserPermission()
	{
		PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION),0);
		usbManager.requestPermission(device, mPendingIntent);
	}
	
	/*
	 *  Data received from serial port will be received here. Just populate onReceivedData with your code
	 *  In this particular example. byte stream is converted to String and send to UI thread to
	 *  be treated there.
	 */
	private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() 
	{
		@Override
		public void onReceivedData(byte[] arg0) 
		{
			String data = new String(arg0, Charset.forName("UTF-8"));
			if(mHandler != null)
				mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT,data).sendToTarget();
		}
	};
	
	public class UsbBinder extends Binder
	{
		public UsbService getService()
		{
			return UsbService.this;
		}
	}
	
	/*
	 * Different notifications from OS will be received here (USB attached, detached, permission responses...)
	 * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
	 */
	private final BroadcastReceiver usbReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{
			if(arg1.getAction().equals(ACTION_USB_PERMISSION))
			{
				boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
				if(granted) // User accepted our USB connection. Try to open the device as a serial port
				{
					Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
					arg0.sendBroadcast(intent);
					connection = usbManager.openDevice(device);
					new ConnectionThread().run();
				}else // User not accepted our USB connection. Send an Intent to the Main Activity
				{
					Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
					arg0.sendBroadcast(intent);
				}
			}else if(arg1.getAction().equals(ACTION_USB_ATTACHED))
			{
				findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
			}else if(arg1.getAction().equals(ACTION_USB_DETACHED))
			{
				// Usb device was disconnected. send an intent to the Main Activity
				Intent intent = new Intent(ACTION_USB_DISCONNECTED);
				arg0.sendBroadcast(intent);
			}
		}
	};
	
	/*
	 * A simple thread to open a serial port.
	 * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
	 */
	private class ConnectionThread extends Thread
	{
		@Override
		public void run()
		{
			serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
			if(serialPort != null)
			{
				if(serialPort.open())
				{
					serialPort.setBaudRate(BAUD_RATE);
					serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
					serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
					serialPort.setParity(UsbSerialInterface.PARITY_NONE);
					serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
					serialPort.read(mCallback);
					
					// Everything went as expected. Send an intent to MainActivity
					Intent intent = new Intent(ACTION_USB_READY);
					context.sendBroadcast(intent);
				}else
				{
					// Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
					// Send an Intent to Main Activity
					if(serialPort instanceof CDCSerialDevice)
					{
						Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
						context.sendBroadcast(intent);
					}else
					{
						Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
						context.sendBroadcast(intent);
					}
				}
			}else
			{
				// No driver for given device, even generic CDC driver could not be loaded
				Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
				context.sendBroadcast(intent);
			}
		}
	}

	public static String getMessageCounter(){
		if (messageCounter == MAX_COUNTER_LENGTH){
			messageCounter = 0;
		}
		return String.format("%02d", messageCounter++);

	};
}
