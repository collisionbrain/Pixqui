package com.libre.mixtli.ui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.libre.mixtli.R;

/**
 * Created by hugo on 14/04/18.
 */

public class ActivitySettings extends PreferenceActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.settings_app);

        CheckBoxPreference checkCapturePositive=(CheckBoxPreference)  getPreferenceManager().findPreference("checkbox_capture_positive");
        CheckBoxPreference checkCaptureNegative=(CheckBoxPreference)  getPreferenceManager().findPreference("checkbox_capture_negative");
        CheckBoxPreference checkCapturePascalVOC=(CheckBoxPreference)  getPreferenceManager().findPreference("checkbox_capture_pascalformat");

    }

    Preference.OnPreferenceChangeListener onPreferenceListener =new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            Log.e("################",""+newValue);

            return false;
        }
    };

}