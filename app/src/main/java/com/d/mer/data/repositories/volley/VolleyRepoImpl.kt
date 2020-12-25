package com.d.mer.data.repositories.volley

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class VolleyRepoImpl private constructor() : VolleyRepo {

    companion object {

        private var INSTANCE: VolleyRepoImpl? = null
        private var firebaseKey: String = ""
        private var context: Context? = null

        fun initialize(firebaseApiKey: String, context: Context) {
            if (INSTANCE == null) {
                INSTANCE = VolleyRepoImpl()
                firebaseKey = firebaseApiKey
                this.context = context
            }
        }

        fun get(): VolleyRepoImpl {
            return INSTANCE
                ?: throw IllegalStateException("VolleyRepoImpl must be initialized first.")
        }
    }

    override fun sendNotifications(
        data: JSONObject,
        tokens: List<String>,
        listener: VolleyRequestResponse?
    ) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val myReq: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { listener?.success() },
            Response.ErrorListener { listener?.failure(it) }) {

            override fun getBody(): ByteArray {
                val rawParameters: MutableMap<String?, Any?> = HashMap()
                rawParameters["data"] = data
                rawParameters["registration_ids"] = tokens
                return JSONObject(rawParameters).toString().toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "key=$firebaseKey"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        Volley.newRequestQueue(context).add(myReq)
    }

}