package com.mrcaracal.activity.accountCreate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.UserInfo
import com.mrcaracal.utils.Constants
import com.mrcaracal.utils.ConstantsFirebase

class AccountCreateViewModel : ViewModel() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    var accountCreateState: MutableLiveData<AccountCreateViewState> =
        MutableLiveData<AccountCreateViewState>()

    fun init(){
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

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
                                    userName,
                                    email,
                                    pass1,
                                    ConstantsFirebase.I_LOVE_MGR,
                                    ConstantsFirebase.DEFAULT_PP_LINK
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
                                            AccountCreateViewState.ShowErrorMessage(exception = exception)
                                    }
                            }
                            ?.addOnFailureListener { exception ->
                                accountCreateState.value =
                                    AccountCreateViewState.ShowErrorMessage(exception = exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        AccountCreateViewState.ShowErrorMessage(exception = exception)
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
        data class ShowErrorMessage(val exception: Exception) : AccountCreateViewState()
        object ThePassIsNotTheSame : AccountCreateViewState()
    }
}