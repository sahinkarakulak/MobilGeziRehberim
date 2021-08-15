package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityAccountCreateBinding
import com.mrcaracal.modul.UserInfo

class AccountCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountCreateBinding

    lateinit var userInfo: UserInfo
    lateinit var userName: String
    lateinit var email: String
    lateinit var passOne: String
    lateinit var passTwo: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val FIREBASE_COLLECTION_NAME = "Kullanicilar"
    private val DEFAULT_PP_LINK = "https://firebasestorage.googleapis.com/v0/b/mobilgezirehberim-7aca5.appspot.com/o/Resimler%2Fdefaultpp.png?alt=media&token=97fe9138-0aad-4ea9-af78-536c637b3be4"

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        title = getString(R.string.account_create)
    }

    fun btn_createAccount(view: View?) {
        userName = binding.edtUserName.text.toString()
        email = binding.edtUserEmail.text.toString()
        passOne = binding.edtUserPassOne.text.toString()
        passTwo = binding.edtUserPassTwo.text.toString()

        if (userName == "" || email == "" || passOne == "" || passTwo == "") {
            toast(getString(R.string.fill_in_the_required_fields))
        } else {
            if (passOne == passTwo) {
                firebaseAuth
                    .createUserWithEmailAndPassword(email, passOne)
                    .addOnSuccessListener {
                        firebaseAuth
                            .getCurrentUser()
                            ?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                toast(getString(R.string.verification_link_sent))
                                userInfo = UserInfo(
                                    userName,
                                    email,
                                    passOne,
                                    getString(R.string.ı_love_mgr),
                                    DEFAULT_PP_LINK
                                )
                                val documentReference = firebaseFirestore
                                    .collection(FIREBASE_COLLECTION_NAME)
                                    .document(email)
                                documentReference
                                    .set(userInfo)
                                    .addOnSuccessListener {
                                        val intent =
                                            Intent(this@AccountCreateActivity, Login::class.java)
                                        startActivity(intent)
                                        finish()
                                        firebaseAuth.signOut()
                                    }
                                    .addOnFailureListener { e ->
                                        toast(e.localizedMessage)
                                    }
                            }
                            ?.addOnFailureListener { e ->
                                toast(e.localizedMessage)
                            }
                    }
            } else
                toast(getString(R.string.passwords_are_not_the_same))
        }
    }
}