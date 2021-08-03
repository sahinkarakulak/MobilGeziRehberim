package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.UserInfo

private const val TAG = "AccountCreateActivity"

class AccountCreateActivity : AppCompatActivity() {

    var userInfo: UserInfo? = null
    var edt_userName: EditText? = null
    var edt_userEmail: EditText? = null
    var edt_userPassOne: EditText? = null
    var edt_userPassTwo: EditText? = null
    var userName: String? = null
    var email: String? = null
    var passOne: String? = null
    var passTwo: String? = null
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
        title = "Hesap Oluştur"
    }

    fun btn_createAccount(view: View?) {
        userName = edt_userName!!.text.toString()
        email = edt_userEmail!!.text.toString()
        passOne = edt_userPassOne!!.text.toString()
        passTwo = edt_userPassTwo!!.text.toString()

        if (userName == "" || email == "" || passOne == "" || passTwo == "") {
            Toast.makeText(this, "Gerekli alanları doldurunuz...", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "btn_hesabiOlustur: EditText'en boşveriler çekildi")
        } else {
            if (passOne == passTwo) {
                firebaseAuth
                    .createUserWithEmailAndPassword(email!!, passOne!!)
                    .addOnSuccessListener {
                        firebaseAuth
                            .getCurrentUser()
                            ?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                Toast.makeText(
                                    this@AccountCreateActivity,
                                    "Doğrulama bağlantısı E-Posta adresinize gönderildi.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i(
                                    TAG,
                                    "onSuccess: Doğrulama baplantııs E-Posta adresine gönderildi"
                                )
                                userInfo = UserInfo(
                                    userName,
                                    email,
                                    passOne,
                                    "MGR'i Seviyorum",
                                    "https://firebasestorage.googleapis.com/v0/b/mobilgezirehberim-7aca5.appspot.com/o/Resimler%2Fdefaultpp.png?alt=media&token=97fe9138-0aad-4ea9-af78-536c637b3be4"
                                )
                                val documentReference = firebaseFirestore
                                    .collection("Kullanicilar")
                                    .document(email!!)
                                documentReference
                                    .set(userInfo!!)
                                    .addOnSuccessListener {
                                        val intent =
                                            Intent(this@AccountCreateActivity, Login::class.java)
                                        startActivity(intent)
                                        finish()
                                        firebaseAuth!!.signOut()
                                        Log.i(
                                            TAG,
                                            "onSuccess: Kayıttan sonra kullanıcı Giris'e gönderildi"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this@AccountCreateActivity,
                                            e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.i(TAG, "onFailure: " + e.message)
                                    }
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(
                                    this@AccountCreateActivity, """
                         Beklenmedik bir hata gerçekleşti
                         ${e.message}
                         """.trimIndent(), Toast.LENGTH_SHORT
                                ).show()
                                Log.i(TAG, "onFailure: " + e.message)
                            }
                    }
            } else Toast.makeText(
                this,
                "Parolalar uyuşmuyor. Lütfen kontrol ediniz...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val TAG = "HesapOlusturma"
    }
}