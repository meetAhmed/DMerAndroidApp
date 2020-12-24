package com.d.mer.dataModels

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class SharedImageModel(
    var date: Long = 0L,
    var user: String = "",
    var username: String = "",
    var imageUrl: String = ""
)