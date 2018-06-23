package kr.maden.watson_iot.ui.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import kr.maden.watson_iot.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static SettingsFragment settingsFragment;

    public SettingsFragment() {
    }

    public static SettingsFragment getInstance() {
        if(settingsFragment==null)
            settingsFragment = new SettingsFragment();
        return settingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstancesState){
        super.onCreate(savedInstancesState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }
}
