package com.d.mer.interfaces

import com.d.mer.dataModels.CategoryModel

interface CategoryClickListener {
    fun click(categoryModel: CategoryModel)
}