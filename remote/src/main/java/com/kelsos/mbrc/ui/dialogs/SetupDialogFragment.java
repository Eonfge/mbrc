package com.kelsos.mbrc.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.ui.activities.SettingsActivity;

public class SetupDialogFragment extends DialogFragment {
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_application_setup)
                .setPositiveButton(R.string.dialog_application_setup_positive,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(getActivity(), SettingsActivity.class));
                            }
                        }
                )
                .setNegativeButton(R.string.dialog_application_setup_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SetupDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
