package com.libra.firebase

import android.util.Log
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.util.GeoUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.libra.Const
import com.libra.entity.Cafe
import com.libra.entity.CafeRoom
import com.libra.entity.User
import io.reactivex.BackpressureStrategy
import io.reactivex.BackpressureStrategy.LATEST
import io.reactivex.Flowable

/**
 * @author Alex on 20.01.2019.
 */
object RxFirebaseWrapper {

  fun runCafesGeoQueryByLocation(geoLocation: GeoLocation): Flowable<Pair<GeoLocation, Map<String, GeoLocation>>> {
    return Flowable.create({ e ->
      Log.e("TAGS","geoquery is "+(geoLocation==null))
      val geoQuery = FBHelper.getLocationsCafe()
          .queryAtLocation(geoLocation, Const.RADIUS_NEARBY_CAFE_IN_KM.toDouble())
      val map = hashMapOf<String, GeoLocation>()

      val listener: GeoQueryListener = object : GeoQueryListener() {
        override fun onKeyEntered(key: String, location: GeoLocation) {
          map[key] = location
        }

        override fun onGeoQueryReady() {
          geoQuery.removeGeoQueryEventListener(this)
          e.onNext(geoLocation to map)
        }

        override fun onGeoQueryError(error: DatabaseError) {
          geoQuery.removeGeoQueryEventListener(this)
          Log.e("TAGS","error is "+error.message)
          e.onError(error.toException())
        }
      }
      geoQuery.addGeoQueryEventListener(listener)

      e.setCancellable { geoQuery.removeAllListeners() }
    }, BackpressureStrategy.LATEST)
  }

  fun getCafeListByGeoQueryResult(cafesLocationData: Pair<GeoLocation, Map<String, GeoLocation>>): Flowable<List<Cafe>> {
    return Flowable.create({ e ->
      val (userLocation, map) = cafesLocationData
      val fbRef = FBHelper.getCafe()

      val listener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val cafes = mutableListOf<Cafe>()
          map.entries.forEach { entry ->
            dataSnapshot.child(entry.key).getValue(Cafe::class.java)?.let {
              it.setDataFromEntry(entry, userLocation)
              cafes.add(it)
            }
          }
          e.onNext(cafes)
        }

        override fun onCancelled(p0: DatabaseError) {
          fbRef.removeEventListener(this)
          e.onError(p0.toException())
        }
      }
      fbRef.addListenerForSingleValueEvent(listener)

      e.setCancellable { fbRef.removeEventListener(listener) }
    }, LATEST)
  }

  fun getCafeRoomsListByGeoQueryResult(cafesLocationData: Pair<GeoLocation, Map<String, GeoLocation>>): Flowable<List<CafeRoom>> {
    return Flowable.create({ e ->
      val (userLocation, map) = cafesLocationData
      val fbRef = FBHelper.getCafeRooms()

      val listener = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val cafeRooms = mutableListOf<CafeRoom>()
          map.entries.forEach { entry ->
            val room = CafeRoom()
            val cafeName = dataSnapshot.child(entry.key).child(Cafe.NAME).value.toString()
            room.id = entry.key
            room.name = cafeName

            val users = dataSnapshot.child(entry.key).child(entry.key).child(FBHelper.USERS)
            for (snapshot in users.children) {
              val user = snapshot.getValue(User::class.java)
              user?.let {
                val hoursAgo = (System.currentTimeMillis()/1000 - user.loginedTime)/60/60
                if(hoursAgo<24) room.users.add(user)
              }
            }
            room.countFemale = room.users.count { it.isFemale }
            room.distance = GeoUtils.distance(userLocation, entry.value)
            cafeRooms.add(room)
          }

          e.onNext(cafeRooms)
        }

        override fun onCancelled(p0: DatabaseError) {
          fbRef.removeEventListener(this)
          e.onError(p0.toException())
        }
      }
      fbRef.addValueEventListener(listener)

      e.setCancellable { fbRef.removeEventListener(listener) }
    }, LATEST)
  }
}