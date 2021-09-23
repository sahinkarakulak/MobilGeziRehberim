package com.mrcaracal.activity.accountCreate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.modul.UserInfo
import com.mrcaracal.utils.ConstantsFirebase

class AccountCreateViewModel : ViewModel() {
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var accountCreateState: MutableLiveData<AccountCreateViewState> =
        MutableLiveData<AccountCreateViewState>()

    fun createAccount(
        userName: String,
        email: String,
        pass1: String,
        pass2: String
    ) {
        if (userName.isEmpty() || email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            accountCreateState.value = AccountCreateViewState.ShowRequiredFieldsMessage
        } else {
            if (pass1 == pass2) {
                firebaseAuth
                    .createUserWithEmailAndPassword(email, pass1)
                    .addOnSuccessListener {
                        firebaseAuth
                            .currentUser
                            ?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                val userInfo = UserInfo(
                                    kullaniciAdi = userName,
                                    kullaniciEposta = email,
                                    kulaniciParola = pass1,
                                    bio = ConstantsFirebase.I_LOVE_MGR,
                                    kullaniciResmi = ConstantsFirebase.DEFAULT_PP_LINK
                                )
                                val documentReference = firebaseFirestore
                                    .collection(ConstantsFirebase.FIREBASE_COLLECTION_NAME)
                                    .document(email)
                                documentReference
                                    .set(userInfo)
                                    .addOnSuccessListener {
                                        accountCreateState.value =
                                            AccountCreateViewState.CreateAccountAndSignOut
                                        firebaseAuth.signOut()
                                    }
                                    .addOnFailureListener { exception ->
                                        accountCreateState.value =
                                            AccountCreateViewState.ShowExceptionMessage(exception = exception)
                                    }
                            }
                            ?.addOnFailureListener { exception ->
                                accountCreateState.value =
                                    AccountCreateViewState.ShowExceptionMessage(exception = exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        AccountCreateViewState.ShowExceptionMessage(exception = exception)
                    }
            } else {
                accountCreateState.value =
                    AccountCreateViewState.ThePassIsNotTheSame
            }
        }
    }

    sealed class AccountCreateViewState {
        object ShowRequiredFieldsMessage : AccountCreateViewState()
        object CreateAccountAndSignOut : AccountCreateViewState()
        object ThePassIsNotTheSame : AccountCreateViewState()

        data class ShowExceptionMessage(val exception: Exception) : AccountCreateViewState()
    }
}