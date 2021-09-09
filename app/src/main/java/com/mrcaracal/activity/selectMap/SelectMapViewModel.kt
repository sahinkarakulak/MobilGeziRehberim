package com.mrcaracal.activity.selectMap

import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrcaracal.utils.Constants

class SelectMapViewModel : ViewModel() {
    var selectMapState: MutableLiveData<SelectMapViewState> = MutableLiveData<SelectMapViewState>()

    fun checkLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), Constants.LOCATION_PERMISSON_CODE
        )
    }

}

sealed class SelectMapViewState