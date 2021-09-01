package com.mrcaracal.activity.selectMap

import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.IOException

class SelectMapViewModel : ViewModel() {
    var selectMapState: MutableLiveData<SelectMapViewState> = MutableLiveData<SelectMapViewState>()

}

sealed class SelectMapViewState