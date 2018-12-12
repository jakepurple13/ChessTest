package com.crestron.aurora.utilities

import com.crestron.aurora.FunApplication
import org.jetbrains.anko.defaultSharedPreferences

class SharedPrefVariables {
    companion object {
        var latestVersion: Float = 7.8f
            set(value) {
                FunApplication.getAppContext().defaultSharedPreferences.edit().putFloat("latestVersionSaved", value).apply()
                hasShownForLatest = field != value
                field = value
            }
            get() {
                return FunApplication.getAppContext().defaultSharedPreferences.getFloat("latestVersionSaved", 7.8f)
            }

        var hasShownForLatest = false
            set(value) {
                FunApplication.getAppContext().defaultSharedPreferences.edit().putBoolean("shouldLatestVersion", value).apply()
                field = value
            }
            get() {
                return FunApplication.getAppContext().defaultSharedPreferences.getBoolean("shouldLatestVersion", false)
            }
    }
}