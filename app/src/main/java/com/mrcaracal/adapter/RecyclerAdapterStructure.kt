package com.mrcaracal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.adapter.RecyclerAdapterStructure.GonderiHolder
import com.mrcaracal.mobilgezirehberim.R
import com.squareup.picasso.Picasso
import java.util.*

private const val TAG = "RecyclerAdapterStructur"

class RecyclerAdapterStructure(
    private val gonderiIDleriListesi: ArrayList<String>,
    private val kullaniciEpostalariListesi: ArrayList<String>,
    private val resimAdresleriListesi: ArrayList<String>,
    private val yerIsimleriListesi: ArrayList<String>,
    private val konumlariListesi: ArrayList<String>,
    private val adresleriListesi: ArrayList<String>,
    private val sehirListesi: ArrayList<String>,
    private val yorumlarListesi: ArrayList<String>,
    private val postaKodlari: ArrayList<String>,
    private val taglarListesi: ArrayList<String>,
    private val zamanlarListesi: ArrayList<Timestamp>,
    private val recyclerViewClickInterface: RecyclerViewClickInterface
) : RecyclerView.Adapter<GonderiHolder>() {

    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GonderiHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recycler_row, parent, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        firebaseFirestore = FirebaseFirestore.getInstance()
        return GonderiHolder(view)
    }

    override fun onBindViewHolder(holder: GonderiHolder, position: Int) {

        // Kullanıcıya gösterilen kısım
        /*holder.row_epostasi.setText(kullaniciEpostalariListesi.get(position));*/
        holder.row_placeName.text = yerIsimleriListesi[position]
        holder.row_comment.text = yorumlarListesi[position]
        Picasso.get()
            .load(resimAdresleriListesi[position])
            .centerCrop()
            .fit()
            .into(holder.row_picturePath)
        holder.row_picturePath.setOnClickListener { }
    }

    // kaç tane row olduğunu ayarlar - listemizde
    override fun getItemCount(): Int {
        return resimAdresleriListesi.size
    }

    //
    inner class GonderiHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var row_picturePath: ImageView
        var row_placeName: TextView
        var row_comment: TextView
        var ll_otherOperations: LinearLayout

        init {

            /*row_epostasi = itemView.findViewById(R.id.row_epostasi);*/
            row_picturePath = itemView.findViewById(R.id.row_picturePath)
            row_placeName = itemView.findViewById(R.id.row_placeName)
            row_comment = itemView.findViewById(R.id.row_comment)
            ll_otherOperations = itemView.findViewById(R.id.ll_otherOperations)

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
            });*/row_picturePath.setOnLongClickListener {
                recyclerViewClickInterface.onLongItemClick(adapterPosition)
                false
            }
            ll_otherOperations.setOnClickListener {
                recyclerViewClickInterface.onOtherOperationsClick(
                    adapterPosition
                )
            }
        }
    }

}