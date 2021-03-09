package com.mrcaracal.Activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.mrcaracal.mobilgezirehberim.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class ProfilDuzenle extends AppCompatActivity {

    private static final String TAG = "ProfilDuzenle";

    FirebaseUser firebaseUser;
    StorageReference storageReference;
    ImageView img_kullaniciResmi;
    TextView tv_kullaniciResmiDegistir;
    EditText et_kullaniciAdiAl, et_biyografiAl;
    TextView txt_kullanici_ePosta_adresi;
    Button btn_guncelle;
    DocumentReference documentReference;
    private Uri mImageUri;
    private StorageTask uploadTask;

    private void initialize() {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Resimler");

        img_kullaniciResmi = findViewById(R.id.img_kullaniciResmi);
        tv_kullaniciResmiDegistir = findViewById(R.id.tv_kullaniciResmiDegistir);
        et_kullaniciAdiAl = findViewById(R.id.et_kullaniciAdiAl);
        et_biyografiAl = findViewById(R.id.et_biyografiAl);
        txt_kullanici_ePosta_adresi = findViewById(R.id.txt_kullanici_ePosta_adresi);
        btn_guncelle = findViewById(R.id.btn_guncelle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_duzenle);
        initialize();

        setTitle("Profili Düzenle");

        txt_kullanici_ePosta_adresi.setText(firebaseUser.getEmail());

        documentReference = FirebaseFirestore
                .getInstance()
                .collection("Kullanicilar")
                .document(firebaseUser.getEmail());

        documentReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {

                                et_kullaniciAdiAl.setText(documentSnapshot.getString("kullaniciAdi"));
                                et_biyografiAl.setText(documentSnapshot.getString("bio"));
                                Picasso.get().load(documentSnapshot.getString("kullaniciResmi")).into(img_kullaniciResmi);
                                Log.d(TAG, "onComplete: VT'den kullanıcı bilgileri alındı ve gösterildi");
                            }

                        }
                    }
                });

        tv_kullaniciResmiDegistir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage
                        .activity()
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(ProfilDuzenle.this);
            }
        });

        img_kullaniciResmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage
                        .activity()
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(ProfilDuzenle.this);
            }
        });

        btn_guncelle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profiliGuncelle(et_kullaniciAdiAl.getText().toString(), et_biyografiAl.getText().toString());
                Log.d(TAG, "onClick: EditText'en alınan veriler parametre olarak gönderildi");
            }
        });

    }

    private void profiliGuncelle(String k_adi, String k_bio) {
        DocumentReference documentReference2 = FirebaseFirestore.getInstance()
                .collection("Kullanicilar")
                .document(firebaseUser.getEmail());

        Map<String, Object> guncelVeriler = new HashMap<>();
        guncelVeriler.put("kullaniciAdi", k_adi);
        guncelVeriler.put("bio", k_bio);

        documentReference2
                .update(guncelVeriler)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Sadece istenen veriler güncellendi");
                        startActivity(new Intent(ProfilDuzenle.this, AnaSayfa.class));
                        Log.d(TAG, "onSuccess: Kullanıcı A_Anasayfaya yönlendirildi");
                    }
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {

        if (mImageUri != null) {
            Log.d(TAG, "uploadImage: Koşul sağlandı");
            StorageReference storageReference2 = storageReference.child(firebaseUser.getEmail()).child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            uploadTask = storageReference2.putFile(mImageUri);
            Log.d(TAG, "uploadImage: Resim yolu alındı");
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return storageReference2.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        Map<String, Object> hasmap = new HashMap<>();
                        hasmap.put("kullaniciResmi", "" + myUrl);

                        FirebaseFirestore.getInstance()
                                .collection("Kullanicilar")
                                .document(firebaseUser.getEmail())
                                .update(hasmap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Log.d(TAG, "onSuccess: VT'de resim yolu değiştirildi");

                                        documentReference
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            if (documentSnapshot.exists()) {

                                                                Picasso.get().load(documentSnapshot.getString("kullaniciResmi")).into(img_kullaniciResmi);
                                                                Log.d(TAG, "onComplete: Resim yolu çekilip Picasso ile yayınlandı");
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                });
                    } else {
                        Toast.makeText(ProfilDuzenle.this, "Bir hata gerçekleşti", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfilDuzenle.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "Resim seçilmedi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            uploadImage();

            Toast.makeText(this, "Profil resmi güncellendi", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onActivityResult: Profil resmi güncellendi");
        } else {
            Toast.makeText(this, "Vaz mı geçtin?", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onActivityResult: İşlemden vazgeçildi");
        }

    }
}