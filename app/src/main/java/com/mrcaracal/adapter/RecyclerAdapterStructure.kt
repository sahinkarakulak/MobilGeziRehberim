package com.mrcaracal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.adapter.RecyclerAdapterStructure.GonderiHolder
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class RecyclerAdapterStructure(
    val recyclerViewClickInterface: RecyclerViewClickInterface,
    var postModelList: ArrayList<PostModel> = arrayListOf()
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
        holder.row_placeName.text = postModelList[position].placeName
        holder.row_comment.text = postModelList[position].comment
        Picasso.get()
            .load(postModelList[position].pictureLink)
            .centerCrop()
            .fit()
            .into(holder.row_picturePath)

        holder.ll_otherOperations.setOnClickListener {
            recyclerViewClickInterface.onOtherOperationsClick(
                postModelList.get(position)
            )
        }

        holder.row_picturePath.setOnLongClickListener {
            recyclerViewClickInterface.onLongItemClick(postModelList.get(position))
            false
        }
    }

    // kaç tane row olduğunu ayarlar - listemizde
    override fun getItemCount(): Int {
        return postModelList.size
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
            });*/


        }
    }

}