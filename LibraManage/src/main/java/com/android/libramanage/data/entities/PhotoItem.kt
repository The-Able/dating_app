package com.android.libramanage.data.entities

import com.google.gson.annotations.SerializedName

data class PhotoItem(
	@SerializedName("photo_reference") val photoReference: String,
	val width: Int,
	val height: Int
)
