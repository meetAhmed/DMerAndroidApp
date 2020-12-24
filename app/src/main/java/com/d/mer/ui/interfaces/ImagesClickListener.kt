package com.d.mer.ui.interfaces

import com.d.mer.data.models.ImageModel

interface ImagesClickListener {
    fun shareImage(imageModel: ImageModel)
    fun endTimer(imageModel: ImageModel, position: Int)
}