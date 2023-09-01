package com.libra.activitys;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.libra.R;
import com.libra.fragments.GeoLocationFragment;

public abstract class GeoLocationActivity extends ToolbarHostActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final long INTERVAL_LOCATION_UPDATE = 10 * 1000;
    private static final long INTERVAL_FAST_TEST = 10 * 1000;
    private static final float DISTANCE_LOCATION_UPDATE = 100;

    private static final int LOCATION_PERMISSIONS = 101;
    private static final int LOCATION_REQUEST = 102;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private boolean isRequestingLocation;
    private boolean shouldRequestPermissionsAndGps = false;
    private boolean isLocationGranted = false;
    private boolean isReRequest = true;

    protected void setShouldRequestPermissionsAndGps(boolean should) {
        this.shouldRequestPermissionsAndGps = should;
    }

    protected abstract GeoLocationFragment getGeoLocationFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = buildGoogleApiClient();
        mLocRequest = createLocationRequest();
        locationSettingsRequest = getLocationSettingsRequest();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST) {
            handleRequestResult(resultCode == RESULT_OK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSIONS) {
            boolean granted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    granted = false;
                    break;
                }
            }
            handleRequestResult(granted);
        }
    }

    public Boolean isLocationGranted() {
        return isLocationGranted;
    }

    public void repeatLocationRequest() {
        shouldRequestPermissionsAndGps = true;
        checkLocationSettings();
    }

    private void handleRequestResult(boolean granted) {
        if (granted) {
            checkLocationSettings();
        } else {
            onLocationSettingsDenied();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClientExecuteConnection();
    }

    private void googleApiClientExecuteConnection() {
        if (mGoogleApiClient == null) return;
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        googleApiClientRefuseConnection();
        super.onStop();
    }

    private void googleApiClientRefuseConnection() {
        if (mGoogleApiClient == null) return;
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private LocationRequest createLocationRequest() {
        return LocationRequest.create().setInterval(INTERVAL_LOCATION_UPDATE)
                .setFastestInterval(INTERVAL_FAST_TEST)
                .setSmallestDisplacement(DISTANCE_LOCATION_UPDATE)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private LocationSettingsRequest getLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(createLocationRequest());
        return builder.build();
    }

    private void checkLocationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isPermissionsGranted()) {
                if (shouldRequestPermissionsAndGps) {

//                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
//                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                        builder.setMessage(getString(R.string.background_message));
//                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                requestPermissions();
//                            }
//                        });
//                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        builder.show();
//                    }else {
                        requestPermissions();
//                    }
                } else {
                    shouldRequestPermissionsAndGps = false;
                    onLocationSettingsDenied();
                }
                return;
            } else shouldRequestPermissionsAndGps = false;
        }else{
            requestPermissions();
        }

        checkLocationEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isPermissionsGranted() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//                    && (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED);
        }else {
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ;
        }
    }

    private void requestPermissions() {
        String[] permission;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            permission = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            };// Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }else{
            permission = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        }


        ActivityCompat.requestPermissions(this,permission , LOCATION_PERMISSIONS);
    }

    private void checkLocationEnabled() {
        PendingResult result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequest);
        result.setResultCallback(new ResultCallback() {
            @Override
            public void onResult(@NonNull Result result) {
                Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        isLocationGranted = true;
                        getGeoLocationFragment().locationRequestingSuccess();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            if (shouldRequestPermissionsAndGps) {
                                status.startResolutionForResult(GeoLocationActivity.this, LOCATION_REQUEST);
                            } else {
                                getGeoLocationFragment().locationRequestingSuccess();
                            }
                        } catch (IntentSender.SendIntentException e) {
                            onLocationSettingsDenied();
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        onLocationSettingsDenied();
                        break;
                }
            }
        });
    }

    private void onLocationSettingsDenied() {

//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage(getString(R.string.background_message));
//            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    requestPermissions();
//                }
//            });
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            builder.show();
//        }else {
            getGeoLocationFragment().locationDenied();
//        }
    }

    private void stopLocationUpdates() {
        isRequestingLocation = false;
    }


    public Location getCurrentLocation() {
        return null;
    }

    public GeoLocation getCurrentGeoLocation() {
        if (getCurrentLocation() == null) {
            return null;
        }
        return new GeoLocation(getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude());
    }

    @Override
    public void onConnected(Bundle bundle) {
        checkLocationSettings();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
