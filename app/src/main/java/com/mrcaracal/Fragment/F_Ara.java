package com.mrcaracal.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mrcaracal.Adapter.RecyclerAdapterYapim;
import com.mrcaracal.Interface.RecyclerViewClickInterface;
import com.mrcaracal.Modul.Gonderiler;
import com.mrcaracal.mobilgezirehberim.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class F_Ara extends Fragment implements RecyclerViewClickInterface {

    private static final String TAG = "F_Ara";

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

    RecyclerView recycler_view_ara;

    RecyclerAdapterYapim recyclerAdapterYapim;

    ImageView img_konuma_gore_bul;
    EditText edt_anahtar_kelime_arat;

    ViewGroup viewGroup;

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
        viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_ara, container, false);
        init();

        img_konuma_gore_bul = viewGroup.findViewById(R.id.img_konuma_gore_bul);
        img_konuma_gore_bul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getActivity(), Harita.class));
                // Bu konuma yakın yerlerin listelenmesini sağla
                Toast.makeText(getActivity(), "Yakın yerler listeleniyor", Toast.LENGTH_SHORT).show();
            }
        });

        edt_anahtar_kelime_arat = viewGroup.findViewById(R.id.edt_anahtar_kelime_arat);
        edt_anahtar_kelime_arat.addTextChangedListener(new TextWatcher() {

            final boolean _ignore = false; // indicates if the change was made by the TextWatcher itself.

            // Önce
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: Önce");

            }

            // Esnasında
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: Esnasında");

                gonderiIDleriFB.clear();
                kullaniciEpostalariFB.clear();
                resimAdresleriFB.clear();
                yerIsimleriFB.clear();
                konumlariFB.clear();
                adresleriFB.clear();
                yorumlarFB.clear();
                zamanlarFB.clear();

                /*String[] ilgiliAlan = {"kullaniciEposta", "yerIsmi"};
                for (int i = 0; i < ilgiliAlan.length; i++) {
                    Log.d(TAG, "onTextChanged: " + ilgiliAlan[i] + " - alanı gönderiliyor");
                    aramaYap(ilgiliAlan[i], s.toString().toLowerCase());
                }*/

                aramaYap("yerIsmi", s.toString().toLowerCase());

            }

            // Sonra
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: Sonra");

            }
        });

        recycler_view_ara = viewGroup.findViewById(R.id.recycler_view_ara);
        recycler_view_ara.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapterYapim = new RecyclerAdapterYapim(gonderiIDleriFB, kullaniciEpostalariFB, resimAdresleriFB, yerIsimleriFB, konumlariFB, adresleriFB, yorumlarFB, zamanlarFB, this);
        recycler_view_ara.setAdapter(recyclerAdapterYapim);

        return viewGroup;
    }

    public void aramaYap(String ilgiliAlan, String anahtarKelime) {
        Log.d(TAG, "aramaYap: ");

        gonderiIDleriFB.clear();
        kullaniciEpostalariFB.clear();
        resimAdresleriFB.clear();
        yerIsimleriFB.clear();
        konumlariFB.clear();
        adresleriFB.clear();
        yorumlarFB.clear();
        zamanlarFB.clear();

        //recycler_view_ara.scrollToPosition(0);

        CollectionReference collectionReference = firebaseFirestore
                .collection("Gonderiler");

        collectionReference
                .orderBy(ilgiliAlan)
                .startAt(anahtarKelime)
                .endAt(anahtarKelime + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "onComplete: Veriler filtrelenmiş şekilde çekildi");
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot snapshot : querySnapshot) {
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
                                Log.d(TAG, "onComplete: Sonu...");

                                // Arraylistlerin içinde tüm özellikleriyle aynı olan gönderiler var ise aynı olanların 1 tanesi hariç hepsini ArrayList'n çıkar.
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
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
        //String tarih = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        String gonderi_detay_goster = yorumlarFB.get(position) + "\n\nPaylaşan: " + kullaniciEpostalariFB.get(position) + "\nTarih: " + tarih_ve_saat + "\nAdres: " + adresleriFB.get(position);
        //String gonderi_detay_goster = "Paylaşan: " + kullaniciEpostalariFB.get(position) + "\n\n" + yorumlarFB.get(position);

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert
                .setTitle(yerIsimleriFB.get(position))
                .setMessage(gonderi_detay_goster)
                .setNegativeButton("TAMAM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .show();
    }

    public void kaydet_islemleri(int position){
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
            map.put("IDsi", gonderiIDleriFB.get(position));

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
                kaydet_islemleri(position);
                bottomSheetDialog.dismiss();
            }
        });

        // Hızlı Şikayet ET
        bottomSheetView.findViewById(R.id.bs_hizli_sikayet_et).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Bu bölüm henüz kodlanmadı", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        // Detaylı Şikayet Bildir
        bottomSheetView.findViewById(R.id.bs_detayli_sikayet_bildir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Bu bölüm henüz kodlanmadı", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
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
