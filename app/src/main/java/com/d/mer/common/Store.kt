package com.d.mer.common

import java.text.SimpleDateFormat
import java.util.*

object Store {

    fun getTextDate(presentTime: Long, futureTime: Long): String {
        val diff: Long = futureTime - presentTime

        val seconds = diff / 1000
        val minutes = seconds / 60
        var hours = minutes / 60
        val days = hours / 24

        hours -= (days * 24)

        return if (presentTime >= futureTime) {
            "0 Days\n0 Hours : 0 Min : 0 sec"
        } else {
            if (hours <= 0L) {
                "$days Days\n$hours Hours : $minutes Min : ${getSeconds(presentTime)} sec"
            } else {
                "$days Days\n$hours Hours : ${getMinutes(presentTime)} Min : ${
                    getSeconds(
                        presentTime
                    )
                } sec"
            }
        }

    }

    private fun getSeconds(presentTime: Long): String {
        val mFormat = SimpleDateFormat("ss", Locale.ENGLISH)
        val secondsStr = mFormat.format(presentTime)
        return try {
            "${(-1 * (secondsStr.trim().toInt() - 60))}"
        } catch (e: Exception) {
            "0"
        }
    }

    private fun getMinutes(presentTime: Long): String {
        val mFormat = SimpleDateFormat("mm", Locale.ENGLISH)
        val minutesStr = mFormat.format(presentTime)
        return try {
            "${(-1 * (((minutesStr.trim().toInt()) - 60)))}"
        } catch (e: Exception) {
            "0"
        }
    }

}