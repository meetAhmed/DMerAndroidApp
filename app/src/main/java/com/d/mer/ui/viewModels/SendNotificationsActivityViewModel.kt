package com.d.mer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.d.mer.data.firestore.FireStoreReferences
import com.d.mer.data.repositories.volley.VolleyRepo
import com.d.mer.data.repositories.volley.VolleyRepoImpl
import com.d.mer.data.repositories.volley.VolleyRequestResponse
import org.json.JSONObject

class SendNotificationsActivityViewModel : ViewModel() {

    private val volleyRepo: VolleyRepo = VolleyRepoImpl.get()

    fun sendNotifications(
        data: JSONObject,
        tokens: List<String>,
        listener: VolleyRequestResponse? = null
    ) {
        volleyRepo.sendNotifications(data, tokens, listener)
    }

    fun getUsers() = FireStoreReferences.usersCollection

}