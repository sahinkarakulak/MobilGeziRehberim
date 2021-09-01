package com.mrcaracal.activity.selectMap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SelectMapViewModel : ViewModel() {
    var selectMapState: MutableLiveData<SelectMapViewState> = MutableLiveData<SelectMapViewState>()

}

sealed class SelectMapViewState