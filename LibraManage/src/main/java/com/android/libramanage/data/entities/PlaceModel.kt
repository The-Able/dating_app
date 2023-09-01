package com.android.libramanage.data.entities

data class PlaceModel(
    val id: String?=null,
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val icon: String? = null,
    val photos: List<PhotoItem>? = null,
    val reference: String? = null,
    val types: List<String>? = null,
    val scope: String? = null,
    val openingHours: OpeningHours? = null,
    val place_id: String? = null,
    var isSelected: Boolean = false
) {
  companion object {
    private const val PHOTO_URL_TEMPLATE = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=%s&key=%s"
  }

  fun getImageUrl(apiKey: String): String? {
    return photos?.firstOrNull()?.photoReference?.let { PHOTO_URL_TEMPLATE.format(it, apiKey) }
  }
}
