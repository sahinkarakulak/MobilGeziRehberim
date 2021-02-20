package com.mrcaracal.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mrcaracal.mobilgezirehberim.R;

public class Iletisim extends AppCompatActivity {

    private static final String TAG = "Iletisim";

    EditText edt_iletisim_konu_baslik, edt_iletisim_mesaj_icerik;
    Button btn_iletisim_gonder;

    private void init(){
        edt_iletisim_konu_baslik = findViewById(R.id.edt_iletisim_konu_baslik);
        edt_iletisim_mesaj_icerik = findViewById(R.id.edt_iletisim_mesaj_icerik);
        btn_iletisim_gonder = findViewById(R.id.btn_iletisim_gonder);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iletisim);
        init();
        setTitle("İletişim");

        btn_iletisim_gonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_konu_baslik = edt_iletisim_konu_baslik.getText().toString();
                String str_mesaj_icerik = edt_iletisim_mesaj_icerik.getText().toString();
                String[] admin_e_postalari = {"turkishpower.new@gmail.com","karakulaksahin@gmail.com"};

                if (str_konu_baslik.equals("") || str_mesaj_icerik.equals("")){
                    Toast.makeText(Iletisim.this, "Gerekli alanları doldurunuz", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: EditText'en boş veriler alındı");
                }else {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_EMAIL, admin_e_postalari);
                    intent.putExtra(Intent.EXTRA_SUBJECT, str_konu_baslik);
                    intent.putExtra(Intent.EXTRA_TEXT, str_mesaj_icerik);
                    intent.setType("plain/text");
                    startActivity(intent.createChooser(intent, "Ne ile göndermek istersiniz?"));
                    Log.d(TAG, "onClick: E-Mail gönderildi");
                }
            }
        });

    }
}