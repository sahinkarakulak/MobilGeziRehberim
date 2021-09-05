package com.mrcaracal.mobilgezirehberim.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.activity.accountCreate.AccountCreateActivity
import com.mrcaracal.activity.homePage.HomePageActivity
import com.mrcaracal.activity.resetPass.ResetPassActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    var hide_show = false
    var status = false
    var doubleBackToExitPressedOnce = false

    fun init() {
        GET = getSharedPreferences(packageName, MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initViewModel()
        initClickListeners()
        observeContactState()
        rememberMe()
        viewModel.userIsAlreadyLoggedIn()
    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    private fun initClickListeners(){
        binding.txtIForgotMyPass.setOnClickListener {
            val intent = Intent(this@Login, ResetPassActivity::class.java)
            startActivity(intent)
        }

        binding.txtCreateAccount.setOnClickListener {
            val intent = Intent(this@Login, AccountCreateActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            // Her şey tamam ise giriş yapılsın
            val email = binding.edtEmailLogin.text.toString()
            val pass = binding.edtPassLogin.text.toString()
            viewModel.login(email = email, pass = pass)
        }
    }

    private fun rememberMe(){
        val rememberInfo = GET.getBoolean("boolean_key", false)
        if (rememberInfo == true) {
            binding.chbLoginInfosRemember.isChecked = true
            binding.edtEmailLogin.setText(GET.getString("keyPosta", ""))
            binding.edtPassLogin.setText(GET.getString("keyParola", ""))
        } else {
            binding.chbLoginInfosRemember.isChecked = false
            binding.edtEmailLogin.setText("")
            binding.edtPassLogin.setText("")
        }
    }

    private fun observeContactState(){
        viewModel.loginState.observe(this) { loginViewState ->
            when(loginViewState){
                is LoginViewSate.ShowRequiredFieldsMessage -> {
                    toast(R.string.fill_in_the_required_fields)
                }
                is LoginViewSate.OpenHomePageActivity -> {
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is LoginViewSate.ConfirmEmail -> {
                    toast(getString(R.string.confirm_your_account_from_the_link_in_your_e_mail))
                }
                is LoginViewSate.ShowExceptionMessage -> {
                    toast(loginViewState.exception.toString())
                }
            }
        }
    }

    fun passHideAndShow() {
        binding.imgPassHideShow.setOnClickListener {
            if (hide_show == false) {
                binding.edtPassLogin.inputType = InputType.TYPE_CLASS_TEXT
                binding.edtPassLogin.transformationMethod = null
                hide_show = true
            } else {
                binding.edtPassLogin.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.edtPassLogin.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                hide_show = false
            }
        }
    }

    // Beni hatırla işlemi - onResume durumunda yapılacaklar
    override fun onResume() {
        super.onResume()
        passHideAndShow()
        binding.chbLoginInfosRemember.setOnClickListener {
            status = binding.chbLoginInfosRemember.isChecked
            if (status == true) {
                SET.putBoolean("boolean_key", true)
                SET.putString("keyPosta", binding.edtEmailLogin.text.toString())
                SET.putString("keyParola", binding.edtPassLogin.text.toString())
                SET.commit()

            } else {
                SET.putBoolean("boolean_key", false)
                SET.putString("keyPosta", "")
                SET.putString("keyParola", "")
                SET.commit()
            }
        }
    }

    // Geri tuşuna ççift tıklama ile uygulamadan çıkma işlemi
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        toast(R.string.press_again_to_exit)

        Handler(Looper.myLooper() ?: return).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}