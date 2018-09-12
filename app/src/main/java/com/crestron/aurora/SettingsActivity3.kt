package com.crestron.aurora

import android.os.Bundle
import android.preference.PreferenceActivity

class SettingsActivity3 : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings_list)
    }

}