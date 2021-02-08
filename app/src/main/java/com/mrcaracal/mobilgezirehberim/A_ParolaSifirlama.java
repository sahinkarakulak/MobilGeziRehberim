package com.mrcaracal.mobilgezirehberim;

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
import com.google.firebase.auth.FirebaseAuth;

public class A_ParolaSifirlama extends AppCompatActivity {

    private static final String TAG = "A_ParolaSifirlama";

    EditText edt_epostaParolaSıfırlama;

    FirebaseAuth firebaseAuth;

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();

        edt_epostaParolaSıfırlama = findViewById(R.id.edt_epostaParolaSıfırlama);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parola_sifirlama);
        init();

        setTitle("Parola Sıfırlama");
    }

    // Kullanıcının girdiği E-Posta adresine parola sıfırlama bağlantısı gönderilecektir.
    public void istegiGonder(View view) {
        // parola sıfırlama işlemi için gereken işlemler yapılsın

        String eposta = edt_epostaParolaSıfırlama.getText().toString();

        if (eposta.equals("")) {
            Toast.makeText(this, "Gerekli alanı doldurunuz", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "istegiGonder: EditText'en boş veriler alındı");
        } else {
            firebaseAuth.sendPasswordResetEmail(eposta)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(A_ParolaSifirlama.this, "E-Postanızı kontorl ediniz", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(A_ParolaSifirlama.this, A_Giris.class);
                            startActivity(intent);
                            finish();
                            Log.d(TAG, "onSuccess: Sıfırlama isteği gönderildi ve kullanıcı A_Giris'e yönlendirilidi");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(A_ParolaSifirlama.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}