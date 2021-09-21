package com.mrcaracal.fragment.postDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mrcaracal.extensions.loadUrl
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.databinding.FragmentPostDetailBinding
import java.text.DateFormat

private const val TAG = "PostDetailFragment"

class PostDetailFragment : Fragment() {

    private lateinit var binding: FragmentPostDetailBinding

    lateinit var postModel: PostModel
    var tags: String = ""
    var dateAndTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initViews()
    }

    private fun initArgs() {
        val bundle = this.arguments
        tags = bundle?.getString(KEY_POST_TAG, "").orEmpty()
        postModel = bundle?.getSerializable("postModel") as PostModel
        dateAndTime = DateFormat.getDateTimeInstance().format(
            postModel.time.toDate()
        )
    }

    private fun initViews() = with(binding) {
        postDetailImageView.loadUrl(postModel.pictureLink)
        postDetailTitle.text = postModel.placeName
        postDetailComment.text = postModel.comment
        postDetailSharing.text = postModel.userEmail
        postDetailDate.text = dateAndTime
        postDetailAddress.text = postModel.address
        postDetailLabels.text = tags.toString()

    }

    companion object {
        const val KEY_POST_TAG = "postTags"
    }

}