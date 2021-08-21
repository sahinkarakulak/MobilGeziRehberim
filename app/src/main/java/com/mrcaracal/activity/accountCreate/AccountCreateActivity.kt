package com.mrcaracal.activity.accountCreate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityAccountCreateBinding

class AccountCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountCreateBinding
    private lateinit var viewModel: AccountCreateViewModel

    lateinit var userName: String
    lateinit var email: String
    lateinit var passOne: String
    lateinit var passTwo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        initClickListeners()
        observeContactState()
    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(this).get(AccountCreateViewModel::class.java)
    }

    private fun initClickListeners(){
        binding.btnCreateAccount.setOnClickListener {
            userName = binding.edtUserName.text.toString()
            email = binding.edtUserEmail.text.toString()
            passOne = binding.edtUserPassOne.text.toString()
            passTwo = binding.edtUserPassTwo.text.toString()
            viewModel.createAccount(userName, email, passOne, passTwo)
        }
    }

    private fun observeContactState(){
        viewModel.accountCreateState.observe(this){ accountCreateViewState ->
            when(accountCreateViewState){
                is AccountCreateViewModel.AccountCreateViewState.ShowRequiredFieldsMessage -> {
                    toast(R.string.fill_in_the_required_fields)
                }
                is AccountCreateViewModel.AccountCreateViewState.CreateAccountAndSignOut -> {
                    startActivity(Intent(this, Login::class.java))
                }
                is AccountCreateViewModel.AccountCreateViewState.ThePassIsNotTheSame -> {
                    toast(R.string.passwords_are_not_the_same)
                }
                is AccountCreateViewModel.AccountCreateViewState.ShowErrorMessage -> {
                    toast(accountCreateViewState.e.toString())
                }
            }
        }
    }
}