package com.mrcaracal.Modul;

import java.util.HashMap;
import java.util.Map;

public class Sehirler {

    public String[] sehirler = {"Şehir Seç", "Adana", "Adıyaman", "Afyon", "Ağrı", "Aksaray", "Amasya", "Ankara", "Antalya", "Ardahan", "Artvin", "Aydın", "Balıkesir", "Bartın", "Batman", "Bayburt", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Iğdır", "Isparta", "İçel (Mersin)", "İstanbul", "İzmir", "Karabük", "Karaman", "Kars", "Kastamonu", "Kayseri", "Kırıkkale", "Kırklareli", "Kırşehir", "Kilis", "Kahramanmaraş", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Mardin", "Muğla", "Muş", "Nevşehir", "Niğde", "Ordu", "Osmaniye", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas", "Şanlıurfa", "Şırnak", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Uşak", "Van", "Yalova", "Yozgat", "Zonguldak"};
    Map<String, String> map = new HashMap<>();

    public Sehirler() {

        map.put("Şehir Seç", "Şehir Seçin!");
        map.put("Adana", "01");
        map.put("Adıyaman", "02");
        map.put("Afyon", "03");
        map.put("Ağrı", "04");
        map.put("Aksaray", "68");
        map.put("Amasya", "05");
        map.put("Ankara", "06");
        map.put("Antalya", "07");
        map.put("Ardahan", "75");
        map.put("Artvin", "08");
        map.put("Aydın", "09");
        map.put("Balıkesir", "10");
        map.put("Bartın", "74");
        map.put("Batman", "72");
        map.put("Bayburt", "69");
        map.put("Bilecik", "11");
        map.put("Bingöl", "12");
        map.put("Bitlis", "13");
        map.put("Bolu", "14");
        map.put("Burdur", "15");
        map.put("Bursa","16");
        map.put("Çanakkale","17");
        map.put("Çankırı","18");
        map.put("Çorum","19");
        map.put("Denizli","20");
        map.put("Diyarbakır","21");
        map.put("Edirne","22");
        map.put("Elazığ","23");
        map.put("Erzincan","24");
        map.put("Erzurum","25");
        map.put("Eskişehir","26");
        map.put("Gaziantep","27");
        map.put("Giresun","28");
        map.put("Gümüşhane","29");
        map.put("Hakkari","30");
        map.put("Hatay","31");
        map.put("Iğdır","76");
        map.put("Isparta","32");
        map.put("İçel (Mersin)","33");
        map.put("İstanbul","34");
        map.put("İzmir","35");
        map.put("Karabük","78");
        map.put("Karaman","70");
        map.put("Kars","36");
        map.put("Kastamonu","37");
        map.put("Kayseri","38");
        map.put("Kırıkkale","71");
        map.put("Kırklareli","39");
        map.put("Kırşehir","40");
        map.put("Kilis","79");
        map.put("Kahramanmaraş","46");
        map.put("Kocaeli","41");
        map.put("Konya","42");
        map.put("Kütahya","43");
        map.put("Malatya","44");
        map.put("Manisa","45");
        map.put("Mardin","47");
        map.put("Muğla","48");
        map.put("Muş","49");
        map.put("Nevşehir","50");
        map.put("Niğde","51");
        map.put("Ordu","52");
        map.put("Osmaniye","80");
        map.put("Rize","53");
        map.put("Sakarya","54");
        map.put("Samsun","55");
        map.put("Siirt","56");
        map.put("Sinop","57");
        map.put("Sivas","58");
        map.put("Şanlıurfa","63");
        map.put("Şırnak","73");
        map.put("Tekirdağ","59");
        map.put("Tokat","60");
        map.put("Trabzon","61");
        map.put("Tunceli","62");
        map.put("Uşak","64");
        map.put("Van","65");
        map.put("Yalova","77");
        map.put("Yozgat","66");
        map.put("Zonguldak","67");

    }

    public String sehirler(String secilenSehir){

        String gonderilecek_sehir_bilgisi = "";

        for (String sehrimiz : map.keySet()){
            if (secilenSehir.equals(sehrimiz)){
                gonderilecek_sehir_bilgisi = map.get(sehrimiz);
            }
        }

        return gonderilecek_sehir_bilgisi;

    }



}
