package com.mrcaracal.activity.accountCreate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityAccountCreateBinding
import com.mrcaracal.mobilgezirehberim.login.Login

class AccountCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountCreateBinding
    private lateinit var viewModel: AccountCreateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        initClickListeners()
        observeAccountCreateState()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(AccountCreateViewModel::class.java)
    }

    private fun initClickListeners() {
        binding.btnCreateAccount.setOnClickListener {
            val userName = binding.edtUserName.text.toString()
            val email = binding.edtUserEmail.text.toString()
            val passOne = binding.edtUserPassOne.text.toString()
            val passTwo = binding.edtUserPassTwo.text.toString()
            viewModel.createAccount(
                userName = userName,
                email = email,
                pass1 = passOne,
                pass2 = passTwo
            )
        }
    }

    private fun observeAccountCreateState() {
        viewModel.accountCreateState.observe(this) { accountCreateViewState ->
            when (accountCreateViewState) {
                is AccountCreateViewModel.AccountCreateViewState.ShowRequiredFieldsMessage -> {
                    toast(R.string.fill_in_the_required_fields)
                }
                is AccountCreateViewModel.AccountCreateViewState.CreateAccountAndSignOut -> {
                    startActivity(Intent(this, Login::class.java))
                }
                is AccountCreateViewModel.AccountCreateViewState.ThePassIsNotTheSame -> {
                    toast(R.string.passwords_are_not_the_same)
                }
                is AccountCreateViewModel.AccountCreateViewState.ShowExceptionMessage -> {
                    toast(accountCreateViewState.exception.toString())
                }
            }
        }
    }
}