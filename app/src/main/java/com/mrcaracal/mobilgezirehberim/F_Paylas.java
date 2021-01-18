package com.mrcaracal.mobilgezirehberim;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class F_Paylas extends Fragment {

    Gonderiler gonderiler;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    Uri resimYolu;
    ImageView img_paylasResimSec;
    EditText edt_paylasYerIsmi, edt_paylasYorum, edt_konum;
    Button btn_paylasGonder;
    ScrollView sv_paylas;
    DocumentReference documentReference;
    String kullaniciAdi;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private void init() {

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_paylas, container, false);
        init();

        img_paylasResimSec = viewGroup.findViewById(R.id.img_paylasResimSec);
        edt_paylasYerIsmi = viewGroup.findViewById(R.id.edt_paylasYerIsmi);
        edt_konum = viewGroup.findViewById(R.id.edt_konum);
        edt_paylasYorum = viewGroup.findViewById(R.id.edt_paylasYorum);
        btn_paylasGonder = viewGroup.findViewById(R.id.btn_paylasGonder);
        sv_paylas = viewGroup.findViewById(R.id.sv_paylas);

        // Galeriden resim çekmek için yapılacaklar
        img_paylasResimSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // İzin verilmemişse izin iste
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    // İzin daha önceden verilmiş ise galeriye gidilsin
                    Intent intentGaleri = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentGaleri, 2);
                }
            }
        });

        // Paylaş butonuna tıklandığında yapılacaklar
        btn_paylasGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                paylasGonder();
            }
        });

        return viewGroup;
    }

    public void paylasGonder() {

        String yerIsmiKontrol = edt_paylasYerIsmi.getText().toString();
        String yorumKontrol = edt_paylasYorum.getText().toString();
        String konumKontrol = edt_konum.getText().toString();

        documentReference = FirebaseFirestore.getInstance()
                .collection("Kullanicilar")
                .document(firebaseUser.getEmail())
                .collection("Bilgileri")
                .document(firebaseUser.getEmail());

        documentReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                kullaniciAdi = documentSnapshot.getString("kullaniciAdi");
                            }
                        }
                    }
                });

        if (!yerIsmiKontrol.equals("") || !konumKontrol.equals("") || !yorumKontrol.equals("")) {

            UUID uuid = UUID.randomUUID();
            String resimIsmi = firebaseUser.getEmail() + "--RESIM--" + uuid;

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

                                                gonderiler = new Gonderiler(gonderiID, kullaniciEposta, resimAdresi, yerIsmi, konum, yorum, FieldValue.serverTimestamp());

                                                DocumentReference documentReference1 = firebaseFirestore
                                                        .collection("Kullanicilar")
                                                        .document(firebaseUser.getEmail())
                                                        .collection("Paylastiklari")
                                                        .document();

                                                documentReference1
                                                        .set(gonderiler)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                DocumentReference documentReference2 = firebaseFirestore
                                                                        .collection("TumGonderiler")
                                                                        .document(gonderiID);

                                                                documentReference2
                                                                        .set(gonderiler)
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

                                                                    }
                                                                });

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            }
                        });
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else
            Toast.makeText(getActivity(), "Gerekli alanları doldurunuz", Toast.LENGTH_SHORT).show();
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

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {

            resimYolu = data.getData();

            Picasso.get()
                    .load(resimYolu)
                    .resize(img_paylasResimSec.getWidth(), img_paylasResimSec.getHeight())
                    .centerCrop()
                    .into(img_paylasResimSec);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}