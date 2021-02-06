package com.mrcaracal.mobilgezirehberim;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Map;

public class F_Ara extends Fragment implements RecyclerViewClickInterface {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    ArrayList<String> gonderiIDleriFB;
    ArrayList<String> kullaniciEpostalariFB;
    ArrayList<String> resimAdresleriFB;
    ArrayList<String> yerIsimleriFB;
    ArrayList<String> konumlariFB;
    ArrayList<String> yorumlarFB;
    ArrayList<Timestamp> zamanlarFB;

    RecyclerView recycler_view_ara;

    RecyclerAdapterYapim recyclerAdapterYapim;

    ImageView img_konuma_gore_bul, img_arama_yap;
    EditText edt_anahtar_kelime_arat;

    private void init() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        gonderiIDleriFB = new ArrayList<>();
        kullaniciEpostalariFB = new ArrayList<>();
        resimAdresleriFB = new ArrayList<>();
        yerIsimleriFB = new ArrayList<>();
        konumlariFB = new ArrayList<>();
        yorumlarFB = new ArrayList<>();
        zamanlarFB = new ArrayList<>();

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_ara, container, false);
        init();

        img_konuma_gore_bul = viewGroup.findViewById(R.id.img_konuma_gore_bul);
        img_konuma_gore_bul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), A_Harita.class));
            }
        });

        edt_anahtar_kelime_arat = viewGroup.findViewById(R.id.edt_anahtar_kelime_arat);
        img_arama_yap = viewGroup.findViewById(R.id.img_arama_yap);
        img_arama_yap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String anahtar_kelime = edt_anahtar_kelime_arat.getText().toString();
                String[] ilgiliAlan = {"kullaniciEposta", "yerIsmi", "konum"};
                for (int i = 0; i < ilgiliAlan.length; i++) {
                    aramaYap(ilgiliAlan[i], anahtar_kelime);
                }
            }
        });

        recycler_view_ara = viewGroup.findViewById(R.id.recycler_view_ara);
        recycler_view_ara.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapterYapim = new RecyclerAdapterYapim(gonderiIDleriFB, kullaniciEpostalariFB, resimAdresleriFB, yerIsimleriFB, konumlariFB, yorumlarFB, zamanlarFB, this);
        recycler_view_ara.setAdapter(recyclerAdapterYapim);

        return viewGroup;
    }


    public void aramaYap(String iligiliAlan, String anahtar_kelime) {

        gonderiIDleriFB.clear();
        kullaniciEpostalariFB.clear();
        resimAdresleriFB.clear();
        yerIsimleriFB.clear();
        konumlariFB.clear();
        yorumlarFB.clear();
        zamanlarFB.clear();

        recycler_view_ara.scrollToPosition(0);

        CollectionReference collectionReference = firebaseFirestore
                .collection("TumGonderiler");

        // VT'ye kaydedilme zamanına göre verileri çek
        collectionReference
                .whereEqualTo(iligiliAlan, anahtar_kelime)
                .orderBy("zaman", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            for (DocumentSnapshot snapshot : querySnapshot) {

                                // Çekilen her veriyi Map dizinine at ve daha sonra çekip kullan
                                Map<String, Object> verilerKumesi = snapshot.getData();

                                String gonderiID = (String) verilerKumesi.get("gonderiID");
                                String kullaniciEposta = (String) verilerKumesi.get("kullaniciEposta");
                                String yerIsmi = (String) verilerKumesi.get("yerIsmi");
                                String resimAdresi = (String) verilerKumesi.get("resimAdresi");
                                String konum = (String) verilerKumesi.get("konum");
                                String yorum = (String) verilerKumesi.get("yorum");
                                Timestamp zaman = (Timestamp) verilerKumesi.get("zaman");

                                gonderiIDleriFB.add(gonderiID);
                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                konumlariFB.add(konum);
                                yorumlarFB.add(yorum);
                                zamanlarFB.add(zaman);

                                recyclerAdapterYapim.notifyDataSetChanged();

                            }
                        }

                    }
                });

    }

    // Her bir recylerRow'a tıklandığında yapılacak işlemler
    // Detayları AlertDialog içerisinde gösterme
    // AlertDialog yerine Expandable Recycler View kullanılabilir
    @Override
    public void onItemClick(int position) {


    }

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    @Override
    public void onLongItemClick(int position) {

        String tarih_ve_saat = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        String tarih = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        String gonderi_detay_goster = "Paylaşan: " + kullaniciEpostalariFB.get(position) + "\nTarih: " + tarih_ve_saat + "\n\n" + yorumlarFB.get(position);


/*
        String gonderi_detay_goster = "Paylaşan: " + kullaniciEpostalariFB.get(position) + "\n\n" + yorumlarFB.get(position);
*/

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert
                .setTitle(yerIsimleriFB.get(position))
                .setMessage(gonderi_detay_goster)
                .setNegativeButton("iptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (kullaniciEpostalariFB.get(position).equals(firebaseUser.getEmail())) {
                            Toast.makeText(getActivity(), "Bunu zaten siz paylaştınız", Toast.LENGTH_SHORT).show();
                        } else {

                            M_Gonderiler MGonderiler = new M_Gonderiler(gonderiIDleriFB.get(position), kullaniciEpostalariFB.get(position), resimAdresleriFB.get(position), yerIsimleriFB.get(position), konumlariFB.get(position), yorumlarFB.get(position), FieldValue.serverTimestamp());

                            DocumentReference documentReference = firebaseFirestore
                                    .collection("Kullanicilar")
                                    .document(firebaseUser.getEmail())
                                    .collection("Kaydettikleri")
                                    .document(gonderiIDleriFB.get(position));

                            documentReference
                                    .set(MGonderiler)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Kaydedildi", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }


                    }
                })
                .show();

    }
}
