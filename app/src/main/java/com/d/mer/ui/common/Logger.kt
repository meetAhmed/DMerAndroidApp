package com.d.mer.ui.common

import android.util.Log

object Logger {

    private const val tag = "logsOfApp"

    fun info(text: String?, tagToShow: String = tag) {
        text?.let {
            Log.i(tagToShow, it)
        }
    }

}