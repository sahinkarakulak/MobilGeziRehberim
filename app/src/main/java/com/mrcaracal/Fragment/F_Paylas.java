package com.mrcaracal.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.collect.ArrayListMultimap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mrcaracal.Activity.AnaSayfa;
import com.mrcaracal.Activity.Harita;
import com.mrcaracal.Modul.Gonderiler;
import com.mrcaracal.mobilgezirehberim.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class F_Paylas extends Fragment {

    private static final String TAG = "F_Paylas";

    Gonderiler MGonderiler;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    Uri resimYolu;
    ImageView img_paylasResimSec;
    EditText edt_paylasYerIsmi, edt_paylasYorum, edt_paylasTag, edt_konum, edt_adres, edt_sehir;
    Button btn_paylasGonder, konum_sec, btn_tag_ekle;
    TextView txt_taglari_yazdir;
    ScrollView sv_paylas;
    SharedPreferences GET;
    SharedPreferences.Editor SET;
    float enlem;
    float boylam;
    String adres;
    String posta_kodu;
    String gonderiID;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    List<String> taglar;

    public F_Paylas() {
        //
    }

    private void init() {

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        GET = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SET = GET.edit();

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_paylas, container, false);
        init();

        img_paylasResimSec = viewGroup.findViewById(R.id.img_paylasResimSec);
        edt_paylasYerIsmi = viewGroup.findViewById(R.id.edt_paylasYerIsmi);
        edt_konum = viewGroup.findViewById(R.id.edt_konum);
        edt_adres = viewGroup.findViewById(R.id.edt_adres);
        edt_sehir = viewGroup.findViewById(R.id.edt_sehir);
        edt_paylasYorum = viewGroup.findViewById(R.id.edt_paylasYorum);
        edt_paylasTag = viewGroup.findViewById(R.id.edt_paylasTag);
        konum_sec = viewGroup.findViewById(R.id.konum_sec);
        btn_paylasGonder = viewGroup.findViewById(R.id.btn_paylasGonder);
        btn_tag_ekle = viewGroup.findViewById(R.id.btn_tag_ekle);
        txt_taglari_yazdir = viewGroup.findViewById(R.id.txt_taglari_yazdir);
        sv_paylas = viewGroup.findViewById(R.id.sv_paylas);

        // Galeriden resim çekmek için yapılacaklar
        img_paylasResimSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    Log.d(TAG, "onClick: Daha önceden izin verilmediğinden izin istendi");
                } else {
                    Intent intentGaleri = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentGaleri, 2);
                    Log.d(TAG, "onClick: Daha önceden izin verildiğinden kullanıcı Galeriye yönlendirildi");
                }
            }
        });

        btn_tag_ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagOlusturma();

             }
        });

        konum_sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Harita.class));
                Log.d(TAG, "onClick: Kullanıcı Harita'a yönlendirildi");
            }
        });

        btn_paylasGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paylasGonder();

            }
        });


        return viewGroup;
    }

    public void tagOlusturma() {

        String[] tagler = edt_paylasTag.getText().toString().toLowerCase().split(" ");

        int etiket_sayisi = 0;
        String taggg = "";
        for (String tags : tagler){

            etiket_sayisi ++;
            Log.d(TAG, "TAGLER: " + tags.trim());
            taglar = Arrays.asList(tagler);

            taggg += "#" + tags + "   ";
            txt_taglari_yazdir.setText(taggg);

            if (etiket_sayisi == 5)
                break;
        }
    }

    public void paylasGonder() {

        btn_paylasGonder.setEnabled(false);

        Log.d(TAG, "paylasGonder: ...");
        String yerIsmiKontrol = edt_paylasYerIsmi.getText().toString();
        String yorumKontrol = edt_paylasYorum.getText().toString();
        String konumKontrol = edt_konum.getText().toString();
        String adresKontrol = edt_adres.getText().toString();

        if (!yerIsmiKontrol.equals("") && !konumKontrol.equals("") && !yorumKontrol.equals("") && !adresKontrol.equals("")) {
            if (txt_taglari_yazdir.equals("")){
                Toast.makeText(getActivity(), "Lütfen TAG ekleyin", Toast.LENGTH_SHORT).show();
                btn_paylasGonder.setEnabled(true);
            }else {
                UUID uuid = UUID.randomUUID();
                String resimIsmi = firebaseUser.getEmail() + "--" + yerIsmiKontrol + "--" + uuid;
                try {
                    storageReference
                            .child("Resimler")
                            .child(resimIsmi)
                            .putFile(resimYolu)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    StorageReference storageReference1 = FirebaseStorage.getInstance().getReference("Resimler/" + resimIsmi);
                                    storageReference1
                                            .getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                                    String kullaniciEposta = firebaseUser.getEmail();
                                                    String resimAdresi = uri.toString();
                                                    String yerIsmi = edt_paylasYerIsmi.getText().toString().toLowerCase();
                                                    String konum = edt_konum.getText().toString();
                                                    String yorum = edt_paylasYorum.getText().toString();
                                                    String adres = edt_adres.getText().toString();
                                                    String sehirrr = edt_sehir.getText().toString();

                                                    if (taglar == null){
                                                        taglar = Arrays.asList("mgr", "gezi", "rehber", "seyahat", "etiketsiz");
                                                    }
                                                    if (sehirrr == null){
                                                        sehirrr = null;
                                                    }

                                                    UUID uuid1 = UUID.randomUUID();
                                                    gonderiID = "" + uuid1;

                                                    MGonderiler = new Gonderiler(gonderiID, kullaniciEposta, resimAdresi, yerIsmi, konum, adres, sehirrr, yorum, posta_kodu, taglar, FieldValue.serverTimestamp());

                                                    DocumentReference documentReference1 = firebaseFirestore
                                                            .collection("Paylasilanlar")
                                                            .document(firebaseUser.getEmail())
                                                            .collection("Paylastiklari")
                                                            .document(gonderiID);

                                                    documentReference1
                                                            .set(MGonderiler)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    DocumentReference documentReference2 = firebaseFirestore
                                                                            .collection("Gonderiler")
                                                                            .document(gonderiID);

                                                                    documentReference2
                                                                            .set(MGonderiler)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    Intent intent = new Intent(getActivity(), AnaSayfa.class);
                                                                                    // Tüm aktiviteleri kapat
                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                    startActivity(intent);
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d(TAG, "onFailure: " + e.getMessage());
                                                                        }
                                                                    });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                                                }
                                                            });
                                                }
                                            });
                                    final Toast benimToast = Toast.makeText(getActivity(), "Gönderildi", Toast.LENGTH_SHORT);
                                    benimToast.show();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            benimToast.cancel();
                                        }
                                    }, 400);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                }
                            });
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "paylasGonder: " + e.getMessage());
                    btn_paylasGonder.setEnabled(true);
                }
            }

        } else
            Toast.makeText(getActivity(), "Gerekli alanları doldurunuz", Toast.LENGTH_SHORT).show();

        btn_paylasGonder.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Çalıştı");
        // BU ACTİVİY'E TEKRAR GELİNDİĞİNDE HARİTA SINIFINDAN GEREKLİ KOORDİNAT VE ADRES BİLGİLERİNİ BURADA ALSIN VE GEREKLİ YERLERDE YAYINLASIN

        enlem = GET.getFloat("enlem", 0);
        boylam = GET.getFloat("boylam", 0);
        adres = GET.getString("adres", "Türkiye Üsküdar");
        posta_kodu = GET.getString("postaKodu", "12000");

        edt_konum.setText(enlem + "," + boylam);
        edt_adres.setText("" + adres);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intentGaleri = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentGaleri, 2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.d(TAG, "onActivityResult: Çalıştı");

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            resimYolu = data.getData();
            Picasso.get()
                    .load(resimYolu)
                    .centerCrop()
                    .fit()
                    .into(img_paylasResimSec);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}