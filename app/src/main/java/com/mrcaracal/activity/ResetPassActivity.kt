package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R

class ResetPassActivity : AppCompatActivity() {

    lateinit var edt_resetPass: EditText
    lateinit var firebaseAuth: FirebaseAuth

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        edt_resetPass = findViewById(R.id.edt_resetPass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parola_sifirlama)
        init()
        title = getString(R.string.reset_pass)
    }

    // Kullanıcının girdiği E-Posta adresine parola sıfırlama bağlantısı gönderilecektir.
    fun btn_sendRequest(view: View?) {
        // parola sıfırlama işlemi için gereken işlemler yapılsın
        val email = edt_resetPass.text.toString()
        if (email == "") {
            toast(R.string.fill_in_the_required_fields.toString())
        } else {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    toast(R.string.check_your_e_mail.toString())
                    val intent = Intent(this@ResetPassActivity, Login::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener { e ->
                    toast(e.localizedMessage)
                }
        }
    }

}