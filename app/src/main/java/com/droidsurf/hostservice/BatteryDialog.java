package com.droidsurf.hostservice;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class BatteryDialog extends AppCompatDialogFragment {

    private DialogListener listener3;
    public static final String TAG = "BatteryDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout3_dialog,null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        /**
        bt1 = view.findViewById(R.id.devlist);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this will open auto start screen where user can enable permission for your app
                Intent intent1 = new Intent();
                intent1.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                startActivity(intent1);
            }
        });

        */

        builder.setView(view)
                .setIcon(R.drawable.ic_perm)
                .setTitle(R.string.run_onboot)
                .setMessage(R.string.warn_battery2)
                .setNegativeButton(R.string.button_del, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String result = "0";
                        listener3.applyTexts3(result);
                    }
                })
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String result = "1";
                        listener3.applyTexts3(result);
                    }
                });

        AlertDialog alert = builder.create();
        setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        return alert;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener3 = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ExampleDialogListener");
        }
    }

    public interface DialogListener {
        void applyTexts3(String selection);
    }
}