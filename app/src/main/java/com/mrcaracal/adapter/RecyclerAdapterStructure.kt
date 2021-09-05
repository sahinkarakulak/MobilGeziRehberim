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

class RecyclerAdapterStructure(
    val recyclerViewClickInterface: RecyclerViewClickInterface,
    var postModelList: ArrayList<PostModel> = arrayListOf()
) : RecyclerView.Adapter<GonderiHolder>() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GonderiHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recycler_row, parent, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()
        return GonderiHolder(view)
    }

    override fun onBindViewHolder(holder: GonderiHolder, position: Int) {

        holder.row_placeName.text = postModelList[position].placeName
        holder.row_comment.text = postModelList[position].comment
        Picasso.get()
            .load(postModelList[position].pictureLink)
            .centerCrop()
            .fit()
            .into(holder.row_picturePath)

        holder.ll_otherOperations.setOnClickListener {
            recyclerViewClickInterface.onOtherOperationsClick(
                postModelList[position]
            )
        }

        holder.row_picturePath.setOnLongClickListener {
            recyclerViewClickInterface.onLongItemClick(postModelList[position])
            false
        }
    }

    override fun getItemCount(): Int {
        return postModelList.size
    }

    inner class GonderiHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var row_picturePath: ImageView = itemView.findViewById(R.id.row_picturePath)
        var row_placeName: TextView = itemView.findViewById(R.id.row_placeName)
        var row_comment: TextView = itemView.findViewById(R.id.row_comment)
        var ll_otherOperations: LinearLayout = itemView.findViewById(R.id.ll_otherOperations)

    }
}