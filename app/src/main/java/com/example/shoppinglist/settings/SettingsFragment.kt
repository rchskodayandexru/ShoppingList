package com.example.shoppinglist.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.shoppinglist.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }

}