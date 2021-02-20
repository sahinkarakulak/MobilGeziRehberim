package com.mrcaracal.mobilgezirehberim;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class F_Anasayfa extends Fragment implements RecyclerViewClickInterface {

    private static final String TAG = "F_Anasayfa";

//    ProgressDialog progressDialog;

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

    RecyclerView recyclerView;

    RecyclerAdapterYapim recyclerAdapterYapim;

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
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_ana_sayfa, container, false);
        init();

        yenidenEskiyeCek();

        // RecyclerView Tanımlama İşlemi
        recyclerView = viewGroup.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapterYapim = new RecyclerAdapterYapim(gonderiIDleriFB, kullaniciEpostalariFB, resimAdresleriFB, yerIsimleriFB, konumlariFB, yorumlarFB, zamanlarFB, this);
        recyclerView.setAdapter(recyclerAdapterYapim);
        Log.d(TAG, "onCreateView: RecyclerView tanımlama ve Adapter'a gerekli paremetrelerin gönderilmesi tamamlandı");

        return viewGroup;
    }

    public void yenidenEskiyeCek() {

//        progressDialog = new ProgressDialog(getActivity());
//        progressDialog.setMessage("Yükleniyor");
//        progressDialog.show();

        CollectionReference collectionReference = firebaseFirestore
                .collection("TumGonderiler");
        // VT'ye kaydedilme zamanına göre verileri çek
        collectionReference
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
                                Log.d(TAG, "onComplete: VT'den veriler çekildi, ArrayListlere aktarılıp RecyclerAdapterYapim'a aktarıldı");
//                                progressDialog.dismiss();
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
        Log.d(TAG, "onItemClick: Tek tık");
    }

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    @Override
    public void onLongItemClick(int position) {
        Log.d(TAG, "onLongItemClick: Uzun tık");
        
        String tarih_ve_saat = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        String tarih = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        String gonderi_detay_goster = "Paylaşan: " + kullaniciEpostalariFB.get(position) + "\nTarih: " + tarih_ve_saat + "\n\n" + yorumlarFB.get(position);
        //String gonderi_detay_goster = "Paylaşan: " + kullaniciEpostalariFB.get(position) + "\n\n" + yorumlarFB.get(position);

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert
                .setTitle(yerIsimleriFB.get(position))
                .setMessage(gonderi_detay_goster)
                .setNegativeButton("iptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: İPTAL");
                    }
                })
                .setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: KAYDET");

                        if (kullaniciEpostalariFB.get(position).equals(firebaseUser.getEmail())) {
                            Toast.makeText(getActivity(), "Bunu zaten siz paylaştınız", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onClick: Bu gönderiyi zaten bu kullanıcı paylaşmıştı");
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
                            Log.d(TAG, "onClick: Gönderi kaydedildi");
                        }


                    }
                })
                .show();

    }
}
