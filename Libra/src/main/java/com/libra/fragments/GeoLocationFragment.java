package com.libra.fragments;

import android.content.Context;
import android.location.Location;

import com.libra.activitys.GeoLocationActivity;
import com.firebase.geofire.GeoLocation;


public abstract class GeoLocationFragment extends BaseHostedFragment {

    private GeoLocationActivity mLocationActivity;

    public abstract void locationRequestingSuccess();
    public abstract void locationDenied();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLocationActivity = (GeoLocationActivity) context;
    }

    protected Location getCurrentLocation() {
        return mLocationActivity.getCurrentLocation();
    }

    protected GeoLocation getCurrentGeoLocation() {
        return mLocationActivity == null ? null : mLocationActivity.getCurrentGeoLocation();
    }

    protected Boolean isLocationGranted() {
        return mLocationActivity.isLocationGranted();
    }

    protected void requestLocationAgain() {
        mLocationActivity.repeatLocationRequest();
    }

    @Override
    public void onDetach() {
        mLocationActivity = null;
        super.onDetach();
    }
}
