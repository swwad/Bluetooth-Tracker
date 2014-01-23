package com.darren.dontleave;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.Vibrator;

public class MonitorDeviceService extends Service {

	NotificationManager mNM;
	Camera camera;
	Parameters camera_parameters;
	int iBackCameraID = -1;

	@Override
	public void onCreate() {
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				iBackCameraID = i;
				break;
			}
		}

		if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_address), "").length() == 0) {
			stopSelf();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			unregisterReceiver(brReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_setting_auto_start), false) || intent.getBooleanExtra(SettingActivity.StartFromActivity, false)) {
			registerReceiver(brReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
			registerReceiver(brReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
			registerReceiver(brReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
			showNotification();
		} else {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	Handler hFindDevice = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (android.os.Build.VERSION.SDK_INT < 11) {

				super.handleMessage(msg);
			}
		}
	};

	private final BroadcastReceiver brReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ((BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) || (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(intent.getAction()))) {
				BluetoothDevice bdDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (bdDevice.getAddress().equalsIgnoreCase(getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_address), ""))) {
					if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_audio), false)) {
						warningAudio();
					}
					if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_screen), false)) {
						warningScreen();
					}
					if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_flash), false)) {
						warningFlash();
					}
					if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_vibrator), false)) {
						warningVibrator();
					}
				}
				hFindDevice.sendEmptyMessage(0);
			} else if ((BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) || (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction()))) {
				BluetoothDevice bdDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (bdDevice.getAddress().equalsIgnoreCase(getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_address), ""))) {
					hFindDevice.removeCallbacksAndMessages(null);
				}
			}
		}
	};

	@Override
	public void onDestroy() {
		try {
			unregisterReceiver(brReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public void warningVibrator() {
		((Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE)).vibrate(10000);
	}

	public void warningScreen() {
		new Thread() {
			public void run() {
				synchronized (this) {
					PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
					WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
					wakeLock.acquire();
				}
			}
		}.start();
	}

	public void warningFlash() {
		new Thread() {
			public void run() {
				synchronized (this) {
					if (iBackCameraID != -1) {
						camera = Camera.open(iBackCameraID);
						camera_parameters = camera.getParameters();

						for (int i = 0; i < 10; i++) {
							camera_parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
							camera.setParameters(camera_parameters);
							SystemClock.sleep(500);
							camera_parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
							camera.setParameters(camera_parameters);
							SystemClock.sleep(500);
						}
						camera.stopPreview();
						camera.release();
					}
				}
			}
		}.start();
	}

	public void warningAudio() {
		AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(true);
		// setVolumeControlStream(AudioManager.STREAM_MUSIC);
		audioManager.setMode(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		MediaPlayer playerSound = MediaPlayer.create(this, R.raw.bb);
		playerSound.start();
	}

	private void showNotification() {
		Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.run_in_service), System.currentTimeMillis());
		notification.flags = Notification.FLAG_NO_CLEAR;
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MonitorDeviceService.class), 0);
		notification.setLatestEventInfo(this, getString(R.string.run_in_service),
				getString(R.string.connecting_device) + getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_name), ""), contentIntent);
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNM.notify(R.string.app_name, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}