package com.d.mer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.d.mer.data.firestore.FireStoreReferences

class WinnerActivityViewModel : ViewModel() {

    fun getImageData(nodeAddress: String) = FireStoreReferences.getImageData(nodeAddress)

}