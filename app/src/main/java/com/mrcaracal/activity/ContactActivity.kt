package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.MyArrayList

class ContactActivity : AppCompatActivity() {

    lateinit var edt_contactSubjectTitle: EditText
    lateinit var edt_contactMessage: EditText
    lateinit var btn_contactSend: Button

    private fun init() {
        edt_contactSubjectTitle = findViewById(R.id.edt_contactSubjectTitle)
        edt_contactMessage = findViewById(R.id.edt_contactMessage)
        btn_contactSend = findViewById(R.id.btn_contactSend)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        init()
        title = getString(R.string.contact)
        btn_contactSend.setOnClickListener {
            val str_subject = edt_contactSubjectTitle.text.toString()
            val str_message = edt_contactMessage.text.toString()
            val contactInfo = MyArrayList()
            if (str_subject == "" || str_message == "") {
                toast(getString(R.string.fill_in_the_required_fields))
            } else {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, contactInfo.admin_account)
                intent.putExtra(Intent.EXTRA_SUBJECT, str_subject)
                intent.putExtra(Intent.EXTRA_TEXT, str_message)
                intent.type = "plain/text"
                startActivity(Intent.createChooser(intent, getString(R.string.fill_in_the_required_fields)))
            }
        }
    }

}