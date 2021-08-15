package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityContactBinding
import com.mrcaracal.modul.MyArrayList

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = getString(R.string.contact)
        binding.btnContactSend.setOnClickListener {
            val str_subject = binding.edtContactSubjectTitle.text.toString()
            val str_message = binding.edtContactMessage.text.toString()
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