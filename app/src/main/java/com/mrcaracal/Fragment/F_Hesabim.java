package com.mrcaracal.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mrcaracal.Activity.HaritaKonumaGit;
import com.mrcaracal.Activity.ProfilDuzenle;
import com.mrcaracal.Adapter.RecyclerAdapterYapim;
import com.mrcaracal.Interface.RecyclerViewClickInterface;
import com.mrcaracal.mobilgezirehberim.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
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
    ArrayList<String> postaKoduFB;
    ArrayList<String> taglarFB;
    ArrayList<Timestamp> zamanlarFB;

    RecyclerView recyclerViewHesabim;

    RecyclerAdapterYapim recyclerAdapterYapim;

    TextView tv_kullaniciAdi, tv_kullaniciBio;
    Button btn_profili_duzenle;

    CircleImageView img_profil_resmi;

    int POSITION_DEGERI;
    String TABKONTROL = "paylasilanlar";
    // kaydedilenler

    SharedPreferences GET;
    SharedPreferences.Editor SET;

    double enlem;
    double boylam;

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
        postaKoduFB = new ArrayList<>();
        taglarFB = new ArrayList<>();
        zamanlarFB = new ArrayList<>();

        GET = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SET = GET.edit();

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_hesabim, container, false);
        init();

        paylasilanlariCek();

        // RecyclerView Tanımlama İşlemi
        recyclerViewHesabim = viewGroup.findViewById(R.id.recyclerViewHesabim);
        recyclerViewHesabim.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapterYapim = new RecyclerAdapterYapim(gonderiIDleriFB, kullaniciEpostalariFB, resimAdresleriFB, yerIsimleriFB, konumlariFB, adresleriFB, yorumlarFB, postaKoduFB, taglarFB, zamanlarFB, this);
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
                        TABKONTROL = "paylasilanlar";
                        listeTemizleme();
                        paylasilanlariCek();
                        Log.d(TAG, "onNavigationItemSelected: paylasilanlariCek() çağrıldı");
                        recyclerViewHesabim.scrollToPosition(0);
                        break;
                    case R.id.kaydedilenler:
                        Log.d(TAG, "onNavigationItemSelected: Kaydedilenler TAB'ı");
                        TABKONTROL = "kaydedilenler";
                        listeTemizleme();
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

    public void listeTemizleme(){
        gonderiIDleriFB.clear();
        kullaniciEpostalariFB.clear();
        resimAdresleriFB.clear();
        yerIsimleriFB.clear();
        konumlariFB.clear();
        adresleriFB.clear();
        yorumlarFB.clear();
        postaKoduFB.clear();
        taglarFB.clear();
        zamanlarFB.clear();
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

                                String gonderiID = verilerKumesiHesaim.get("gonderiID").toString();
                                String kullaniciEposta = verilerKumesiHesaim.get("kullaniciEposta").toString();
                                String yerIsmi = verilerKumesiHesaim.get("yerIsmi").toString();
                                yerIsmi = yerIsmi.substring(0, 1).toUpperCase() + yerIsmi.substring(1);
                                String konum = verilerKumesiHesaim.get("konum").toString();
                                String resimAdresi = verilerKumesiHesaim.get("resimAdresi").toString();
                                String yorum = verilerKumesiHesaim.get("yorum").toString();
                                String adres = verilerKumesiHesaim.get("adres").toString();
                                String postaKodu = verilerKumesiHesaim.get("postaKodu").toString();
                                Timestamp zaman = (Timestamp) verilerKumesiHesaim.get("zaman");

                                gonderiIDleriFB.add(gonderiID);
                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                konumlariFB.add(konum);
                                yorumlarFB.add(yorum);
                                postaKoduFB.add(postaKodu);
                                taglarFB.add(verilerKumesiHesaim.get("taglar").toString());
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

                                String gonderiID = verilerKumesiHesaim.get("gonderiID").toString();
                                String kullaniciEposta = verilerKumesiHesaim.get("kullaniciEposta").toString();
                                String yerIsmi = verilerKumesiHesaim.get("yerIsmi").toString();
                                yerIsmi = yerIsmi.substring(0, 1).toUpperCase() + yerIsmi.substring(1);
                                String konum = verilerKumesiHesaim.get("konum").toString();
                                String resimAdresi = verilerKumesiHesaim.get("resimAdresi").toString();
                                String yorum = verilerKumesiHesaim.get("yorum").toString();
                                String postaKodu = verilerKumesiHesaim.get("postaKodu").toString();
                                String adres = verilerKumesiHesaim.get("adres").toString();
                                Timestamp zaman = (Timestamp) verilerKumesiHesaim.get("zaman");

                                gonderiIDleriFB.add(gonderiID);
                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                konumlariFB.add(konum);
                                yorumlarFB.add(yorum);
                                postaKoduFB.add(postaKodu);
                                taglarFB.add(verilerKumesiHesaim.get("taglar").toString());
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

    public void paylasilanlardanKaldir() {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!

        //1. Adım
        firebaseFirestore
                .collection("Paylasilanlar")
                .document(kullaniciEpostalariFB.get(POSITION_DEGERI))
                .collection("Paylastiklari")
                .document(gonderiIDleriFB.get(POSITION_DEGERI))
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void aVoid) {
                        //
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //
                    }
                });

        //2. Adım
        firebaseFirestore
                .collection("Gonderiler")
                .document(gonderiIDleriFB.get(POSITION_DEGERI))
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void aVoid) {
                        //
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //
                    }
                });

    }

    public void kaydedilenlerdenKaldir() {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!

        firebaseFirestore
                .collection("Kaydedenler")
                .document(firebaseUser.getEmail())
                .collection("Kaydedilenler")
                .document(gonderiIDleriFB.get(POSITION_DEGERI))
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void aVoid) {
                        Toast.makeText(getActivity(), "Kaldırıldı", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "İşlem Başarısız", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void paylasilanlardanKonumaGit(){
        String[] gonderi_konumu = konumlariFB.get(POSITION_DEGERI).split(",");

        int belirtec = 0;

        for (String konumxy : gonderi_konumu) {
            belirtec++;
            if (belirtec == 1)
                enlem = Double.parseDouble(konumxy);
            if (belirtec == 2)
                boylam = Double.parseDouble(konumxy);
        }

        SET.putFloat("konum_git_enlem", (float) enlem);
        SET.putFloat("konum_git_boylam", (float) boylam);
        SET.commit();

        startActivity(new Intent(getActivity(), HaritaKonumaGit.class));

        Log.d(TAG, "Enlem: "+enlem+"   \tBoylam: "+boylam);
    }

    public void kaydedilenlerdenKonumaGit(){
        String[] gonderi_konumu = konumlariFB.get(POSITION_DEGERI).split(",");

        int belirtec = 0;

        for (String konumxy : gonderi_konumu) {
            belirtec++;
            if (belirtec == 1)
                enlem = Double.parseDouble(konumxy);
            if (belirtec == 2)
                boylam = Double.parseDouble(konumxy);
        }

        SET.putFloat("konum_git_enlem", (float) enlem);
        SET.putFloat("konum_git_boylam", (float) boylam);
        SET.commit();

        startActivity(new Intent(getActivity(), HaritaKonumaGit.class));

        Log.d(TAG, "Enlem: "+enlem+"   \tBoylam: "+boylam);
    }

    public void konumaGitIslemleri(int position) {
        String[] gonderi_konumu = konumlariFB.get(position).split(",");

        int belirtec = 0;

        for (String konumxy : gonderi_konumu) {
            belirtec++;
            if (belirtec == 1)
                enlem = Double.parseDouble(konumxy);
            if (belirtec == 2)
                boylam = Double.parseDouble(konumxy);
        }

        SET.putFloat("konum_git_enlem", (float) enlem);
        SET.putFloat("konum_git_boylam", (float) boylam);
        SET.commit();

        startActivity(new Intent(getActivity(), HaritaKonumaGit.class));

        Log.d(TAG, "Enlem: "+enlem+"   \tBoylam: "+boylam);

    }

    public String tagGoster(int position){

        String taggg = "";
        String al_taglar = taglarFB.get(position);
        int tag_uzunluk = al_taglar.length();
        String alinan_taglar;
        String[] a_t;

        switch (TABKONTROL){
            case "paylasilanlar":
                alinan_taglar = al_taglar.substring(1, tag_uzunluk-1);
                a_t = alinan_taglar.split(",");

                for(String tags : a_t){
                    Log.d(TAG, "onLongItemClick: "+tags.trim());
                    taggg += "#" + tags.trim() + " ";
                }
                break;

            case "kaydedilenler":
                alinan_taglar = al_taglar.substring(2, tag_uzunluk-2);
                a_t = alinan_taglar.split(",");

                for(String tags : a_t){
                    Log.d(TAG, "onLongItemClick: "+tags.trim());
                    taggg += "#" + tags.trim() + " ";
                }
                break;
        }

        return taggg;
    }

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    @Override
    public void onLongItemClick(int position) {
        Log.d(TAG, "onLongItemClick: Uzun tık");

        String tarih_ve_saat = DateFormat.getDateTimeInstance().format(zamanlarFB.get(position).toDate());
        String gonderi_detay_goster = yorumlarFB.get(position) + "\n\nPaylaşan: " + kullaniciEpostalariFB.get(position) +
                "\nTarih: " + tarih_ve_saat + "\nAdres: " + adresleriFB.get(position) +
                "\n\nEtiketler: " + tagGoster(position);

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

    @Override
    public void onDigerSeceneklerClick(int position) {
        POSITION_DEGERI = position;
        dialogPenceresiAc(position);
    }

    public void dialogPenceresiAc(int position) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity())
                .inflate(R.layout.layout_bottom_sheet_hesabim, viewGroup.findViewById(R.id.bottomSheetContainer_hesabim));

        TextView baslik = bottomSheetView.findViewById(R.id.bs_baslik);
        baslik.setText(yerIsimleriFB.get(position));

        // KONUMA GİT
        bottomSheetView.findViewById(R.id.bs_konuma_git).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (TABKONTROL){
                    case "paylasilanlar":
                        paylasilanlardanKonumaGit();
                        break;
                    case "kaydedilenler":
                        kaydedilenlerdenKonumaGit();
                        break;
                }
                bottomSheetDialog.dismiss();
            }
        });

        // KALDIR
        bottomSheetView.findViewById(R.id.bs_kaldir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (TABKONTROL) {
                    case "paylasilanlar":
                        paylasilanlardanKaldir();
                        listeTemizleme();
                        paylasilanlariCek();
                        recyclerViewHesabim.scrollToPosition(0);
                        break;
                    case "kaydedilenler":
                        kaydedilenlerdenKaldir();
                        listeTemizleme();
                        kaydedilenleriCek();
                        recyclerViewHesabim.scrollToPosition(0);
                        break;
                }

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