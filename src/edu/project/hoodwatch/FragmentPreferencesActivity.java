package edu.project.hoodwatch;

/*
 * Activity to load and display a fragement and related xml schema
 * 
 */


import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;


public class FragmentPreferencesActivity extends Activity {

	// ------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();

	}

	// ------------------------------------------------------------------------
	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}
	}

	

	
}
