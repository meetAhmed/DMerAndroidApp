package com.d.mer.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserModel(
    var email: String = "",
    var name: String = "",
    var password: String = "",
    var filteredCategories: String = ""
)