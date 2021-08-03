package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R

private const val TAG = "ResetPassActivity"

class ResetPassActivity : AppCompatActivity() {

    var edt_resetPass: EditText? = null
    var firebaseAuth: FirebaseAuth? = null

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        edt_resetPass = findViewById(R.id.edt_resetPass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parola_sifirlama)
        init()
        title = "Parola Sıfırla"
    }

    // Kullanıcının girdiği E-Posta adresine parola sıfırlama bağlantısı gönderilecektir.
    fun btn_sendRequest(view: View?) {
        // parola sıfırlama işlemi için gereken işlemler yapılsın
        val email = edt_resetPass!!.text.toString()
        if (email == "") {
            Toast.makeText(this, "Gerekli alanı doldurunuz", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "istegiGonder: EditText'en boş veriler alındı")
        } else {
            firebaseAuth!!.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@ResetPassActivity,
                        "E-Postanızı kontorl ediniz",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@ResetPassActivity, Login::class.java)
                    startActivity(intent)
                    finish()
                    Log.i(
                        TAG,
                        "onSuccess: Sıfırlama isteği gönderildi ve kullanıcı Giris'e yönlendirilidi"
                    )
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@ResetPassActivity,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    companion object {
        private const val TAG = "ParolaSifirlama"
    }
}