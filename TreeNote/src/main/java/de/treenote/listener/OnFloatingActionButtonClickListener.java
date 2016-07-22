package de.treenote.listener;

import android.view.View;

/**
 * Dieses Interface wird für die drei floating action Buttons Add, Check und Clear verwendet
 */
public interface OnFloatingActionButtonClickListener {

    void onFloatingActionButtonAddClicked(View floatingActionButton);

    void onFloatingActionButtonCheckClicked(View floatingActionButton);

    void onFloatingActionButtonClearClicked(View floatingActionButton);
}
