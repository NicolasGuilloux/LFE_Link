package fr.lfe.isn.link;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Configuration extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.option_configuration);}
}