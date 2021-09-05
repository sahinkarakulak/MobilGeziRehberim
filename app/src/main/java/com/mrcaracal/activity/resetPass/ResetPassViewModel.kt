package com.mrcaracal.activity.resetPass

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ResetPassViewModel : ViewModel() {

    var resetPassState: MutableLiveData<ResetPassViewState> = MutableLiveData<ResetPassViewState>()
    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun sendRequest(email: String) {
        if (email.isEmpty()) {
            resetPassState.value = ResetPassViewState.ShowRequiredFieldsMessage
        } else {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    resetPassState.value = ResetPassViewState.ShowCheckEmailMessage
                    resetPassState.value = ResetPassViewState.OpenLoginActivity
                }.addOnFailureListener { exception ->
                    resetPassState.value =
                        ResetPassViewState.ShowErrorMessage(exception = exception)
                }
        }
    }
}

sealed class ResetPassViewState {
    object ShowRequiredFieldsMessage : ResetPassViewState()
    object ShowCheckEmailMessage : ResetPassViewState()
    object OpenLoginActivity : ResetPassViewState()

    data class ShowErrorMessage(val exception: Exception) : ResetPassViewState()
}