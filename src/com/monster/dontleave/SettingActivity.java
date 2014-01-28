package com.monster.dontleave;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.widget.Toast;

import com.util.IabHelper;
import com.util.IabHelper.OnIabPurchaseFinishedListener;
import com.util.IabResult;
import com.util.Purchase;

//http://www.icoding.co/2012/12/android-in-app-billing-version-3

public class SettingActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

	Set<BluetoothDevice> pairedDevices = new HashSet<BluetoothDevice>();
	public final static String StartFromActivity = "StartFromActivity";

	ArrayList<String> BuyProveList = new ArrayList<String>();

	final static int BuyFullVersionRequestCode = 721010;
	final static String IABRequestCode = "FullVersionID123456789";
	final static String IABKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAutzaL2p34g8tkLuySwac0dUT30sR5s61nhI02VITWJSdxZ3y4P6NW1vb8d9a+6dfZYpzYQPkebKVwlvJYFG7xwPeHcqyqCNc5EWa3hPaVbPHfeUrM/AI/pe/Go1LeniZpt27M0A7rUckEDryI+W5Eqp1d9+b0ie3L2aUzKKEKQGa+RDPGfXlVD7zuPuIyZZtgwzu2IDz8SZkBGTYQnbZe4vVetw0o/Vz7g4b3XPeGEYxYlpyj3K5yT93u2T2iUKfdRBHapx3p23xWrA0Ojh+GCBHAn0Jr/X83BqtnPGssrIdUHsZdo5KokQbieqOm6OCfgCulqejbqdGqKsECqj0qQIDAQAB";
	final static String FullVersionID = "dontleave.full.version";

	IabHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BluetoothAdapter.getDefaultAdapter().enable();
		addPreferencesFromResource(R.xml.setting_preference);
		reStartService();
		hCheckBtDeviceStatus.sendEmptyMessage(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetDefaultData();
	}

	Handler hCheckBtDeviceStatus = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (BluetoothAdapter.getDefaultAdapter() != null) {
				pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
				ListPreference listPref = (ListPreference) findPreference(getString(R.string.key_setting_select_btdevice));
				if (pairedDevices.size() > 0) {
					listPref.setSummary("");
					String[] entryDeviceValues = new String[pairedDevices.size()];
					String[] entryDevice = new String[pairedDevices.size()];
					int i = 0;
					for (BluetoothDevice device : pairedDevices) {
						entryDeviceValues[i] = device.getAddress();
						entryDevice[i] = device.getName();
						if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_address), "").equalsIgnoreCase(device.getAddress()))
							listPref.setSummary(device.getName());
						i++;
					}
					listPref.setEntries(entryDevice);
					listPref.setEntryValues(entryDeviceValues);
					listPref.setOnPreferenceChangeListener(SettingActivity.this);
					listPref.setEnabled(true);
					hCheckBtDeviceStatus.removeCallbacksAndMessages(null);
				} else {
					listPref.setEnabled(false);
					hCheckBtDeviceStatus.sendMessageDelayed(new Message().obtain(), 500);

				}
			}

		}
	};

	void SetDefaultData() {
		ListPreference listPref;
//		= (ListPreference) findPreference(getString(R.string.key_setting_select_btdevice));
//		if (BluetoothAdapter.getDefaultAdapter() != null) {
//			pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
//			listPref.setSummary("");
//			String[] entryDeviceValues = new String[pairedDevices.size()];
//			String[] entryDevice = new String[pairedDevices.size()];
//			int i = 0;
//			for (BluetoothDevice device : pairedDevices) {
//				entryDeviceValues[i] = device.getAddress();
//				entryDevice[i] = device.getName();
//				if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_address), "").equalsIgnoreCase(device.getAddress()))
//					listPref.setSummary(device.getName());
//				i++;
//			}
//			listPref.setEntries(entryDevice);
//			listPref.setEntryValues(entryDeviceValues);
//			listPref.setOnPreferenceChangeListener(this);
//			listPref.setEnabled(true);
//		} else {
//			listPref.setEnabled(false);
//		}

		CheckBoxPreference cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_setting_auto_start));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_setting_auto_start), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setEnabled(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.key_full_version), false));

		Preference pref = (Preference) findPreference(getString(R.string.key_setting_support_me));
		if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.key_full_version), false)) {
			if (pref != null)
				((PreferenceGroup) findPreference(getString(R.string.key_setting_title))).removePreference(pref);
		} else {
			pref.setOnPreferenceClickListener(this);
		}

		String[] OptionString = getResources().getStringArray(R.array.warning_option_string);
		String[] OptionValue = getResources().getStringArray(R.array.warning_option_value);

		listPref = (ListPreference) findPreference(getString(R.string.key_notify_audio));
		for (int i = 0; i < OptionValue.length; i++) {
			if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_warning_audio), "0").equalsIgnoreCase(OptionValue[i])) {
				listPref.setSummary(OptionString[i]);
				break;
			}
		}
		listPref.setOnPreferenceChangeListener(this);
		listPref.setEnabled(true);

		listPref = (ListPreference) findPreference(getString(R.string.key_notify_vibrate));
		for (int i = 0; i < OptionValue.length; i++) {
			if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_warning_vibrator), "0").equalsIgnoreCase(OptionValue[i])) {
				listPref.setSummary(OptionString[i]);
				break;
			}
		}
		listPref.setOnPreferenceChangeListener(this);
		listPref.setEnabled(true);

		listPref = (ListPreference) findPreference(getString(R.string.key_notify_flash));
		for (int i = 0; i < OptionValue.length; i++) {
			String sss = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_warning_flash), "0");
			if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_warning_flash), "0").equalsIgnoreCase(OptionValue[i])) {
				listPref.setSummary(OptionString[i]);
				break;
			}
		}
		listPref.setOnPreferenceChangeListener(this);
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			listPref.setEnabled(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.key_full_version), false));
		} else {
			listPref.setEnabled(false);
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(getString(R.string.pref_warning_flash), "0").commit();
		}

		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_screen));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_screen), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setEnabled(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.key_full_version), false));

		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_popwindow));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_popwindow), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setEnabled(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.key_full_version), false));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (getString(R.string.key_setting_select_btdevice).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(getString(R.string.pref_setting_bt_device_address), (String) newValue).commit();

			for (BluetoothDevice device : pairedDevices) {
				if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_address), "").equalsIgnoreCase(device.getAddress())) {
					((ListPreference) findPreference(getString(R.string.key_setting_select_btdevice))).setSummary(device.getName());
					getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(getString(R.string.pref_setting_bt_device_name), device.getName()).commit();
					break;
				}
			}
			reStartService();
		} else if (getString(R.string.key_setting_auto_start).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_setting_auto_start), (Boolean) newValue).commit();
			reStartService();
		} else if (getString(R.string.key_notify_audio).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(getString(R.string.pref_warning_audio), (String) newValue).commit();
		} else if (getString(R.string.key_notify_flash).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(getString(R.string.pref_warning_flash), (String) newValue).commit();
		} else if (getString(R.string.key_notify_popwindow).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_warning_popwindow), (Boolean) newValue).commit();
		} else if (getString(R.string.key_notify_screen).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_warning_screen), (Boolean) newValue).commit();
		} else if (getString(R.string.key_notify_vibrate).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(getString(R.string.pref_warning_vibrator), (String) newValue).commit();
		}
		SetDefaultData();
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (getString(R.string.key_setting_support_me).equals(preference.getKey())) {
			if (mHelper == null)
				mHelper = new IabHelper(this, IABKey);
			BuyProveList.clear();
			BuyProveList.add(FullVersionID);

			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					if (!result.isSuccess()) {
						Toast.makeText(SettingActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
						releaseIabHelper();
					} else {
						mHelper.launchPurchaseFlow(SettingActivity.this, FullVersionID, BuyFullVersionRequestCode, new OnIabPurchaseFinishedListener() {
							@Override
							public void onIabPurchaseFinished(IabResult result, Purchase info) {
								if (result.isFailure()) {
									if (result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
										getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.key_full_version), true).commit();
										Toast.makeText(SettingActivity.this, R.string.iabhelper_fullversion, Toast.LENGTH_LONG).show();
									} else {
										getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.key_full_version), false).commit();
										Toast.makeText(SettingActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
									}
								} else if (info.getSku().equals(FullVersionID)) {
									getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.key_full_version), true).commit();
									Toast.makeText(SettingActivity.this, R.string.iabhelper_fullversion, Toast.LENGTH_LONG).show();
								}
								releaseIabHelper();
								SetDefaultData();
							}
						}, IABRequestCode);
					}
				}
			});
		}
		return true;
	}

	private void releaseIabHelper() {
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
	}

	public void reStartService() {
		stopService(new Intent(SettingActivity.this, MonitorDeviceService.class));
		if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_setting_bt_device_address), "").length() == 0) {
			Toast.makeText(SettingActivity.this, R.string.warning_no_btdevice, Toast.LENGTH_SHORT).show();
		} else {
			startService(new Intent(SettingActivity.this, MonitorDeviceService.class).putExtra(StartFromActivity, true));
		}
	}
}