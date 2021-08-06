package com.mrcaracal.mobilgezirehberim

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrcaracal.activity.AccountCreateActivity
import com.mrcaracal.activity.HomePageActivity
import com.mrcaracal.activity.ResetPassActivity
import com.mrcaracal.extensions.toast

class Login : AppCompatActivity() {

    lateinit var progressDialog: ProgressDialog
    lateinit var edt_emailLogin: EditText
    lateinit var edt_passLogin: EditText
    lateinit var chb_loginInfosRemember: CheckBox
    lateinit var img_passHideShow: ImageView

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    var doubleBackToExitPressedOnce = false
    var status = false
    var hide_show = false
    private var firebaseAuth: FirebaseAuth? = null

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        edt_emailLogin = findViewById(R.id.edt_emailLogin)
        edt_passLogin = findViewById(R.id.edt_passLogin)
        img_passHideShow = findViewById(R.id.img_passHideShow)
        chb_loginInfosRemember = findViewById(R.id.chb_loginInfosRemember)
        GET = getSharedPreferences(packageName, MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
        title = getString(R.string.login)

        // Remember me!
        val rememverInfo = GET.getBoolean("boolean_key", false)
        if (rememverInfo == true) {
            chb_loginInfosRemember.isChecked = true
            edt_emailLogin.setText(GET.getString("keyPosta", ""))
            edt_passLogin.setText(GET.getString("keyParola", ""))
        } else {
            chb_loginInfosRemember.isChecked = false
            edt_emailLogin.setText("")
            edt_passLogin.setText("")
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
        val email = edt_emailLogin.text.toString()
        val pass = edt_passLogin.text.toString()
        if (email == "" || pass == "") {
            toast(R.string.fill_in_the_required_fields.toString())
        } else {
            firebaseAuth!!.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    if (firebaseAuth?.currentUser!!.isEmailVerified) {
                        progressDialog = ProgressDialog(this)
                        progressDialog.setMessage(R.string.login_in.toString())
                        progressDialog.show()
                        val intent = Intent(this, HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        toast(R.string.confirm_your_account_from_the_link_in_your_e_mail.toString())
                    }
                    progressDialog.dismiss()
                }.addOnFailureListener { e ->
                    toast(e.localizedMessage)
                }
        }
    }

    fun passHideAndShow() {
        img_passHideShow.setOnClickListener {
            if (hide_show == false) {
                edt_passLogin.inputType = InputType.TYPE_CLASS_TEXT
                edt_passLogin.transformationMethod = null
                hide_show = true
            } else {
                edt_passLogin.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                edt_passLogin.transformationMethod = PasswordTransformationMethod.getInstance()
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
        chb_loginInfosRemember.setOnClickListener {
            status = chb_loginInfosRemember.isChecked
            if (status == true) {
                SET.putBoolean("boolean_key", true)
                SET.putString("keyPosta", edt_emailLogin.text.toString())
                SET.putString("keyParola", edt_passLogin.text.toString())
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
        toast(R.string.press_again_to_exit.toString())
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}