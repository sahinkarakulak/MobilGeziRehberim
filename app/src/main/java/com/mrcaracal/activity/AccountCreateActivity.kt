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
import com.mrcaracal.modul.UserInfo

private const val TAG = "AccountCreateActivity"

class AccountCreateActivity : AppCompatActivity() {

    lateinit var userInfo: UserInfo
    lateinit var edt_userName: EditText
    lateinit var edt_userEmail: EditText
    lateinit var edt_userPassOne: EditText
    lateinit var edt_userPassTwo: EditText
    lateinit var userName: String
    lateinit var email: String
    lateinit var passOne: String
    lateinit var passTwo: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        edt_userName = findViewById(R.id.edt_userName)
        edt_userEmail = findViewById(R.id.edt_userEmail)
        edt_userPassOne = findViewById(R.id.edt_userPassOne)
        edt_userPassTwo = findViewById(R.id.edt_userPassTwo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_create)
        init()
        title = getString(R.string.account_create)
    }

    fun btn_createAccount(view: View?) {
        userName = edt_userName.text.toString()
        email = edt_userEmail.text.toString()
        passOne = edt_userPassOne.text.toString()
        passTwo = edt_userPassTwo.text.toString()

        if (userName == "" || email == "" || passOne == "" || passTwo == "") {
            toast("Gerekli alanları doldurunuz")
        } else {
            if (passOne == passTwo) {
                firebaseAuth
                    .createUserWithEmailAndPassword(email, passOne)
                    .addOnSuccessListener {
                        firebaseAuth
                            .getCurrentUser()
                            ?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                toast("Doğrulama bağlantısı E-Posta adresinize gönderildi.")
                                userInfo = UserInfo(
                                    userName,
                                    email,
                                    passOne,
                                    "MGR'i Seviyorum",
                                    "https://firebasestorage.googleapis.com/v0/b/mobilgezirehberim-7aca5.appspot.com/o/Resimler%2Fdefaultpp.png?alt=media&token=97fe9138-0aad-4ea9-af78-536c637b3be4"
                                )
                                val documentReference = firebaseFirestore
                                    .collection("Kullanicilar")
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
                toast("Parolalar uyuşmuyor. Lütfen kontrol ediniz...")
        }
    }
}