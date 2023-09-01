package com.android.libramanage.data

import com.android.libramanage.data.entities.NearbySearchResponse
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author Alex on 24.01.2019.
 */
interface PlacesService {

  companion object {
    fun get(): PlacesService = ServiceGenerator.createService()
  }

  @GET("nearbysearch/json")
  fun getNearbyPlaces(
      @Query("key") apiKey: String,
      @Query("location") location: String,
      @Query("radius") radius: Int,
      @Query("type") type: String
  ): Flowable<NearbySearchResponse>

  @GET("nearbysearch/json")
  fun getNearbyPlacesNextPage(
      @Query("key") apiKey: String,
      @Query("pagetoken") pageToken: String
  ): Flowable<NearbySearchResponse>
}