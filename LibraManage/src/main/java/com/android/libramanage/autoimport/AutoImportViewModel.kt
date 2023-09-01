package com.android.libramanage.autoimport

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.libramanage.data.PlacesService
import com.android.libramanage.data.entities.PlaceModel
import com.android.libramanage.entity.Cafe
import com.android.libramanage.firebase.FBHelper
import com.android.libramanage.support.addTo
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/**
 * @author Alex on 24.01.2019.
 */
class AutoImportViewModel : ViewModel() {

    private lateinit var apiKey: String
    private lateinit var alreadyRegisteredPlacesIds: List<String>
    private val disposables = CompositeDisposable()
    private val placesService = PlacesService.get()
    private var type = "cafe"
    private var typeSearch = "cafe"
    private val placesToImport = mutableListOf<PlaceModel>()

    val placesData = MutableLiveData<List<PlaceModel>>()
    val finishData = MutableLiveData<Boolean>()

    fun init(apiKey: String, alreadyRegisteredPlacesIds: List<String>) {
        this.apiKey = apiKey
        this.alreadyRegisteredPlacesIds = alreadyRegisteredPlacesIds

    }

    fun onTypeUpdated(type: String) {
        this.type = type
    }

    fun importPlaces(center: LatLng, radius: Float) {
        val radiusInMeeters = (radius * 1000).toInt()
        val location = "${center.latitude},${center.longitude}"
        var pageToken: String? = null
        Flowable.interval(0, 5L, TimeUnit.SECONDS)
                .concatMap { Flowable.fromCallable { pageToken ?: "" } }
                .concatMap {
                    if (it.isNotBlank()) placesService.getNearbyPlacesNextPage(apiKey, it)
                    else

                        when (type) {
                            "Bar" -> {
                                typeSearch="bar"
                            }
                            "Restaurant" -> {
                                typeSearch="restaurant"
                            }
                            "Parks" -> {
                                typeSearch="park"
                            }
                            "Gym" -> {
                                typeSearch="gym"
                            }
                            "Office" -> {
                                typeSearch="offices"
                            }
                            "Shopping" -> {
                                typeSearch="shopping_mall"
                            }
                            "Coffee" -> {
                                typeSearch="cafe"
                            }
                        }
                        placesService.getNearbyPlaces(apiKey, location, radiusInMeeters, typeSearch)
                }
                .doOnNext { pageToken = it.nextPageToken }
                .doOnNext { placesToImport.addAll(it.results)}
                .takeWhile { it.nextPageToken != null }
                .collect({ mutableListOf<PlaceModel>() }, { list, response -> list.addAll(response.results)  })
                .subscribe({ placesData.postValue(placesToImport.map { it.copy() }) }, { it.printStackTrace(); finishData.postValue(true) })
                .addTo(disposables)
    }

    fun placeSelected(place: PlaceModel) {
        placesToImport.firstOrNull { it.place_id == place.place_id }?.let { it.isSelected = !it.isSelected }
        placesData.postValue(placesToImport.map { it.copy() })
    }

    fun selectAll(shouldSelect: Boolean) {
        placesToImport.forEach { it.isSelected = shouldSelect }
        placesData.postValue(placesToImport.map { it.copy() })
    }

    fun importConfirmed() {
        Flowable.fromCallable { placesToImport.filter { it.isSelected } }
                .flatMap(::saveCafes)
                .subscribe({ finishData.postValue(true) }, { it.printStackTrace(); finishData.postValue(true) })
                .addTo(disposables)
    }

    private fun saveCafes(places: List<PlaceModel>): Flowable<Boolean> {
        val fbCafe = FBHelper.getCafe()
        val fbCafeRooms = FBHelper.getCafeRooms()
        val fbLocations = FBHelper.getLocationsCafe()

        return Flowable.fromCallable {
            places.filter { !alreadyRegisteredPlacesIds.contains(it.place_id) }.forEach {
                val cafePush = fbCafe.push()
                val key = cafePush.key
                val cafe = Cafe(it.name, it.vicinity, it.getImageUrl(apiKey), it.place_id, type)
                val geoLoc = with(it.geometry.location) { GeoLocation(lat, lng) }
                cafePush.setValue(cafe)
                if (key != null) {
                    fbCafeRooms.child(key).setValue(mapOf(Cafe.NAME to it.name))
                }
                fbLocations.setLocation(key, geoLoc)
            }
            true
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun setPlType(placeType: String?) {
        if (placeType != null) {
            type=placeType
        }
    }

    companion object {
        const val TYPE_CAFE = "cafe"
        const val TYPE_RESTAURANT = "restaurant"
    }
}