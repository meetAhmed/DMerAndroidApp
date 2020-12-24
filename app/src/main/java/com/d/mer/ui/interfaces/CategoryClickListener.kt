package com.d.mer.ui.interfaces

import com.d.mer.data.models.CategoryModel

interface CategoryClickListener {
    fun click(categoryModel: CategoryModel)
}