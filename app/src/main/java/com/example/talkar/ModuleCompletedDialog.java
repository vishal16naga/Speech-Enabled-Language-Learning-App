package com.example.talkar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.annotations.Nullable;

import javax.annotation.Nonnull;

public class ModuleCompletedDialog extends AppCompatDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nonnull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Module Completed")
                .setMessage("You have successfully completed the module")
                .setPositiveButton("Okay", (dialog, which) -> {
                    getActivity().finish();
                });
        return  builder.create();
    }
}
