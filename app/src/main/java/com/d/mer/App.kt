package com.d.mer

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        MultiDex.install(this)
    }

}