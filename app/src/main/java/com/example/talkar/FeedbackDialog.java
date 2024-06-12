package com.example.talkar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.annotations.Nullable;

import javax.annotation.Nonnull;

public class FeedbackDialog extends AppCompatDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nonnull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Course Completed!!")
                .setMessage("You have successfully completed the entire course, Congratulations!")
                .setPositiveButton("Give Feedback", (dialog, which) -> {
                    Intent intent = new Intent(this.getContext(), Feedback.class);
                    startActivity(intent);
                    getActivity().finish();
                });
        return  builder.create();
    }
}
