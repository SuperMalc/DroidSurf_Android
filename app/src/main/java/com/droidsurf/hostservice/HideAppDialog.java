package com.droidsurf.hostservice;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class HideAppDialog extends AppCompatDialogFragment {
    private DialogListener listener2;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout2_dialog,null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view)
                .setIcon(R.drawable.ic_warning_black)
                .setTitle(R.string.warning)
                .setMessage(R.string.warning2)
                .setNegativeButton(R.string.button_del, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String result = "0";
                        listener2.applyTexts2(result);
                    }
                })
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String result = "1";
                        listener2.applyTexts2(result);
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
            listener2 = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ExampleDialogListener");
        }
    }

    public interface DialogListener {
        void applyTexts2(String result);
    }
}
