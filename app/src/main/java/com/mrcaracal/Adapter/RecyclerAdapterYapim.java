package com.mrcaracal.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mrcaracal.Interface.RecyclerViewClickInterface;
import com.mrcaracal.mobilgezirehberim.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerAdapterYapim extends RecyclerView.Adapter<RecyclerAdapterYapim.GonderiHolder> {

    private static final String TAG = "RecyclerAdapterYapim";

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    private final ArrayList<String> gonderiIDleriListesi;
    private final ArrayList<String> kullaniciEpostalariListesi;
    private final ArrayList<String> resimAdresleriListesi;
    private final ArrayList<String> yerIsimleriListesi;
    private final ArrayList<String> konumlariListesi;
    private final ArrayList<String> adresleriListesi;
    private final ArrayList<String> yorumlarListesi;
    private final ArrayList<com.google.firebase.Timestamp> zamanlarListesi;

    private final RecyclerViewClickInterface recyclerViewClickInterface;

    public RecyclerAdapterYapim(ArrayList<String> gonderiIDleriListesi, ArrayList<String> kullaniciEpostalariListesi, ArrayList<String> resimAdresleriListesi, ArrayList<String> yerIsimleriListesi, ArrayList<String> konumlariListesi, ArrayList<String> adresleriListesi, ArrayList<String> yorumlarListesi, ArrayList<Timestamp> zamanlarListesi, RecyclerViewClickInterface recyclerViewClickInterface) {
        this.gonderiIDleriListesi = gonderiIDleriListesi;
        this.kullaniciEpostalariListesi = kullaniciEpostalariListesi;
        this.resimAdresleriListesi = resimAdresleriListesi;
        this.yerIsimleriListesi = yerIsimleriListesi;
        this.konumlariListesi = konumlariListesi;
        this.adresleriListesi = adresleriListesi;
        this.yorumlarListesi = yorumlarListesi;
        this.zamanlarListesi = zamanlarListesi;
        this.recyclerViewClickInterface = recyclerViewClickInterface;
    }

    @NonNull
    @Override
    public GonderiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row, parent, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new GonderiHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GonderiHolder holder, int position) {

        // Kullanıcıya gösterilen kısım
        /*holder.row_epostasi.setText(kullaniciEpostalariListesi.get(position));*/
        holder.row_yerIsmi.setText(yerIsimleriListesi.get(position));
        holder.row_YorumBilgisi.setText(yorumlarListesi.get(position));
        Picasso.get()
                .load(resimAdresleriListesi.get(position))
                .centerCrop()
                .fit()
                .into(holder.row_resimAdresi);

        holder.row_resimAdresi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Log.d(TAG, "onBindViewHolder: " + "Veriler çekildi ve işlendi");
    }

    // kaç tane row olduğunu ayarlar - listemizde
    @Override
    public int getItemCount() {

        return resimAdresleriListesi.size();
    }

    //
    class GonderiHolder extends RecyclerView.ViewHolder {

        ImageView row_resimAdresi, row_digerSecenekler;
        TextView row_yerIsmi, row_YorumBilgisi;

        public GonderiHolder(@NonNull View itemView) {
            super(itemView);

            /*row_epostasi = itemView.findViewById(R.id.row_epostasi);*/
            row_resimAdresi = itemView.findViewById(R.id.row_resimAdresi);
            row_yerIsmi = itemView.findViewById(R.id.row_yerIsmi);
            row_YorumBilgisi = itemView.findViewById(R.id.row_YorumBilgisi);
            row_digerSecenekler = itemView.findViewById(R.id.row_digerSecenekler);

            // position'a göre hangisine tıklandıysa position'u bunun için oluşturulan RecyclerViewClickInterface'e göndersin.
            // RecyclerViewClickInterface'i hangi sınıf impelements edecekse orada kullanılsın.
            /*itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewClickInterface.onItemClick(getAdapterPosition());
                }
            });*/

            /*itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    recyclerViewClickInterface.onLongItemClick(getAdapterPosition());
                    return false;
                }
            });*/

            row_resimAdresi.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    recyclerViewClickInterface.onLongItemClick(getAdapterPosition());
                    return false;
                }
            });

            row_yerIsmi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewClickInterface.onBaslikClick(getAdapterPosition());
                }
            });

            row_digerSecenekler.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewClickInterface.onDigerSeceneklerClick(getAdapterPosition());
                }
            });

        }
    }

}