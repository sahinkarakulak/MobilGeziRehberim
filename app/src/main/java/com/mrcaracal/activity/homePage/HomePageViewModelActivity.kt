package com.mrcaracal.activity.homePage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class HomePageViewModelActivity : ViewModel() {

    var homePageActivityState: MutableLiveData<HomePageActivityViewState> =
        MutableLiveData<HomePageActivityViewState>()
    lateinit var firebaseAuth: FirebaseAuth

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

}

sealed class HomePageActivityViewState