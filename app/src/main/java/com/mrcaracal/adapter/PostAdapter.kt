package com.mrcaracal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.adapter.PostAdapter.PostHolder
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.squareup.picasso.Picasso

class PostAdapter(
    val recyclerViewClickInterface: RecyclerViewClickInterface,
    var postModelList: ArrayList<PostModel> = arrayListOf()
) : RecyclerView.Adapter<PostHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recycler_row, parent, false)
        return PostHolder(view)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {

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

    inner class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var row_picturePath: ImageView = itemView.findViewById(R.id.row_picturePath)
        var row_placeName: TextView = itemView.findViewById(R.id.row_placeName)
        var row_comment: TextView = itemView.findViewById(R.id.row_comment)
        var ll_otherOperations: LinearLayout = itemView.findViewById(R.id.ll_otherOperations)

    }
}