package de.treenote.util;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import de.treenote.R;

public class TreeNoteActionBarDrawerToggle extends ActionBarDrawerToggle {

    public TreeNoteActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar) {
        super(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    // ----------------- Animation des überdeckten toggle Button soll nicht ausgeführt werden -----------

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        // nothing
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        // nothing
    }
}
