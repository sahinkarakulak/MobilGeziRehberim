package com.mrcaracal.activity.contact

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.MyArrayList

class ContactViewModel: ViewModel() {

    fun sendMessage(subject: String, message: String, context: Context){
        val contactInfo = MyArrayList()
        if (subject == "" || message == "") {
            Toast.makeText(context, R.string.fill_in_the_required_fields, Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_EMAIL, contactInfo.admin_account)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.type = "plain/text"

            startActivity(context, Intent.createChooser(intent,
                R.string.what_would_u_like_to_send_with.toString()
            ), null)
        }
    }
}