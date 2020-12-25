package com.d.mer

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.d.mer.data.repositories.volley.VolleyRepoImpl
import com.d.mer.ui.common.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

        VolleyRepoImpl.initialize(getString(R.string.firebase_api_key), this)

        PreferenceManager.initialize(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        MultiDex.install(this)
    }

}