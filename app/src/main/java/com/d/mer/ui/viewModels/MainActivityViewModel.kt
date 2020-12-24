package com.d.mer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.d.mer.data.firestore.FireStoreReferences

class MainActivityViewModel : ViewModel() {

    fun getCategories() = FireStoreReferences.categoriesCollection

    fun getImages() = FireStoreReferences.imagesCollection

    fun categoriesForImageCollection(nodeAddress: String) =
        FireStoreReferences.categoriesForImageCollection(nodeAddress)

    fun getUserData(nodeAddress: String) = FireStoreReferences.getUserData(nodeAddress)

}