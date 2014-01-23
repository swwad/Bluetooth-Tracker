package com.darren.dontleave;

import java.util.HashSet;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
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

		CheckBoxPreference cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_setting_auto_start));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_setting_auto_start), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setEnabled(true);

		Preference pref = (Preference) findPreference(getString(R.string.key_setting_support_me));
		pref.setOnPreferenceClickListener(this);

		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_audio));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_audio), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setEnabled(true);

		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_flash));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_flash), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setOnPreferenceClickListener(this);
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			cbPref.setEnabled(true);
		} else {
			cbPref.setChecked(false);
			cbPref.setEnabled(false);
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_warning_flash), false).commit();
		}

		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_screen));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_screen), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setEnabled(true);

		cbPref = (CheckBoxPreference) findPreference(getString(R.string.key_notify_vibrate));
		cbPref.setChecked(getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(getString(R.string.pref_warning_vibrator), false));
		cbPref.setOnPreferenceChangeListener(this);
		cbPref.setEnabled(true);
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
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_warning_audio), (Boolean) newValue).commit();
		} else if (getString(R.string.key_notify_flash).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_warning_flash), (Boolean) newValue).commit();
		} else if (getString(R.string.key_notify_screen).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_warning_screen), (Boolean) newValue).commit();
		} else if (getString(R.string.key_notify_vibrate).equals(preference.getKey())) {
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(getString(R.string.pref_warning_vibrator), (Boolean) newValue).commit();
		}

		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (getString(R.string.key_setting_support_me).equals(preference.getKey())) {

		}

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