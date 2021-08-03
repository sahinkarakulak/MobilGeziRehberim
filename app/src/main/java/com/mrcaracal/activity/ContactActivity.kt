package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.ContactInfo

private const val TAG = "ContactActivity"

class ContactActivity : AppCompatActivity() {

    var edt_contactSubjectTitle: EditText? = null
    var edt_contactMessage: EditText? = null
    var btn_contactSend: Button? = null

    private fun init() {
        edt_contactSubjectTitle = findViewById(R.id.edt_contactSubjectTitle)
        edt_contactMessage = findViewById(R.id.edt_contactMessage)
        btn_contactSend = findViewById(R.id.btn_contactSend)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        init()
        title = "İletişim"
        btn_contactSend!!.setOnClickListener {
            val str_subject = edt_contactSubjectTitle!!.text.toString()
            val str_message = edt_contactMessage!!.text.toString()
            val contactInfo = ContactInfo()
            if (str_subject == "" || str_message == "") {
                Toast.makeText(
                    this@ContactActivity,
                    "Gerekli alanları doldurunuz",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i(TAG, "onClick: EditText'en boş veriler alındı")
            } else {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, contactInfo.admin_hesaplari)
                intent.putExtra(Intent.EXTRA_SUBJECT, str_subject)
                intent.putExtra(Intent.EXTRA_TEXT, str_message)
                intent.type = "plain/text"
                startActivity(Intent.createChooser(intent, "Ne ile göndermek istersiniz?"))
                Log.i(TAG, "onClick: E-Mail gönderildi")
            }
        }
    }

}