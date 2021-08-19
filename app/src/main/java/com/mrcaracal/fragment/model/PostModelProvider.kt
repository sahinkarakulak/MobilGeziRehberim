package com.mrcaracal.fragment.model

import com.google.firebase.Timestamp

object PostModelProvider {

    fun provide(data: Map<String, Any>): PostModel {

        val postID = data["gonderiID"].toString()
        val userEmail = data["kullaniciEposta"].toString()
        var palceName = data["yerIsmi"].toString()
        palceName = palceName.substring(0, 1).uppercase() + palceName.substring(1)
        val location = data["konum"].toString()
        val pictureLink = data["resimAdresi"].toString()
        val comment = data["yorum"].toString()
        val addres = data["adres"].toString()
        val city = data["sehir"].toString()
        val postCode = data["postaKodu"].toString()
        val tags = data["taglar"].toString()
        val time = data["zaman"] as Timestamp

        return PostModel(
            postId = postID,
            userEmail = userEmail,
            pictureLink = pictureLink,
            location = location,
            placeName = palceName,
            comment = comment,
            postCode = postCode,
            tag = tags,
            address = addres,
            city = city,
            time = time
        )
    }

}