package com.mrcaracal.modul

import java.util.*

class Cities {
    var cities = arrayOf(
        "Şehir Seç",
        "Adana",
        "Adıyaman",
        "Afyon",
        "Ağrı",
        "Aksaray",
        "Amasya",
        "Ankara",
        "Antalya",
        "Ardahan",
        "Artvin",
        "Aydın",
        "Balıkesir",
        "Bartın",
        "Batman",
        "Bayburt",
        "Bilecik",
        "Bingöl",
        "Bitlis",
        "Bolu",
        "Burdur",
        "Bursa",
        "Çanakkale",
        "Çankırı",
        "Çorum",
        "Denizli",
        "Diyarbakır",
        "Edirne",
        "Elazığ",
        "Erzincan",
        "Erzurum",
        "Eskişehir",
        "Gaziantep",
        "Giresun",
        "Gümüşhane",
        "Hakkari",
        "Hatay",
        "Iğdır",
        "Isparta",
        "İçel (Mersin)",
        "İstanbul",
        "İzmir",
        "Karabük",
        "Karaman",
        "Kars",
        "Kastamonu",
        "Kayseri",
        "Kırıkkale",
        "Kırklareli",
        "Kırşehir",
        "Kilis",
        "Kahramanmaraş",
        "Kocaeli",
        "Konya",
        "Kütahya",
        "Malatya",
        "Manisa",
        "Mardin",
        "Muğla",
        "Muş",
        "Nevşehir",
        "Niğde",
        "Ordu",
        "Osmaniye",
        "Rize",
        "Sakarya",
        "Samsun",
        "Siirt",
        "Sinop",
        "Sivas",
        "Şanlıurfa",
        "Şırnak",
        "Tekirdağ",
        "Tokat",
        "Trabzon",
        "Tunceli",
        "Uşak",
        "Van",
        "Yalova",
        "Yozgat",
        "Zonguldak"
    )
    var map: MutableMap<String, String> = HashMap()
    fun selectedCity(selectedCity: String): String? {
        var sendCityInformation: String? = ""
        for (ourCity in map.keys) {
            if (selectedCity == ourCity) {
                sendCityInformation = map[ourCity]
            }
        }
        return sendCityInformation
    }

    init {
        map["Şehir Seç"] = "Şehir Seçin!"
        map["Adana"] = "01"
        map["Adıyaman"] = "02"
        map["Afyon"] = "03"
        map["Ağrı"] = "04"
        map["Aksaray"] = "68"
        map["Amasya"] = "05"
        map["Ankara"] = "06"
        map["Antalya"] = "07"
        map["Ardahan"] = "75"
        map["Artvin"] = "08"
        map["Aydın"] = "09"
        map["Balıkesir"] = "10"
        map["Bartın"] = "74"
        map["Batman"] = "72"
        map["Bayburt"] = "69"
        map["Bilecik"] = "11"
        map["Bingöl"] = "12"
        map["Bitlis"] = "13"
        map["Bolu"] = "14"
        map["Burdur"] = "15"
        map["Bursa"] = "16"
        map["Çanakkale"] = "17"
        map["Çankırı"] = "18"
        map["Çorum"] = "19"
        map["Denizli"] = "20"
        map["Diyarbakır"] = "21"
        map["Edirne"] = "22"
        map["Elazığ"] = "23"
        map["Erzincan"] = "24"
        map["Erzurum"] = "25"
        map["Eskişehir"] = "26"
        map["Gaziantep"] = "27"
        map["Giresun"] = "28"
        map["Gümüşhane"] = "29"
        map["Hakkari"] = "30"
        map["Hatay"] = "31"
        map["Iğdır"] = "76"
        map["Isparta"] = "32"
        map["İçel (Mersin)"] = "33"
        map["İstanbul"] = "34"
        map["İzmir"] = "35"
        map["Karabük"] = "78"
        map["Karaman"] = "70"
        map["Kars"] = "36"
        map["Kastamonu"] = "37"
        map["Kayseri"] = "38"
        map["Kırıkkale"] = "71"
        map["Kırklareli"] = "39"
        map["Kırşehir"] = "40"
        map["Kilis"] = "79"
        map["Kahramanmaraş"] = "46"
        map["Kocaeli"] = "41"
        map["Konya"] = "42"
        map["Kütahya"] = "43"
        map["Malatya"] = "44"
        map["Manisa"] = "45"
        map["Mardin"] = "47"
        map["Muğla"] = "48"
        map["Muş"] = "49"
        map["Nevşehir"] = "50"
        map["Niğde"] = "51"
        map["Ordu"] = "52"
        map["Osmaniye"] = "80"
        map["Rize"] = "53"
        map["Sakarya"] = "54"
        map["Samsun"] = "55"
        map["Siirt"] = "56"
        map["Sinop"] = "57"
        map["Sivas"] = "58"
        map["Şanlıurfa"] = "63"
        map["Şırnak"] = "73"
        map["Tekirdağ"] = "59"
        map["Tokat"] = "60"
        map["Trabzon"] = "61"
        map["Tunceli"] = "62"
        map["Uşak"] = "64"
        map["Van"] = "65"
        map["Yalova"] = "77"
        map["Yozgat"] = "66"
        map["Zonguldak"] = "67"
    }
}