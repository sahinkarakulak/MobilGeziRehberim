package com.mrcaracal.mobilgezirehberim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mrcaracal.Activity.AnaSayfa;
import com.mrcaracal.Activity.HesapOlusturma;
import com.mrcaracal.Activity.ParolaSifirlama;

public class A_Giris extends AppCompatActivity {

    private static final String TAG = "A_Giris";

    ProgressDialog progressDialog;

    EditText edt_epostaGiris, edt_parolaGiris;
    CheckBox chb_giris_bilgileri_hatirla;
    SharedPreferences GET;
    SharedPreferences.Editor SET;
    boolean doubleBackToExitPressedOnce = false;
    boolean durum;
    private FirebaseAuth firebaseAuth;

    public void init() {

        firebaseAuth = FirebaseAuth.getInstance();

        edt_epostaGiris = findViewById(R.id.edt_epostaGiris);
        edt_parolaGiris = findViewById(R.id.edt_parolaGiris);

        chb_giris_bilgileri_hatirla = findViewById(R.id.chb_giris_bilgileri_hatirla);

        GET = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SET = GET.edit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris);
        init();

        setTitle("Giriş");

        // Beni hatırla işlemi
        boolean bilgihatirla = GET.getBoolean("boolean_key", false);
        if (bilgihatirla == true) {
            chb_giris_bilgileri_hatirla.setChecked(true);

            edt_epostaGiris.setText(GET.getString("keyPosta", ""));
            edt_parolaGiris.setText(GET.getString("keyParola", ""));
            Log.d(TAG, "onCreate: Kullanıcının girdiği bilgiler alındı");
        } else {
            chb_giris_bilgileri_hatirla.setChecked(false);

            edt_epostaGiris.setText("");
            edt_parolaGiris.setText("");
            Log.d(TAG, "onCreate: Kullanıcının girdiği bilgiler serbest bırakıldı");
        }

        // Kullanıcı daha önceden giriş yapmış ise otomatik olarak giriş yapıp Ana sayfaya yönelendirilecektir.
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(A_Giris.this, AnaSayfa.class);
            startActivity(intent);
            finish();
            Log.d(TAG, "onCreate: Kullanıcı doğrudan uygulama içine yönlendirildi");
        }

    }

    // Kullanıcı hesap oluşturma sayfasına yönlendirilecektir.
    public void txt_hesapOlustur(View view) {
        Intent intent = new Intent(A_Giris.this, HesapOlusturma.class);
        startActivity(intent);
        Log.d(TAG, "txt_hesapOlustur: Kullanıcı HesapOlusturma'a geçti");
    }

    // Kullanıcının girdiği bilgiler doğrultusunda giriş yapma işlemleri...
    public void girisYap(View view) {
        // Her şey tamam ise giriş yapılsın
        String eposta = edt_epostaGiris.getText().toString();
        String parola = edt_parolaGiris.getText().toString();

        if (eposta.equals("") || parola.equals("")) {
            Toast.makeText(this, "Lütfen gerekli alanları doldrunuz", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "girisYap: EditText içerisinden boş veri çekildi");
        } else {
            firebaseAuth.signInWithEmailAndPassword(eposta, parola)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if (firebaseAuth.getCurrentUser().isEmailVerified()) {

                                progressDialog = new ProgressDialog(A_Giris.this);
                                progressDialog.setMessage("Giriş Yapılıyor");
                                progressDialog.show();

                                Intent intent = new Intent(A_Giris.this, AnaSayfa.class);
                                startActivity(intent);
                                finish();
                                Log.d(TAG, "onSuccess: Kullanıcı A_Giris'e geçti");

                            } else {
                                Toast.makeText(A_Giris.this, "E-Postanıza gelen bağlantıdan hesabınızı onaylayın", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onSuccess: Kullanıcıya hesap doğrulama bağlantısı gönderildi");
                            }
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(A_Giris.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: "+e.getMessage());
                }
            });


        }

    }

    // Kullanıcı parola sıfırlama sayfasına yönlendirilecektir.
    public void txt_parolamıUnuttum(View view) {
        Intent intent = new Intent(A_Giris.this, ParolaSifirlama.class);
        startActivity(intent);
        Log.d(TAG, "txt_parolamıUnuttum: Kullanıcı ParolaSifirlama'a geçti");
    }

    // Beni hatırla işlemi - onResume durumunda yapılacaklar
    @Override
    protected void onResume() {
        super.onResume();

        chb_giris_bilgileri_hatirla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                durum = chb_giris_bilgileri_hatirla.isChecked();

                if (durum == true) {
                    SET.putBoolean("boolean_key", true);
                    SET.putString("keyPosta", edt_epostaGiris.getText().toString());
                    SET.putString("keyParola", edt_parolaGiris.getText().toString());
                    SET.commit();
                    Log.d(TAG, "onClick: Kullanıcının girdiği bilgiler çekildi ve EditText'e yazdırıldı");

                } else {
                    SET.putBoolean("boolean_key", false);
                    SET.putString("keyPosta", "");
                    SET.putString("keyParola", "");
                    SET.commit();
                    Log.d(TAG, "onClick: herhangi bir şey yazdırılmadı");
                }

            }
        });
    }

    // Geri tuşuna ççift tıklama ile uygulamadan çıkma işlemi
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();

            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Çıkmak için tekrar basınız", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce = false;

            }
        }, 2000);
    }
}