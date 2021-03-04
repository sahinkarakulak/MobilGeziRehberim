package com.mrcaracal.Fragment;

import android.app.AlertDialog;
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
import com.mrcaracal.Adapter.RecyclerAdapterYapim;
import com.mrcaracal.Interface.RecyclerViewClickInterface;
import com.mrcaracal.Modul.Gonderiler;
import com.mrcaracal.mobilgezirehberim.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
    ArrayList<String> adresleriFB;
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
        adresleriFB = new ArrayList<>();
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
        recyclerAdapterYapim = new RecyclerAdapterYapim(gonderiIDleriFB, kullaniciEpostalariFB, resimAdresleriFB, yerIsimleriFB, konumlariFB, adresleriFB, yorumlarFB, zamanlarFB, this);
        recyclerView.setAdapter(recyclerAdapterYapim);
        Log.d(TAG, "onCreateView: RecyclerView tanımlama ve Adapter'a gerekli paremetrelerin gönderilmesi tamamlandı");

        return viewGroup;
    }

    public void yenidenEskiyeCek() {

//        progressDialog = new ProgressDialog(getActivity());
//        progressDialog.setMessage("Yükleniyor");
//        progressDialog.show();

        CollectionReference collectionReference = firebaseFirestore
                .collection("Gonderiler");
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

                                String gonderiID = verilerKumesi.get("gonderiID").toString();
                                String kullaniciEposta = verilerKumesi.get("kullaniciEposta").toString();
                                String yerIsmi = verilerKumesi.get("yerIsmi").toString();
                                yerIsmi = yerIsmi.substring(0, 1).toUpperCase() + yerIsmi.substring(1);
                                String resimAdresi = verilerKumesi.get("resimAdresi").toString();
                                String konum = verilerKumesi.get("konum").toString();
                                String adres = verilerKumesi.get("adres").toString();
                                String yorum = verilerKumesi.get("yorum").toString();
                                Timestamp zaman = (Timestamp) verilerKumesi.get("zaman");

                                gonderiIDleriFB.add(gonderiID);
                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                konumlariFB.add(konum);
                                adresleriFB.add(adres);
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

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    @Override
    public void onLongItemClick(int position) {
        Log.d(TAG, "onLongItemClick: Uzun tık");

        String tarih_ve_saat = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        //String gonderi_detay_goster = "Paylaşan: " + kullaniciEpostalariFB.get(position) + "\nTarih: " + tarih_ve_saat + "\n\n" + yorumlarFB.get(position) + "\n\nAdres: "+adresleriFB.get(position);
        String gonderi_detay_goster = yorumlarFB.get(position) + "\n\nPaylaşan: " + kullaniciEpostalariFB.get(position) + "\nTarih: " + tarih_ve_saat + "\nAdres: "+adresleriFB.get(position);

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert
                .setTitle(yerIsimleriFB.get(position))
                .setMessage(gonderi_detay_goster)
                .setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: TAMAM");

                    }
                })
                .show();

    }

    @Override
    public void onBaslikClick(int position) {
        Toast.makeText(getActivity(), "BAŞLIK", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKaydetClick(int position) {
        if (kullaniciEpostalariFB.get(position).equals(firebaseUser.getEmail())) {
            Toast.makeText(getActivity(), "Bunu zaten siz paylaştınız", Toast.LENGTH_SHORT).show();
        } else {

            Gonderiler MGonderiler = new Gonderiler(gonderiIDleriFB.get(position), kullaniciEpostalariFB.get(position), resimAdresleriFB.get(position), yerIsimleriFB.get(position), konumlariFB.get(position), adresleriFB.get(position), yorumlarFB.get(position), FieldValue.serverTimestamp());

            DocumentReference documentReference = firebaseFirestore
                    .collection("Kaydedenler")
                    .document(firebaseUser.getEmail())
                    .collection("Kaydedilenler")
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

            Map<String, Object> map = new HashMap();
            map.put("gonderiID", true);
            map.put("kaydeden", firebaseUser.getEmail());
            map.put("IDsi",gonderiIDleriFB.get(position));

            DocumentReference documentReference1 = firebaseFirestore
                    .collection("Kaydedilenler")
                    .document(gonderiIDleriFB.get(position))
                    .collection("Kaydedenler")
                    .document(firebaseUser.getEmail());

            documentReference1
                    .set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            // İşlem Başarılı
                        }
                    });

            Log.d(TAG, "onClick: Gönderi kaydedildi");
        }
    }

    @Override
    public void onDigerSeceneklerClick(int position) {
        Toast.makeText(getActivity(), "DİĞER", Toast.LENGTH_SHORT).show();
    }

}
