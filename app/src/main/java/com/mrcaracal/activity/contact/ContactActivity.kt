package com.mrcaracal.activity.contact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding
    private lateinit var viewModel: ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = getString(R.string.contact)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        binding.btnContactSend.setOnClickListener {
            val str_subject = binding.edtContactSubjectTitle.text.toString()
            val str_message = binding.edtContactMessage.text.toString()
            viewModel.sendMessage(str_subject, str_message, it.context)

        }
    }
}