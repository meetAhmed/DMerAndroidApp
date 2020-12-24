package com.d.mer.interfaces

import com.d.mer.dataModels.ImageModel

interface ImagesClickListener {
    fun shareImage(imageModel: ImageModel)
    fun endTimer(imageModel: ImageModel, position: Int)
}