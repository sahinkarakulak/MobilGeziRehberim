package com.mrcaracal.mobilgezirehberim

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mrcaracal.activity.AccountCreateActivity
import com.mrcaracal.activity.HomePageActivity
import com.mrcaracal.activity.ResetPassActivity

private const val TAG = "Login"

class Login : AppCompatActivity() {

    var progressDialog: ProgressDialog? = null
    var edt_emailLogin: EditText? = null
    var edt_passLogin: EditText? = null
    var chb_loginInfosRemember: CheckBox? = null
    var img_passHideShow: ImageView? = null

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
        title = "Giriş"

        // Remember me!
        val rememverInfo = GET!!.getBoolean("boolean_key", false)
        if (rememverInfo == true) {
            chb_loginInfosRemember!!.isChecked = true
            edt_emailLogin!!.setText(GET!!.getString("keyPosta", ""))
            edt_passLogin!!.setText(GET!!.getString("keyParola", ""))
            Log.i(TAG, "onCreate: Kullanıcının girdiği bilgiler alındı")
        } else {
            chb_loginInfosRemember!!.isChecked = false
            edt_emailLogin!!.setText("")
            edt_passLogin!!.setText("")
            Log.i(TAG, "onCreate: Kullanıcının girdiği bilgiler serbest bırakıldı")
        }

        // Kullanıcı daha önceden giriş yapmış ise otomatik olarak giriş yapıp Ana sayfaya yönelendirilecektir.
        val firebaseUser = firebaseAuth!!.currentUser
        if (firebaseUser != null) {
            val intent = Intent(this@Login, HomePageActivity::class.java)
            startActivity(intent)
            finish()
            Log.i(TAG, "onCreate: Kullanıcı doğrudan uygulama içine yönlendirildi")
        }
    }

    // Kullanıcı hesap oluşturma sayfasına yönlendirilecektir.
    fun txt_createAccount(view: View?) {
        val intent = Intent(this@Login, AccountCreateActivity::class.java)
        startActivity(intent)
        Log.i(TAG, "txt_hesapOlustur: Kullanıcı HesapOlusturma'a geçti")
    }

    // Kullanıcının girdiği bilgiler doğrultusunda giriş yapma işlemleri...
    fun btn_login(view: View?) {
        // Her şey tamam ise giriş yapılsın
        val email = edt_emailLogin!!.text.toString()
        val pass = edt_passLogin!!.text.toString()
        if (email == "" || pass == "") {
            Toast.makeText(this, "Lütfen gerekli alanları doldrunuz", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "girisYap: EditText içerisinden boş veri çekildi")
        } else {
            firebaseAuth!!.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    if (firebaseAuth!!.currentUser!!.isEmailVerified) {
                        progressDialog = ProgressDialog(this)
                        progressDialog!!.setMessage("Giriş Yapılıyor")
                        progressDialog!!.show()
                        val intent = Intent(this, HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                        Log.i(TAG, "onSuccess: Kullanıcı Giris'e geçti")
                    } else {
                        Toast.makeText(
                            this,
                            "E-Postanıza gelen bağlantıdan hesabınızı onaylayın",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.i(TAG, "onSuccess: Kullanıcıya hesap doğrulama bağlantısı gönderildi")
                    }
                    progressDialog!!.dismiss()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "onFailure: " + e.message)
                }
        }
    }

    fun passHideAndShow() {
        img_passHideShow!!.setOnClickListener {
            if (hide_show == false) {
                edt_passLogin!!.inputType = InputType.TYPE_CLASS_TEXT
                edt_passLogin!!.transformationMethod = null
                hide_show = true
            } else {
                edt_passLogin!!.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                edt_passLogin!!.transformationMethod = PasswordTransformationMethod.getInstance()
                hide_show = false
            }
        }
    }

    // Kullanıcı parola sıfırlama sayfasına yönlendirilecektir.
    fun txt_iForgotMyPass(view: View?) {
        val intent = Intent(this@Login, ResetPassActivity::class.java)
        startActivity(intent)
        Log.i(TAG, "txt_parolamıUnuttum: Kullanıcı ParolaSifirlama'a geçti")
    }

    // Beni hatırla işlemi - onResume durumunda yapılacaklar
    override fun onResume() {
        super.onResume()
        passHideAndShow()
        chb_loginInfosRemember!!.setOnClickListener {
            status = chb_loginInfosRemember!!.isChecked
            if (status == true) {
                SET!!.putBoolean("boolean_key", true)
                SET!!.putString("keyPosta", edt_emailLogin!!.text.toString())
                SET!!.putString("keyParola", edt_passLogin!!.text.toString())
                SET!!.commit()
                Log.i(
                    TAG,
                    "onClick: Kullanıcının girdiği bilgiler çekildi ve EditText'e yazdırıldı"
                )
            } else {
                SET!!.putBoolean("boolean_key", false)
                SET!!.putString("keyPosta", "")
                SET!!.putString("keyParola", "")
                SET!!.commit()
                Log.i(TAG, "onClick: herhangi bir şey yazdırılmadı")
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
        Toast.makeText(this, "Çıkmak için tekrar basınız", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}