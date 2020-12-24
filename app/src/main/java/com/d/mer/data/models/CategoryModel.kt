package com.d.mer.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CategoryModel(
    var id: Long = 0,
    var name: String = ""
) {
    companion object {
        fun isPresent(
            listOfCategories: List<CategoryModel>,
            categoryId: Long
        ): Boolean {
            for (i in listOfCategories.indices) {
                if (listOfCategories[i].id == categoryId) {
                    return true
                }
            }
            return false
        }
    }
}