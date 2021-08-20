package com.mrcaracal.activity.accountCreate

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.UserInfo

class AccountCreateViewModel : ViewModel() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val FIREBASE_COLLECTION_NAME = "Kullanicilar"
    private val DEFAULT_PP_LINK =
        "https://firebasestorage.googleapis.com/v0/b/mobilgezirehberim-7aca5.appspot.com/o/Resimler%2Fdefaultpp.png?alt=media&token=97fe9138-0aad-4ea9-af78-536c637b3be4"

    fun createAccount(
        userName: String,
        email: String,
        pass1: String,
        pass2: String,
        context: Context
    ) {
        if (userName == "" || email == "" || pass1 == "" || pass2 == "") {
            Toast.makeText(
                context,
                R.string.fill_in_the_required_fields.toString(),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (pass1 == pass2) {
                firebaseAuth
                    .createUserWithEmailAndPassword(email, pass1)
                    .addOnSuccessListener {
                        firebaseAuth
                            .currentUser
                            ?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    R.string.verification_link_sent.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                        val intent =
                                            Intent(context, Login::class.java)
                                        startActivity(context, intent, null)
                                        firebaseAuth.signOut()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            e.localizedMessage.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    e.localizedMessage.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            } else
                Toast.makeText(
                    context,
                    R.string.passwords_are_not_the_same.toString(),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }
}