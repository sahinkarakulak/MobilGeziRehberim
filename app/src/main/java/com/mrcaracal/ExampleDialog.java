package com.mrcaracal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.mrcaracal.mobilgezirehberim.R;

public class ExampleDialog extends AppCompatDialogFragment {

    private TextView ad1_baslik, ad1_sikayet, ad1_kaydet;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_1, null);

        builder.setView(view);

        ad1_baslik = view.findViewById(R.id.ad1_baslik);
        ad1_sikayet = view.findViewById(R.id.ad1_sikayet);
        ad1_kaydet = view.findViewById(R.id.ad1_kaydet);

        return builder.create();
    }
}
