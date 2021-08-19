package com.mrcaracal.Interface

import com.mrcaracal.fragment.model.PostModel

interface RecyclerViewClickInterface {
    fun onLongItemClick(postModel: PostModel)
    fun onOtherOperationsClick(postModel: PostModel)
}