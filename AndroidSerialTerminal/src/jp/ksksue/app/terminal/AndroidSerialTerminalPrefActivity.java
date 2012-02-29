package jp.ksksue.app.terminal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class AndroidSerialTerminalPrefActivity extends PreferenceActivity {
	final static String BAUDRATE_KEY = "baudrate_list";  
	  
	Map<String, String> baudrateMap = new HashMap<String, String>();  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		// show a Preference header
		loadHeadersFromResource(R.xml.pref_header, target);
	}
	
	public static class SettingPrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// show a Preference inner
			addPreferencesFromResource(R.xml.pref_inner);
		}
	}

	public static class SettingDisplayPrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// show a Preference inner
			addPreferencesFromResource(R.xml.pref_disp_inner);
		}
	}
	
	/*
	 * @Override protected void onResume() { CharSequence cs =
	 * getText(R.xml.pref_inner); ListPreference lp =
	 * (ListPreference)getPreferenceScreen().findPreference("baudrate_list");
	 * lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
	 * 
	 * @Override public boolean onPreferenceChange(
	 * android.preference.Preference preference, Object newValue){ String
	 * summary = ""; summary = font_normal_summary + newValue; ((ListPreference)
	 * preference).setSummary(summary); return true; } }); super.onResume(); }
	 */
	private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

//			if (key.equals("baudrate_list"))
//				searchbooksPref.setSummary(searchbooksMap.get(sharedPreferences
//						.getString(key, "Books")));
		}
	};

	/*	private SharedPreferences.OnSharedPreferenceChangeListener listener =   
		    new SharedPreferences.OnSharedPreferenceChangeListener() {
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		ListPreference lp = (ListPreference)findPreference(key);
		lp.setSummary(lp.getValue());
		}
	};
	*/
	@Override
	protected void onResume() {
	    super.onResume();
//	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	}
	 
	@Override
	protected void onPause() {
	    super.onPause();
//	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
	}
}