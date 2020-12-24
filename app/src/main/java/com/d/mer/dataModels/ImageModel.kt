package com.d.mer.dataModels

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ImageModel(
    var nodeAddress: String = "",
    var image_url: String = "",
    var text: String = "",
    var skip: Int = -99,
    var timer: Long = 0L,
    var winner: String = "",
    var categories: ArrayList<Long>? = null
)