package com.nemator.needle.fragments.haystack;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;

import com.nemator.needle.R;

/**
 * Created by Alex on 25/04/2016.
 */
public class AddPinDialogFragment extends DialogFragment {

    private Listener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_new_pin, null))
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AppCompatEditText titleText = (AppCompatEditText) getDialog().findViewById(R.id.input_text);
                        listener.onConfirm(titleText.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onCancel();
                        dialog.cancel();
                    }
                })
                .setTitle(R.string.new_pin);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddPinDialogFragment.Listener");
        }
    }

    public interface Listener{
        void onConfirm(String title);
        void onCancel();
    }
}
