package com.mrcaracal.mobilgezirehberim;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class F_Ara extends Fragment {

    ImageView img_konuma_gore_bul;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.frag_ara, container, false);

        img_konuma_gore_bul = viewGroup.findViewById(R.id.img_konuma_gore_bul);

        img_konuma_gore_bul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), A_Harita.class));
            }
        });

        return viewGroup;
    }
}
