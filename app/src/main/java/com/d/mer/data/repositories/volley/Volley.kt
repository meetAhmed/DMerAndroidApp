package com.d.mer.data.repositories.volley

import org.json.JSONObject

interface VolleyRepo {

    fun sendNotifications(
        data: JSONObject,
        tokens: List<String>,
        listener: VolleyRequestResponse? = null
    )

}

interface VolleyRequestResponse {
    fun success()
    fun failure(exception: Exception)
}