package com.mrcaracal.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.mrcaracal.Activity.HaritaKonumaGit;
import com.mrcaracal.Adapter.RecyclerAdapterYapim;
import com.mrcaracal.Interface.RecyclerViewClickInterface;
import com.mrcaracal.Modul.Gonderiler;
import com.mrcaracal.Modul.IletisimBilgileri;
import com.mrcaracal.Modul.Sehirler;
import com.mrcaracal.mobilgezirehberim.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    ArrayList<String> postaKoduFB;
    ArrayList<String> taglarFB;
    ArrayList<Timestamp> zamanlarFB;

    RecyclerView recycler_view_ara;

    RecyclerAdapterYapim recyclerAdapterYapim;

    ImageView img_konuma_gore_bul;
    EditText edt_anahtar_kelime_arat;
    Spinner sp_ara_neye_gore, sp_sehirler;
    String anahtar_kelimemiz = "yerIsmi";
    SharedPreferences GET;
    SharedPreferences.Editor SET;
    double enlem;
    double boylam;
    ViewGroup viewGroup;
    private final String[] neye_gore = {"Yer İsmi", "Etiket", "Şehir", "Kullanıcı"};
    private ArrayAdapter<String> sp_adapter_neye_gore;
    private ArrayAdapter<String> sp_adapter_sehirler;

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
        postaKoduFB = new ArrayList<>();
        taglarFB = new ArrayList<>();
        zamanlarFB = new ArrayList<>();

        GET = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SET = GET.edit();

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_ara, container, false);
        init();

        img_konuma_gore_bul = viewGroup.findViewById(R.id.img_konuma_gore_bul);
        img_konuma_gore_bul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bizim konuma yakın yerlerin listelenmesini sağlanacak

                // Kullanıcının anlık konumu alınacak. konumla beraber posta kodu da alınacak.
                // Böylelikle posta kodundaki ilk 2 haneye göre veriler çekilecek.
                // Örnek: Posta kodu 12200 ise gönderiler arasında posta kodu 12 ile başlayan tüm gönderiler çekilecek.
                Toast.makeText(getActivity(), "Yakın yerler listeleniyor", Toast.LENGTH_SHORT).show();
            }
        });

        sp_ara_neye_gore = viewGroup.findViewById(R.id.sp_ara_neye_gore);
        sp_adapter_neye_gore = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, neye_gore);
        sp_adapter_neye_gore.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_ara_neye_gore.setAdapter(sp_adapter_neye_gore);

        sp_ara_neye_gore.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItem().toString().equals(neye_gore[0])) {

                    edt_anahtar_kelime_arat.setVisibility(View.VISIBLE);
                    sp_sehirler.setVisibility(View.INVISIBLE);

                    // Yer İsmine göre işlemler yapılsın.
                    listeTemizleme();
                    recycler_view_ara.scrollToPosition(0);
                    anahtar_kelimemiz = "yerIsmi";
                }

                if (parent.getSelectedItem().toString().equals(neye_gore[1])) {

                    edt_anahtar_kelime_arat.setVisibility(View.VISIBLE);
                    sp_sehirler.setVisibility(View.INVISIBLE);

                    // Etikete göre işlemler yapılsın.
                    listeTemizleme();
                    recycler_view_ara.scrollToPosition(0);
                    anahtar_kelimemiz = "taglar";
                }

                // Bu item seçilirse Ara çubuğu spinner'a dönüşsün ve orada şehirler listelensin.
                // Seçilen şehre göre posta kodu değeri alınsın ve VT de ona göre bir arama yapılsın.
                if (parent.getSelectedItem().toString().equals(neye_gore[2])) {

                    edt_anahtar_kelime_arat.setVisibility(View.INVISIBLE);
                    sp_sehirler.setVisibility(View.VISIBLE);

                    listeTemizleme();
                    recycler_view_ara.scrollToPosition(0);
                    anahtar_kelimemiz = "sehir";

                }

                if (parent.getSelectedItem().toString().equals(neye_gore[3])) {

                    edt_anahtar_kelime_arat.setVisibility(View.VISIBLE);
                    sp_sehirler.setVisibility(View.INVISIBLE);

                    // Kullanıcıya göre işlemler yapılsın.
                    listeTemizleme();
                    recycler_view_ara.scrollToPosition(0);
                    anahtar_kelimemiz = "kullaniciEposta";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Sehirler sehirler_al = new Sehirler();
        sp_sehirler = viewGroup.findViewById(R.id.sp_sehirler);
        sp_adapter_sehirler = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, sehirler_al.sehirler);
        sp_adapter_sehirler.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_sehirler.setAdapter(sp_adapter_sehirler);

        sp_sehirler.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                listeTemizleme();
                recycler_view_ara.scrollToPosition(0);

                if (anahtar_kelimemiz.equals("sehir")) {
                    Sehirler sehirler = new Sehirler();
                    String secilen_sehir_kodu = sehirler.sehirler(parent.getSelectedItem().toString());

                    if (secilen_sehir_kodu.equals("Şehir Seçin!")) {
                        Toast.makeText(getActivity(), "Lütfen Şehir Seçin!", Toast.LENGTH_SHORT).show();
                    } else{
                        // VT'de Gonderiler bölümünde posta kodu alınan değerle başlayan tüm gonderileri çeken bir algoritma geliştir.

                        aramaYapSehirIcin(secilen_sehir_kodu);

                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        edt_anahtar_kelime_arat = viewGroup.findViewById(R.id.edt_anahtar_kelime_arat);
        edt_anahtar_kelime_arat.addTextChangedListener(new TextWatcher() {

            // Önce
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: Önce");
                //
            }

            // Esnasında
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: Esnasında");
                listeTemizleme();
                if (anahtar_kelimemiz.equals("taglar")) {
                    aramaYapEtiketIcin(anahtar_kelimemiz, s.toString().toLowerCase());
                } else {
                    aramaYap(anahtar_kelimemiz, s.toString().toLowerCase());
                }

            }

            // Sonra
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: Sonra");
                //
            }
        });


        recycler_view_ara = viewGroup.findViewById(R.id.recycler_view_ara);
        recycler_view_ara.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapterYapim = new RecyclerAdapterYapim(gonderiIDleriFB, kullaniciEpostalariFB, resimAdresleriFB, yerIsimleriFB, konumlariFB, adresleriFB, yorumlarFB, postaKoduFB, taglarFB, zamanlarFB, this);
        recycler_view_ara.setAdapter(recyclerAdapterYapim);

        return viewGroup;
    }

    public void listeTemizleme() {
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

    public void aramaYap(String ilgiliAlan, String anahtarKelime) {
        Log.d(TAG, "aramaYap: Çalıştı");
        listeTemizleme();

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
                                String postaKodu = verilerKumesi.get("postaKodu").toString();
                                Timestamp zaman = (Timestamp) verilerKumesi.get("zaman");

                                gonderiIDleriFB.add(gonderiID);
                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                konumlariFB.add(konum);
                                adresleriFB.add(adres);
                                yorumlarFB.add(yorum);
                                postaKoduFB.add(postaKodu);
                                taglarFB.add(verilerKumesi.get("taglar").toString());
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

    public void aramaYapEtiketIcin(String ilgiliAlan, String anahtarKelime) {
        Log.d(TAG, "aramaYapEtiketIcin: Çalıştı");
        listeTemizleme();

        CollectionReference collectionReference = firebaseFirestore
                .collection("Gonderiler");

        collectionReference
                .whereArrayContains(ilgiliAlan, anahtarKelime)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                Map<String, Object> verilerKumesi = documentSnapshot.getData();

                                String gonderiID = verilerKumesi.get("gonderiID").toString();
                                String kullaniciEposta = verilerKumesi.get("kullaniciEposta").toString();
                                String yerIsmi = verilerKumesi.get("yerIsmi").toString();
                                yerIsmi = yerIsmi.substring(0, 1).toUpperCase() + yerIsmi.substring(1);
                                String resimAdresi = verilerKumesi.get("resimAdresi").toString();
                                String konum = verilerKumesi.get("konum").toString();
                                String adres = verilerKumesi.get("adres").toString();
                                String yorum = verilerKumesi.get("yorum").toString();
                                String postaKodu = verilerKumesi.get("postaKodu").toString();
                                Timestamp zaman = (Timestamp) verilerKumesi.get("zaman");

                                gonderiIDleriFB.add(gonderiID);
                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                konumlariFB.add(konum);
                                adresleriFB.add(adres);
                                yorumlarFB.add(yorum);
                                postaKoduFB.add(postaKodu);
                                taglarFB.add(verilerKumesi.get("taglar").toString());
                                zamanlarFB.add(zaman);

                                recyclerAdapterYapim.notifyDataSetChanged();
                                Log.d(TAG, "onComplete: Sonu...");
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void aramaYapSehirIcin(String postaKodu){
        Log.d(TAG, "aramaYapSehirIcin: Çalıştı");

        CollectionReference collectionReference = firebaseFirestore
                .collection("Gonderiler");

        collectionReference
                .orderBy("postaKodu")
                .startAt(postaKodu)
                .endAt(postaKodu + "\uf8ff")
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
                                String postaKodu = verilerKumesi.get("postaKodu").toString();
                                Timestamp zaman = (Timestamp) verilerKumesi.get("zaman");

                                gonderiIDleriFB.add(gonderiID);
                                kullaniciEpostalariFB.add(kullaniciEposta);
                                resimAdresleriFB.add(resimAdresi);
                                yerIsimleriFB.add(yerIsmi);
                                konumlariFB.add(konum);
                                adresleriFB.add(adres);
                                yorumlarFB.add(yorum);
                                postaKoduFB.add(postaKodu);
                                taglarFB.add(verilerKumesi.get("taglar").toString());
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

    public String tagGoster(int position) {

        String taggg = "";
        String al_taglar = taglarFB.get(position);
        int tag_uzunluk = al_taglar.length();
        String alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1);
        String[] a_t = alinan_taglar.split(",");

        for (String tags : a_t) {
            Log.d(TAG, "onLongItemClick: " + tags.trim());
            taggg += "#" + tags.trim() + " ";
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

    public void kaydet_islemleri(int position) {
        if (kullaniciEpostalariFB.get(position).equals(firebaseUser.getEmail())) {
            Toast.makeText(getActivity(), "Bunu zaten siz paylaştınız", Toast.LENGTH_SHORT).show();
        } else {

            Gonderiler MGonderiler = new Gonderiler(gonderiIDleriFB.get(position), kullaniciEpostalariFB.get(position), resimAdresleriFB.get(position), yerIsimleriFB.get(position), konumlariFB.get(position), adresleriFB.get(position), yorumlarFB.get(position), postaKoduFB.get(position), Collections.singletonList(taglarFB.get(position)), FieldValue.serverTimestamp());

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


            // Aşağıdaki işlemler gerekmiyor. Ama şimdilik burada bulunsun
            /*Map<String, Object> map = new HashMap();
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
                    });*/

            Log.d(TAG, "onClick: Gönderi kaydedildi");
        }
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

        Log.d(TAG, "Enlem: " + enlem + "   \tBoylam: " + boylam);

    }

    @Override
    public void onDigerSeceneklerClick(int position) {
        dialogPenceresiAc(position);
    }

    public void dialogPenceresiAc(int position) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity())
                .inflate(R.layout.layout_bottom_sheet, (LinearLayout) viewGroup.findViewById(R.id.bottomSheetContainer));

        // Gönderiyi Kaydet
        bottomSheetView.findViewById(R.id.bs_gonderiyi_kaydet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kaydet_islemleri(position);
                bottomSheetDialog.dismiss();
            }
        });

        // Konuma Git
        bottomSheetView.findViewById(R.id.bs_konuma_git).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                konumaGitIslemleri(position);
                bottomSheetDialog.dismiss();
            }
        });

        // Detaylı Şikayet Bildir
        bottomSheetView.findViewById(R.id.bs_detayli_sikayet_bildir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (kullaniciEpostalariFB.get(position).equals(firebaseUser.getEmail())) {
                    Toast.makeText(getActivity(), "Bunu zaten siz paylaştınız", Toast.LENGTH_SHORT).show();
                } else {

                    IletisimBilgileri iletisimBilgileri = new IletisimBilgileri();

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_EMAIL, iletisimBilgileri.getAdmin_hesaplari());
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, "");
                    intent.setType("plain/text");
                    startActivity(Intent.createChooser(intent, "Ne ile göndermek istersiniz?"));

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