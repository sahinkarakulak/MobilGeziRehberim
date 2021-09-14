package com.mrcaracal.fragment.model

import java.io.Serializable

data class PostModel(

    val postId: String,
    val userEmail: String,
    val pictureLink: String,
    val placeName: String,
    val location: String,
    val comment: String,
    val postCode: String,
    val tag: String,
    val address: String,
    val city: String,
    val time: com.google.firebase.Timestamp

) : Serializable