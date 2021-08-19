package com.mrcaracal.mobilgezirehberim

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrcaracal.activity.AccountCreateActivity
import com.mrcaracal.activity.HomePageActivity
import com.mrcaracal.activity.ResetPassActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    var doubleBackToExitPressedOnce = false
    var status = false
    var hide_show = false
    private var firebaseAuth: FirebaseAuth? = null

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        GET = getSharedPreferences(packageName, MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        title = getString(R.string.login)

        // Remember me!
        val rememverInfo = GET.getBoolean("boolean_key", false)
        if (rememverInfo == true) {
            binding.chbLoginInfosRemember.isChecked = true
            binding.edtEmailLogin.setText(GET.getString("keyPosta", ""))
            binding.edtPassLogin.setText(GET.getString("keyParola", ""))
        } else {
            binding.chbLoginInfosRemember.isChecked = false
            binding.edtEmailLogin.setText("")
            binding.edtPassLogin.setText("")
        }

        // Kullanıcı daha önceden giriş yapmış ise otomatik olarak giriş yapıp Ana sayfaya yönelendirilecektir.
        val firebaseUser = firebaseAuth?.currentUser
        if (firebaseUser != null) {
            val intent = Intent(this@Login, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Kullanıcı hesap oluşturma sayfasına yönlendirilecektir.
    fun txt_createAccount(view: View?) {
        val intent = Intent(this@Login, AccountCreateActivity::class.java)
        startActivity(intent)
    }

    // Kullanıcının girdiği bilgiler doğrultusunda giriş yapma işlemleri...
    fun btn_login(view: View?) {
        // Her şey tamam ise giriş yapılsın
        val email = binding.edtEmailLogin.text.toString()
        val pass = binding.edtPassLogin.text.toString()
        if (email == "" || pass == "") {
            toast(getString(R.string.fill_in_the_required_fields))
        } else {
            firebaseAuth!!.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    if (firebaseAuth?.currentUser!!.isEmailVerified) {
                        val intent = Intent(this, HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        toast(getString(R.string.confirm_your_account_from_the_link_in_your_e_mail))
                    }
                }.addOnFailureListener { e ->
                    toast(e.localizedMessage)
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

    // Kullanıcı parola sıfırlama sayfasına yönlendirilecektir.
    fun txt_iForgotMyPass(view: View?) {
        val intent = Intent(this@Login, ResetPassActivity::class.java)
        startActivity(intent)
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
        toast(getString(R.string.press_again_to_exit))

        Handler(Looper.myLooper() ?: return).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}