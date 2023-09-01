package com.libra.ui.home.cafeslist.viewmodels

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationRequest
import com.libra.base.BaseViewModel
import com.libra.entity.Cafe
import com.libra.firebase.RxFirebaseWrapper
import com.libra.tools.addTo
import com.patloew.rxlocation.RxLocation
import io.reactivex.BackpressureStrategy.LATEST
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

/**
 * @author Alex on 20.01.2019.
 */
class CafeListViewModel : BaseViewModel() {

  val cafeList = MutableLiveData<List<Cafe>>()
  private var cafesDisposable: Disposable? = null
  private var rxLocation: RxLocation? = null

  fun init(rxLocation: RxLocation) {
    this.rxLocation = rxLocation
  }

  fun requestCafesList() {
    if (rxLocation == null) return
    cafesDisposable?.dispose()
    cafesDisposable = getGeoLocationFlowable(rxLocation!!)
        .switchMap(RxFirebaseWrapper::runCafesGeoQueryByLocation)
        .switchMap(::requestCafeList)
        .subscribe({ cafeList.postValue(it) }, { cafeList.postValue(emptyList()) })
    cafesDisposable?.addTo(disposables)
  }

  @SuppressLint("MissingPermission")
  public fun getGeoLocationFlowable(rxLocation: RxLocation): Flowable<GeoLocation> {
    return rxLocation.location().updates(getLocationRequest(), LATEST)
        .mergeWith(rxLocation.location().lastLocation())
        .map { GeoLocation(it.latitude, it.longitude) }
  }

  private fun getLocationRequest(): LocationRequest {
    return LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(TimeUnit.SECONDS.toMillis(10))
        .setSmallestDisplacement(50f)
  }

  private fun requestCafeList(locationData: Pair<GeoLocation, Map<String, GeoLocation>>): Flowable<List<Cafe>> {
    val cafeList = RxFirebaseWrapper.getCafeListByGeoQueryResult(locationData)
    val cafeRooms = RxFirebaseWrapper.getCafeRoomsListByGeoQueryResult(locationData)
    return Flowable.combineLatest(cafeList, cafeRooms, BiFunction { cafes, rooms ->
      cafes.forEach { cafe ->
        rooms.firstOrNull { it.id == cafe.id }?.let {
          cafe.users = it.users
          cafe.femaleCount = it.countFemale
          cafe.maleCount = it.countMale
        }
      }
      cafes
    })
  }

}