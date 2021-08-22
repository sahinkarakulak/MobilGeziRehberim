package com.mrcaracal.activity.accountCreate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.UserInfo

class AccountCreateViewModel : ViewModel() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    var accountCreateState: MutableLiveData<AccountCreateViewState> =
        MutableLiveData<AccountCreateViewState>()

    private val FIREBASE_COLLECTION_NAME = "Kullanicilar"
    private val DEFAULT_PP_LINK =
        "https://firebasestorage.googleapis.com/v0/b/mobilgezirehberim-7aca5.appspot.com/o/Resimler%2Fdefaultpp.png?alt=media&token=97fe9138-0aad-4ea9-af78-536c637b3be4"

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
                                //
                                val userInfo = UserInfo(
                                    userName,
                                    email,
                                    pass1,
                                    R.string.ı_love_mgr.toString(),
                                    DEFAULT_PP_LINK
                                )
                                val documentReference = firebaseFirestore
                                    .collection(FIREBASE_COLLECTION_NAME)
                                    .document(email)
                                documentReference
                                    .set(userInfo)
                                    .addOnSuccessListener {
                                        accountCreateState.value =
                                            AccountCreateViewState.CreateAccountAndSignOut
                                        firebaseAuth.signOut()
                                    }
                                    .addOnFailureListener { e ->
                                        accountCreateState.value =
                                            AccountCreateViewState.ShowErrorMessage(e = e)
                                    }
                            }
                            ?.addOnFailureListener { e ->
                                accountCreateState.value =
                                    AccountCreateViewState.ShowErrorMessage(e = e)
                            }
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
        data class ShowErrorMessage(val e: Exception) : AccountCreateViewState()
        object ThePassIsNotTheSame : AccountCreateViewState()
    }
}