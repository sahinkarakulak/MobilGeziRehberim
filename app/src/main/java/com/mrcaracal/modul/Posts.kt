package com.mrcaracal.modul

import com.google.firebase.firestore.FieldValue

class Posts(
    val gonderiID: String? = null,
    val kullaniciEposta: String? = null,
    val resimAdresi: String? = null,
    val yerIsmi: String? = null,
    val konum: String? = null,
    val adres: String? = null,
    val sehir: String? = null,
    val yorum: String? = null,
    val postaKodu: String? = null,
    val taglar: List<String>? = null,
    val zaman: FieldValue? = null
)