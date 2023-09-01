package com.android.libramanage.data.entities

import com.google.gson.annotations.SerializedName

data class NearbySearchResponse(
    @SerializedName("results") private val _results: List<PlaceModel>? = null,
    val status: String? = null,
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("next_page_token") val nextPageToken: String? = null
    ) {
  val results: List<PlaceModel>
    get() = _results ?: emptyList()
}
