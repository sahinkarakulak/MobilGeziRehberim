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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    EditText edt_paylasYerIsmi, edt_paylasYorum, edt_konum, edt_adres;
    Button btn_paylasGonder, konum_sec;
    ScrollView sv_paylas;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    SharedPreferences GET;
    SharedPreferences.Editor SET;

    float enlem;
    float boylam;
    String adres;
    private void init() {

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        GET = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SET = GET.edit();

    }

    public F_Paylas(){
        //
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_paylas, container, false);
        init();

        img_paylasResimSec = viewGroup.findViewById(R.id.img_paylasResimSec);
        edt_paylasYerIsmi = viewGroup.findViewById(R.id.edt_paylasYerIsmi);
        edt_konum = viewGroup.findViewById(R.id.edt_konum);
        edt_adres = viewGroup.findViewById(R.id.edt_adres);
        edt_paylasYorum = viewGroup.findViewById(R.id.edt_paylasYorum);
        konum_sec = viewGroup.findViewById(R.id.konum_sec);
        btn_paylasGonder = viewGroup.findViewById(R.id.btn_paylasGonder);
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

    public void paylasGonder() {
        Log.d(TAG, "paylasGonder: ...");
        String yerIsmiKontrol = edt_paylasYerIsmi.getText().toString();
        String yorumKontrol = edt_paylasYorum.getText().toString();
        String konumKontrol = edt_konum.getText().toString();

        if (!yerIsmiKontrol.equals("") || !konumKontrol.equals("") || !yorumKontrol.equals("")) {
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
                                                String yerIsmi = edt_paylasYerIsmi.getText().toString();
                                                String konum = edt_konum.getText().toString();
                                                String yorum = edt_paylasYorum.getText().toString();

                                                UUID uuid1 = UUID.randomUUID();
                                                String gonderiID = "" + uuid1;

                                                MGonderiler = new Gonderiler(gonderiID, kullaniciEposta, resimAdresi, yerIsmi, konum, yorum, FieldValue.serverTimestamp());

                                                DocumentReference documentReference1 = firebaseFirestore
                                                        .collection("Kullanicilar")
                                                        .document(firebaseUser.getEmail())
                                                        .collection("Paylastiklari")
                                                        .document();

                                                documentReference1
                                                        .set(MGonderiler)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                DocumentReference documentReference2 = firebaseFirestore
                                                                        .collection("TumGonderiler")
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
            }
        } else
            Toast.makeText(getActivity(), "Gerekli alanları doldurunuz", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Çalıştı");
        // BU ACTİVİY'E TEKRAR GELİNDİĞİNDE HARİTA SINIFINDAN GEREKLİ KOORDİNAT VE ADRES BİLGİLERİNİ BURADA ALSIN VE GEREKLİ YERLERDE YAYINLASIN

        enlem = GET.getFloat("enlem",0);
        boylam = GET.getFloat("boylam", 0);
        adres = GET.getString("adres", "Türkiye Üsküdar");

        edt_konum.setText(enlem+","+boylam);
        edt_adres.setText(""+adres);

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