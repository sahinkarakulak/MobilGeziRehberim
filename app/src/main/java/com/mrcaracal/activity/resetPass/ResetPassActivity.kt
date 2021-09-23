package com.mrcaracal.activity.resetPass

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityParolaSifirlamaBinding
import com.mrcaracal.mobilgezirehberim.login.Login

class ResetPassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParolaSifirlamaBinding
    private lateinit var viewModel: ResetPassViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParolaSifirlamaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        initClickListeners()
        observeRestPassState()
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(ResetPassViewModel::class.java)
    }

    fun initClickListeners() {
        binding.btnSendRequest.setOnClickListener {
            val email = binding.edtResetPass.text.toString()
            viewModel.sendRequest(email = email)
        }
    }

    private fun observeRestPassState() {
        viewModel.resetPassState.observe(this) { resetPassViewState ->
            when (resetPassViewState) {
                is ResetPassViewState.ShowRequiredFieldsMessage -> {
                    toast(R.string.fill_in_the_required_fields)
                }
                is ResetPassViewState.ShowErrorMessage -> {
                    toast(resetPassViewState.exception.toString())
                }
                is ResetPassViewState.ShowCheckEmailMessage -> {
                    toast(R.string.check_your_e_mail)
                }
                is ResetPassViewState.OpenLoginActivity -> {
                    startActivity(Intent(applicationContext, Login::class.java))
                }
            }
        }
    }
}