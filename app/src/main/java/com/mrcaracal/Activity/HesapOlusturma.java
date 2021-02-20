package com.mrcaracal.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mrcaracal.Modul.KullaniciBilgileri;
import com.mrcaracal.mobilgezirehberim.Giris;
import com.mrcaracal.mobilgezirehberim.R;

public class HesapOlusturma extends AppCompatActivity {

    private static final String TAG = "HesapOlusturma";

    KullaniciBilgileri MKullaniciBilgileri;

    EditText edt_kullaniciAdi, edt_eposta, edt_parola1, edt_parola2;
    String kullaniciAdi, eposta, parola1, parola2;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public void init() {
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();

        edt_kullaniciAdi = findViewById(R.id.edt_kullaniciAdi);
        edt_eposta = findViewById(R.id.edt_eposta);
        edt_parola1 = findViewById(R.id.edt_parola1);
        edt_parola2 = findViewById(R.id.edt_parola2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hesap_olustur);
        init();


        setTitle("Hesap Oluştur");
    }

    // Kullanıcının girdiği bilgiler doğrultusunda hesap oluşturma işlemleri gerçekleşecektir.
    public void btn_hesabiOlustur(View view) {
        // Her şey tamam ise hesap oluşsun ve giriş yapılsın

        kullaniciAdi = edt_kullaniciAdi.getText().toString();
        eposta = edt_eposta.getText().toString();
        parola1 = edt_parola1.getText().toString();
        parola2 = edt_parola2.getText().toString();

        if (kullaniciAdi.equals("") || eposta.equals("") || parola1.equals("") || parola2.equals("")) {
            Toast.makeText(this, "Gerekli alanları doldurunuz...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "btn_hesabiOlustur: EditText'en boşveriler çekildi");
        } else {
            if (parola1.equals(parola2)) {
                firebaseAuth
                        .createUserWithEmailAndPassword(eposta, parola1)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                firebaseAuth
                                        .getCurrentUser()
                                        .sendEmailVerification()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(HesapOlusturma.this, "Doğrulama bağlantısı E-Posta adresinize gönderildi.", Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "onSuccess: Doğrulama baplantııs E-Posta adresine gönderildi");
                                                MKullaniciBilgileri = new KullaniciBilgileri(kullaniciAdi, eposta, parola1, "MGR'i Seviyorum", "https://firebasestorage.googleapis.com/v0/b/mobilgezirehberim-7aca5.appspot.com/o/Resimler%2Fdefaultpp.png?alt=media&token=97fe9138-0aad-4ea9-af78-536c637b3be4");

                                                DocumentReference documentReference = firebaseFirestore
                                                        .collection("Kullanicilar")
                                                        .document(eposta)
                                                        .collection("Bilgileri")
                                                        .document(eposta);

                                                documentReference
                                                        .set(MKullaniciBilgileri)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Intent intent = new Intent(HesapOlusturma.this, Giris.class);
                                                                startActivity(intent);
                                                                finish();
                                                                firebaseAuth.signOut();
                                                                Log.d(TAG, "onSuccess: Kayıttan sonra kullanıcı Giris'e gönderildi");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(HesapOlusturma.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                Log.d(TAG, "onFailure: "+e.getMessage());
                                                            }
                                                        });


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(HesapOlusturma.this, "Beklenmedik bir hata gerçekleşti\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "onFailure: "+e.getMessage());
                                            }
                                        });
                            }
                        });
            } else
                Toast.makeText(this, "Parolalar uyuşmuyor. Lütfen kontrol ediniz...", Toast.LENGTH_SHORT).show();
        }

    }
}