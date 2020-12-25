package com.d.mer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.d.mer.data.firestore.FireStoreReferences
import com.d.mer.data.repositories.volley.VolleyRepo
import com.d.mer.data.repositories.volley.VolleyRepoImpl
import org.json.JSONObject

class MainActivityViewModel : ViewModel() {

    private val volleyRepo: VolleyRepo = VolleyRepoImpl.get()

    fun sendNotifications(
        data: JSONObject,
        token: String
    ) {
        if (token.trim().isNotEmpty()) {
            volleyRepo.sendNotifications(data, arrayListOf(token))
        }
    }

    fun getCategories() = FireStoreReferences.categoriesCollection

    fun getImages() = FireStoreReferences.imagesCollection

    fun categoriesForImageCollection(nodeAddress: String) =
        FireStoreReferences.categoriesForImageCollection(nodeAddress)

    fun getUserData(nodeAddress: String) = FireStoreReferences.getUserData(nodeAddress)

}