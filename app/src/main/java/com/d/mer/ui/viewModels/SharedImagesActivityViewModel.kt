package com.d.mer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.d.mer.data.firestore.FireStoreReferences

class SharedImagesActivityViewModel : ViewModel() {

    fun getImages() = FireStoreReferences.imagesCollection

    fun userSharedImagesCollection(nodeAddress: String) =
        FireStoreReferences.userSharedImagesCollection(nodeAddress)

}