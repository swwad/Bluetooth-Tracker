package com.monster.dontleave;

import java.util.HashSet;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

	Set<BluetoothDevice> pairedDevices = new HashSet<BluetoothDevice>();
	public final static String StartFromActivity = "StartFromActivity";

	// String IABKey =
	// "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAutzaL2p34g8tkLuySwac0dUT30sR5s61nhI02VITWJSdxZ3y4P6NW1vb8d9a+6dfZYpzYQPkebKVwlvJYFG7xwPeHcqyqCNc5EWa3hPaVbPHfeUrM/AI/pe/Go1LeniZpt27M0A7rUckEDryI+W5Eqp1d9+b0ie3L2aUzKKEKQGa+RDPGfXlVD7zuPuIyZZtgwzu2IDz8SZkBGTYQnbZe4vVetw0o/Vz7g4b3XPeGEYxYlpyj3K5yT93u2T2iUKfdRBHapx3p23xWrA0Ojh+GCBHAn0Jr/X83BqtnPGssrIdUHsZdo5KokQbieqOm6OCfgCulqejbqdGqKsECqj0qQIDAQAB";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_preference);
		reStartService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SetDefaultData();
	}

	void SetDefaultData() {
		ListPreference listPref = (ListPreference) findPreference(getString(R.string.key_setting_select_btdevice));
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			pairedDevices = bluetoothAdapter.getBondedDevices();
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
			listPref.setOnPreferenceChangeListener(this);
			listPref.setEntries(entryDevice);
			listPref.setEntryValues(entryDeviceValues);
			listPref.setEnabled(true);
		} else {
			listPref.setEnabled(false);
		}

		CheckBoxPreference cbPref;
//		= (CheckBoxPreference) findPreference(getString(R.string.key_setting_auto_start));
//		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_setting_auto_start), false));
//		cbPref.setOnPreferenceChangeListener(this);
//		cbPref.setEnabled(true);

//		Preference pref = (Preference) findPreference(getString(R.string.key_setting_support_me));
//		pref.setOnPreferenceClickListener(this);

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

//		listPref = (ListPreference) findPreference(getString(R.string.key_notify_flash));
//		for (int i = 0; i < OptionValue.length; i++) {
//			String sss = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_warning_flash), "0");
//			if (getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(getString(R.string.pref_warning_flash), "0").equalsIgnoreCase(OptionValue[i])) {
//				listPref.setSummary(OptionString[i]);
//				break;
//			}
//		}
//		listPref.setOnPreferenceChangeListener(this);
//		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
//			listPref.setEnabled(true);
//		} else {
//			listPref.setEnabled(false);
//			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(getString(R.string.pref_warning_flash), "0").commit();
//		}

//		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_screen));
//		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_screen), false));
//		cbPref.setOnPreferenceChangeListener(this);
//		cbPref.setEnabled(true);

//		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_popwindow));
//		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_popwindow), false));
//		cbPref.setOnPreferenceChangeListener(this);
//		cbPref.setEnabled(true);


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
		// if
		// (getString(R.string.key_setting_support_me).equals(preference.getKey()))
		// {
		//
		// }

		return true;
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