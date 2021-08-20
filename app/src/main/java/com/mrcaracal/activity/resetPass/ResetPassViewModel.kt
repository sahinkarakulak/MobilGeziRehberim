package com.mrcaracal.activity.resetPass

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R

class ResetPassViewModel: ViewModel() {

    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun sendRequest(email: String, context: Context){
        if (email == "") {
            Toast.makeText(context, R.string.fill_in_the_required_fields, Toast.LENGTH_SHORT).show()
        } else {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(context, R.string.check_your_e_mail, Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, Login::class.java)
                    startActivity(context, intent, null)
                }.addOnFailureListener { e ->
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }
}