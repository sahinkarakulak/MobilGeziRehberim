package com.mrcaracal.modul

import com.google.firebase.firestore.FieldValue

class Posts(
    val postId: String? = null,
    val userEmail: String? = null,
    val pictureLink: String? = null,
    val placeName: String? = null,
    val location: String? = null,
    val address: String? = null,
    val city: String? = null,
    val comment: String? = null,
    val postCode: String? = null,
    val tags: List<String>? = null,
    val time: FieldValue? = null
)