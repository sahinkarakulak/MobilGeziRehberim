package com.mrcaracal.activity.resetPass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityParolaSifirlamaBinding

class ResetPassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParolaSifirlamaBinding
    private lateinit var viewModel: ResetPassViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParolaSifirlamaBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = getString(R.string.reset_pass)
        viewModel = ViewModelProvider(this).get(ResetPassViewModel::class.java)

        binding.btnSendRequest.setOnClickListener {
            val email = binding.edtResetPass.text.toString()
            viewModel.sendRequest(email, it.context)
        }
    }
}