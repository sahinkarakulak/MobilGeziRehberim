package com.mrcaracal.mobilgezirehberim.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    var loginState: MutableLiveData<LoginViewSate> = MutableLiveData<LoginViewSate>()
    private var firebaseAuth: FirebaseAuth? = null

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            loginState.value = LoginViewSate.ShowRequiredFieldsMessage
        } else {
            firebaseAuth!!.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    if (firebaseAuth?.currentUser!!.isEmailVerified) {
                        loginState.value = LoginViewSate.OpenHomePageActivity
                    } else {
                        loginState.value = LoginViewSate.ConfirmEmail
                    }
                }.addOnFailureListener { e ->
                    loginState.value = LoginViewSate.ShowExceptionMessage(exception = e)
                }
        }
    }

    fun userIsAlreadyLoggedIn() {
        val firebaseUser = firebaseAuth?.currentUser
        if (firebaseUser != null) {
            loginState.value = LoginViewSate.OpenHomePageActivity
        }
    }
}

sealed class LoginViewSate {
    object ConfirmEmail : LoginViewSate()
    object ShowRequiredFieldsMessage : LoginViewSate()
    object OpenHomePageActivity : LoginViewSate()

    data class ShowExceptionMessage(val exception: Exception) : LoginViewSate()
}