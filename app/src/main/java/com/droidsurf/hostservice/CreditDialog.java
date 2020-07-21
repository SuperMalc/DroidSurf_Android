package com.droidsurf.hostservice;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class CreditDialog extends AppCompatDialogFragment {

    public static final String URL2 = "https://github.com/SuperMalc/DroidSurf";
    public static final String URL1 = "http://androidsurfer.altervista.org/";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_credits,null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view);

        AlertDialog alert = builder.create();
        setCancelable(true);
        alert.setCanceledOnTouchOutside(true);

        Typeface tf = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/East_Lift.ttf");
        TextView tvTitle = view.findViewById(R.id.textViewTitle);
        tvTitle.setTypeface(tf);

        Typeface tf2 = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/Zekton_rg.ttf");
        TextView tvTitle2 = view.findViewById(R.id.textView3);
        tvTitle2.setTypeface(tf2);

        TextView gitHubBt = view.findViewById(R.id.textView5);
        ImageButton settingsButton = view.findViewById(R.id.buttonPerms);

        gitHubBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL2));
                startActivity(browserIntent);
            }
        });

        TextView email = view.findViewById(R.id.textViewEmail);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "surferdevelopers@gmail.com" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bug report");
                startActivity(Intent.createChooser(intent, "Type of bug"));
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", Objects.requireNonNull(getContext()).getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        return alert;
    }
}
