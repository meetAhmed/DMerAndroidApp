package com.d.mer.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build


/**
 * Utility object contains methods which can be used at multiple location in app
 */
object Utility {

    /**
     * Opens App in play - store
     */
    fun openAppInPlayStore(packageName: String, context: Context) {
        try {
            val rateIntent: Intent = intentForUrl("market://details", packageName)
            context.startActivity(rateIntent)
        } catch (e: ActivityNotFoundException) {
            val rateIntent: Intent =
                intentForUrl("https://play.google.com/store/apps/details", packageName)
            context.startActivity(rateIntent)
        }
    }

    @Suppress("DEPRECATION")
    private fun intentForUrl(url: String, packageName: String): Intent {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, packageName)))
        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        flags = if (Build.VERSION.SDK_INT >= 21) {
            flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        } else {
            flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
        }
        intent.addFlags(flags)
        return intent
    }

}