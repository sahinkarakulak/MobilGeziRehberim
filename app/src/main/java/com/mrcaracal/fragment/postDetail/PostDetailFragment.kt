package com.mrcaracal.fragment.postDetail

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.databinding.FragmentPostDetailBinding
import com.squareup.picasso.Picasso
import java.text.DateFormat

private const val TAG = "PostDetailFragment"

class PostDetailFragment : Fragment() {

    private lateinit var binding : FragmentPostDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostDetailBinding.inflate(layoutInflater)

        val bundle = this.arguments
        val tags = bundle?.getString("postTags", "")
        val postModel = bundle?.getSerializable("postModel") as PostModel
        val dateAndTime = DateFormat.getDateTimeInstance().format(
            postModel.time.toDate()
        )
        Log.i(TAG, "onCreateView: " + postModel.pictureLink.toString())

        /*val testImage = "https://firebasestorage.googleapis.com/v0/b/mobilgezirehberim-7aca5.appspot.com/o/Resimler%2Fkarakulaksahin%40gmail.com--An%C4%B1tkabir--062cca41-1a47-4e20-90be-dca0c430e302?alt=media&token=9f184020-1b18-4542-8fe4-2e17ccb4e293"
        val imgUri = Uri.parse(postModel.pictureLink)
        binding.postDetailImageView.setImageURI(imgUri)*/

        Picasso.get()
            .load(postModel.pictureLink)
            .centerCrop()
            .fit()
            .into(binding.postDetailImageView)

        binding.postDetailTitle.text = postModel.placeName
        binding.postDetailComment.text = postModel.comment
        binding.postDetailSharing.text = postModel.userEmail
        binding.postDetailDate.text = dateAndTime
        binding.postDetailAddress.text = postModel.address
        binding.postDetailLabels.text = tags.toString()

        return binding.root
    }
}