package com.d.mer.ui.common

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

        fun getInstance(context: Context): PreferenceManager {
            initialize(context)
            return getInstance()
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

    fun getReqCodeForNotification(): Int {
        var code = pref.getInt(Constants.REQ_CODE_FOR_NOTIFICATION, 0)
        // max int value is 2147483647 - just to be on safe side, make 1 less
        val maxInt = 2147483646
        if (code == maxInt) {
            code = 0
        }
        code += 1
        put(Constants.REQ_CODE_FOR_NOTIFICATION, code)
        return code
    }

    fun put(column: String, value: Int) {
        getEditor()?.putInt(column, value)
        getEditor()?.apply()
    }

}