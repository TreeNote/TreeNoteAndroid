package de.treenote.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import de.treenote.R;
import lombok.Setter;

/**
 * Dieser Dialog wird angezeigt, wenn man auf einer TreeNodeView einen Longpress durchführt
 * <p>
 * Nach https://developer.android.com/guide/topics/ui/dialogs.html umgesetzt
 */
public class TreeNodeViewOptionsDialog extends DialogFragment {

    public interface OnOptionClickedListener {

        void onZoomInOptionClicked();

        void onDeleteOptionClicked();
    }

    private static final String LOG_TAG = TreeNodeViewOptionsDialog.class.getName();

    private static final int ZOOM_IN_INDEX = 0;
    private static final int DELETE_INDEX = 1;

    @Setter
    private OnOptionClickedListener onOptionClickedListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_option)
                .setItems(R.array.tree_node_view_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (onOptionClickedListener != null) {
                            switch (which) {
                                case ZOOM_IN_INDEX:
                                    onOptionClickedListener.onZoomInOptionClicked();
                                    break;
                                case DELETE_INDEX:
                                    onOptionClickedListener.onDeleteOptionClicked();
                                    break;
                                default:
                                    throw new IllegalStateException("Unknown option with index " + which);
                            }
                        } else {
                            Log.w(LOG_TAG, "No OnOptionClickedListener was set");
                        }
                        dismiss();
                    }
                });
        return builder.create();
    }
}
