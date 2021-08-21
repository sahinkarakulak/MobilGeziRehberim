package com.mrcaracal.activity.contact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityContactBinding
import com.mrcaracal.utils.IntentProcessor

class ContactActivity : AppCompatActivity() {

  private lateinit var binding: ActivityContactBinding
  private lateinit var viewModel: ContactViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityContactBinding.inflate(layoutInflater)
    setContentView(binding.root)
    initViewModel()
    initClickListeners()
    observeContactState()
  }

  private fun initViewModel() {
    viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
  }

  private fun initClickListeners() {
    binding.btnContactSend.setOnClickListener {
      val title = binding.edtContactSubjectTitle.text.toString()
      val message = binding.edtContactMessage.text.toString()
      viewModel.sendMessage(title, message)
    }
  }

  private fun observeContactState() {
    viewModel.contactState.observe(this) { contactViewState ->
      when (contactViewState) {
        is ContactViewState.OpenEmail -> {
          IntentProcessor.process(
            context = this,
            emails = contactViewState.emails,
            subject = contactViewState.subject,
            text = contactViewState.message
          )
        }
        is ContactViewState.ShowRequiredFieldsMessage -> {
          toast(R.string.fill_in_the_required_fields)
        }
      }
    }
  }
}