package group13.ntphat.evernote.ui.notebook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.json.JSONException;

import group13.ntphat.evernote.R;

public class NewNotebookDialog extends AppCompatDialogFragment {

    private EditText editText;
    private NewNotebookDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_notebook, null);

        editText = view.findViewById(R.id.new_nb_name);

        builder.setView(view)
                .setTitle("Tạo notebook ")
                .setNegativeButton("đóng", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("xác nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            listener.applyTexts(editText.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (NewNotebookDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement NewNotebookDialogListener");
        }
    }

    public interface NewNotebookDialogListener {
        void applyTexts(String notebookName) throws JSONException;
    }

}
