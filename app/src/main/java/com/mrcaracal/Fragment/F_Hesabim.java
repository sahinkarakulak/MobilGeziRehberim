package com.mrcaracal.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.mrcaracal.Activity.ProfilDuzenle;
import com.mrcaracal.Adapter.RecyclerAdapterYapim;
import com.mrcaracal.Interface.RecyclerViewClickInterface;
import com.mrcaracal.Modul.Gonderiler;
import com.mrcaracal.mobilgezirehberim.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class F_Hesabim extends Fragment implements RecyclerViewClickInterface {

    private static final String TAG = "F_Hesabim";

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

    RecyclerView recyclerViewHesabim;

    RecyclerAdapterYapim recyclerAdapterYapim;

    TextView tv_kullaniciAdi, tv_kullaniciBio;
    Button btn_profili_duzenle;

    CircleImageView img_profil_resmi;

    ViewGroup viewGroup;

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

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
        viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_hesabim, container, false);
        init();

        paylasilanlariCek();

        // RecyclerView Tanımlama İşlemi
        recyclerViewHesabim = viewGroup.findViewById(R.id.recyclerViewHesabim);
        recyclerViewHesabim.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapterYapim = new RecyclerAdapterYapim(gonderiIDleriFB, kullaniciEpostalariFB, resimAdresleriFB, yerIsimleriFB, konumlariFB, adresleriFB, yorumlarFB, zamanlarFB, this);
        recyclerViewHesabim.setAdapter(recyclerAdapterYapim);

        img_profil_resmi = viewGroup.findViewById(R.id.img_profil_resmi);
        tv_kullaniciAdi = viewGroup.findViewById(R.id.tv_kullaniciAdi);
        tv_kullaniciBio = viewGroup.findViewById(R.id.tv_kullaniciBio);

        btn_profili_duzenle = viewGroup.findViewById(R.id.btn_profili_duzenle);

        btn_profili_duzenle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profili_duzenle = new Intent(getActivity(), ProfilDuzenle.class);
                startActivity(profili_duzenle);
                Log.d(TAG, "onClick: Kullanıcı A_ProfiliDuzenle'e yönlendirildi");
            }
        });

        DocumentReference documentReference = FirebaseFirestore
                .getInstance()
                .collection("Kullanicilar")
                .document(firebaseUser.getEmail());

        documentReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d(TAG, "onComplete: Veriler çekildi");
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                tv_kullaniciAdi.setText(documentSnapshot.getString("kullaniciAdi"));
                                tv_kullaniciBio.setText(documentSnapshot.getString("bio"));
                                Picasso.get().load(documentSnapshot.getString("kullaniciResmi")).into(img_profil_resmi);
                                Log.d(TAG, "onComplete: Çekilen veriler kullanıldı\n" + documentSnapshot.getString("kullaniciResmi"));

                                if (documentSnapshot.getString("kullaniciResmi") == null) {
                                    Picasso.get().load(R.drawable.defaultpp).into(img_profil_resmi);
                                    Log.d(TAG, "onComplete: Default resim kullanıldı");
                                }

                            }
                        }
                    }
                });

        BottomNavigationView bottomNavigationView = viewGroup.findViewById(R.id.bottomHesabimMenusu);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // Hangi TAB'a tıklanmışsa onu tespit ediyoruz.
                switch (item.getItemId()) {
                    case R.id.paylasilanlar:
                        Log.d(TAG, "onNavigationItemSelected: Paylaşılanlar TAB'ı");

                        gonderiIDleriFB.clear();
                        kullaniciEpostalariFB.clear();
                        resimAdresleriFB.clear();
                        yerIsimleriFB.clear();
                        konumlariFB.clear();
                        adresleriFB.clear();
                        yorumlarFB.clear();
                        zamanlarFB.clear();

                        paylasilanlariCek();
                        Log.d(TAG, "onNavigationItemSelected: paylasilanlariCek() çağrıldı");
                        recyclerViewHesabim.scrollToPosition(0);
                        break;
                    case R.id.kaydedilenler:
                        Log.d(TAG, "onNavigationItemSelected: Kaydedilenler TAB'ı");

                        gonderiIDleriFB.clear();
                        kullaniciEpostalariFB.clear();
                        resimAdresleriFB.clear();
                        yerIsimleriFB.clear();
                        konumlariFB.clear();
                        adresleriFB.clear();
                        yorumlarFB.clear();
                        zamanlarFB.clear();

                        kaydedilenleriCek();
                        Log.d(TAG, "onNavigationItemSelected: kaydedilenleriCek() çağrıldı");
                        recyclerViewHesabim.scrollToPosition(0);
                        break;
                }
                return true;
            }
        });

        return viewGroup;
    }

    public void paylasilanlariCek() {
        Log.d(TAG, "paylasilanlariCek: ...");
        CollectionReference collectionReference = firebaseFirestore
                .collection("Paylasilanlar")
                .document(firebaseUser.getEmail())
                .collection("Paylastiklari");

        collectionReference
                .orderBy("zaman", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "onComplete: ");
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                final Map<String, Object> verilerKumesiHesaim = documentSnapshot.getData();

                                String kullaniciEposta = verilerKumesiHesaim.get("kullaniciEposta").toString();
                                String yerIsmi = verilerKumesiHesaim.get("yerIsmi").toString();
                                yerIsmi = yerIsmi.substring(0, 1).toUpperCase() + yerIsmi.substring(1);
                                String resimAdresi = verilerKumesiHesaim.get("resimAdresi").toString();
                                String yorum = verilerKumesiHesaim.get("yorum").toString();
                                String adres = verilerKumesiHesaim.get("adres").toString();
                                Timestamp zaman = (Timestamp) verilerKumesiHesaim.get("zaman");

                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                yorumlarFB.add(yorum);
                                adresleriFB.add(adres);
                                zamanlarFB.add(zaman);

                                recyclerAdapterYapim.notifyDataSetChanged();
                                Log.d(TAG, "onComplete: Çekilen veriler RecyclerAdapterYapim'a gönderildi");
                            }
                        }
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

    public void kaydedilenleriCek() {
        Log.d(TAG, "kaydedilenleriCek: ...");

        CollectionReference collectionReference = firebaseFirestore
                .collection("Kaydedenler")
                .document(firebaseUser.getEmail())
                .collection("Kaydedilenler");

        collectionReference
                .orderBy("zaman", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "onComplete: Veriler filtrelenmiş olarak çekildi");
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                final Map<String, Object> verilerKumesiHesaim = documentSnapshot.getData();

                                String kullaniciEposta = verilerKumesiHesaim.get("kullaniciEposta").toString();
                                String yerIsmi = verilerKumesiHesaim.get("yerIsmi").toString();
                                yerIsmi = yerIsmi.substring(0, 1).toUpperCase() + yerIsmi.substring(1);
                                String resimAdresi = verilerKumesiHesaim.get("resimAdresi").toString();
                                String yorum = verilerKumesiHesaim.get("yorum").toString();
                                String adres = verilerKumesiHesaim.get("adres").toString();
                                Timestamp zaman = (Timestamp) verilerKumesiHesaim.get("zaman");

                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                yorumlarFB.add(yorum);
                                adresleriFB.add(adres);
                                zamanlarFB.add(zaman);

                                recyclerAdapterYapim.notifyDataSetChanged();
                                Log.d(TAG, "onComplete: Çekilen veriler RecyclerAdapterYapim'a gönderildi");
                            }
                        }
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

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    @Override
    public void onLongItemClick(int position) {
        Log.d(TAG, "onLongItemClick: Uzun tık");

        String tarih_ve_saat = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        String gonderi_detay_goster = yorumlarFB.get(position) + "\n\nPaylaşan: " + kullaniciEpostalariFB.get(position) + "\nTarih: " + tarih_ve_saat + "\nAdres: " + adresleriFB.get(position);

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert
                .setTitle(yerIsimleriFB.get(position))
                .setMessage(gonderi_detay_goster)
                .setNegativeButton("TAMAM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override
    public void onBaslikClick(int position) {

    }

    @Override
    public void onDigerSeceneklerClick(int position) {
        dialogPenceresiAc(position);
    }

    public void dialogPenceresiAc(int position) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity())
                .inflate( R.layout.layout_bottom_sheet, (LinearLayout) viewGroup.findViewById(R.id.bottomSheetContainer)  );

        // Gönderiyi Kaydet
        bottomSheetView.findViewById(R.id.bs_gonderiyi_kaydet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Bu gönderiyi siz paylaştınız veya kaydettiniz", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        // Hızlı Şikayet ET
        bottomSheetView.findViewById(R.id.bs_hizli_sikayet_et).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Detaylı Şikayet Bildir
        bottomSheetView.findViewById(R.id.bs_detayli_sikayet_bildir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // İPTAL butonu
        bottomSheetView.findViewById(R.id.bottom_sheet_iptal_btnsi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();


    }
}
