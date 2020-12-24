package com.d.mer.dataModels

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserModel(
    var email: String = "",
    var name: String = "",
    var password: String = "",
    var filteredCategories: String = ""
)