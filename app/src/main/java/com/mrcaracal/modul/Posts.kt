package com.mrcaracal.modul

import com.google.firebase.firestore.FieldValue

class Posts {
    var gonderiID: String? = null
    var kullaniciEposta: String? = null
    var resimAdresi: String? = null
    var yerIsmi: String? = null
    var konum: String? = null
    var adres: String? = null
    var sehir: String? = null
    var yorum: String? = null
    var postaKodu: String? = null
    var taglar: List<String>? = null
    var zaman: FieldValue? = null

    constructor(
        gonderiID: String?,
        kullaniciEposta: String?,
        resimAdresi: String?,
        yerIsmi: String?,
        konum: String?,
        adres: String?,
        sehir: String?,
        yorum: String?,
        postaKodu: String?,
        taglar: List<String>?,
        zaman: FieldValue?
    ) {
        this.gonderiID = gonderiID
        this.kullaniciEposta = kullaniciEposta
        this.resimAdresi = resimAdresi
        this.yerIsmi = yerIsmi
        this.konum = konum
        this.adres = adres
        this.sehir = sehir
        this.yorum = yorum
        this.postaKodu = postaKodu
        this.taglar = taglar
        this.zaman = zaman
    }
}