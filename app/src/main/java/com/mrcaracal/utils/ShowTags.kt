package com.mrcaracal.utils

import com.mrcaracal.fragment.model.PostModel

object ShowTags {

    fun showPostTags(postModel: PostModel): String {
        var tagsToReturn = ""
        val tagsTakenByEditText = postModel.tag
        val tagLength = tagsTakenByEditText.length
        val tagTaken = tagsTakenByEditText.substring(1, tagLength - 1)
        val tagShredding = tagTaken.split(",").toTypedArray()
        for (tags: String in tagShredding) {
            tagsToReturn += "#" + tags.trim { it <= ' ' } + " "
        }
        return tagsToReturn
    }

    fun showPostTagsForAccount(postModel: PostModel, tabControl: String): String {
        var tagsToReturn = ""
        val tagsTakenByEditText = postModel.tag
        val tagLength = tagsTakenByEditText.length
        val tagTaken: String
        val tagShredding: Array<String>
        when (tabControl) {
            "paylasilanlar" -> {
                tagTaken = tagsTakenByEditText.substring(1, tagLength - 1)
                tagShredding = tagTaken.split(",").toTypedArray()
                for (tags: String in tagShredding) {
                    tagsToReturn += "#" + tags.trim { it <= ' ' } + " "
                }
            }
            "kaydedilenler" -> {
                tagTaken = tagsTakenByEditText.substring(2, tagLength - 2)
                tagShredding = tagTaken.split(",").toTypedArray()
                for (tags: String in tagShredding) {
                    tagsToReturn += "#" + tags.trim { it <= ' ' } + " "
                }
            }
        }
        return tagsToReturn
    }
}