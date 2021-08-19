package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityParolaSifirlamaBinding

class ResetPassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParolaSifirlamaBinding
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParolaSifirlamaBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        firebaseAuth = FirebaseAuth.getInstance()
        title = getString(R.string.reset_pass)
    }

    // Kullanıcının girdiği E-Posta adresine parola sıfırlama bağlantısı gönderilecektir.
    fun btn_sendRequest(view: View?) {
        // parola sıfırlama işlemi için gereken işlemler yapılsın
        val email = binding.edtResetPass.text.toString()
        if (email == "") {
            toast(getString(R.string.fill_in_the_required_fields))
        } else {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    toast(getString(R.string.check_your_e_mail))
                    val intent = Intent(this@ResetPassActivity, Login::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener { e ->
                    toast(e.localizedMessage)
                }
        }
    }

}