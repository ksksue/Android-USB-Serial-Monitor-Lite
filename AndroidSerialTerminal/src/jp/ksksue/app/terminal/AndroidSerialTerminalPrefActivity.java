package jp.ksksue.app.terminal;

import java.util.List;

import jp.ksksue.driver.serial.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class AndroidSerialTerminalPrefActivity extends PreferenceActivity {
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
}