package com.d.mer.common

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager private constructor() {

    companion object {
        private var instance_of_pref_manager: PreferenceManager? = null
        private lateinit var pref: SharedPreferences
        private var prefEditor: SharedPreferences.Editor? = null
        private const val preferenceName = Constants.PREFERENCE_NAME

        fun initialize(context: Context) {
            if (instance_of_pref_manager == null) {
                instance_of_pref_manager = PreferenceManager()
                pref = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            }
        }

        fun getInstance(): PreferenceManager {
            return instance_of_pref_manager
                ?: throw IllegalStateException("PreferenceManager must be initialized first.")
        }

        fun getInstance(context: Context): PreferenceManager? {
            initialize(context)
            return instance_of_pref_manager
        }

        fun getPreference(): SharedPreferences {
            return pref
        }

    }

    private fun getEditor(): SharedPreferences.Editor? {
        if (prefEditor == null) {
            prefEditor = pref.edit()
        }
        return prefEditor
    }
}