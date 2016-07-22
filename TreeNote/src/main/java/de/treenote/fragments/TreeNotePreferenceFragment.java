package de.treenote.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import de.treenote.R;

public class TreeNotePreferenceFragment extends PreferenceFragment {
    public static final String ENABLE_NOTIFICATIONS = "enable_notifications";
    private SharedPreferences.OnSharedPreferenceChangeListener urlListener;
    private Preference urlPreference;
    private SharedPreferences.OnSharedPreferenceChangeListener userNameListener;
    private Preference userNamePreference;

    public static final String OWN_CLOUD_URL = "own_cloud_url";
    public static final String OWN_CLOUD_USERNAME = "own_cloud_username";
    public static final String OWN_CLOUD_PASSWORD = "own_cloud_password";

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.action_preferences);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

        urlPreference = getPreferenceManager().findPreference(OWN_CLOUD_URL);
        urlListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key) {
                urlPreference.setSummary(sharedPreferences.getString(OWN_CLOUD_URL, "https://example.com"));
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(urlListener);
        urlListener.onSharedPreferenceChanged(prefs, OWN_CLOUD_URL);

        userNamePreference = getPreferenceManager().findPreference(OWN_CLOUD_USERNAME);
        userNameListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key) {
                userNamePreference.setSummary(sharedPreferences.getString(
                        OWN_CLOUD_USERNAME, getString(R.string.none_set)));
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(userNameListener);
        userNameListener.onSharedPreferenceChanged(prefs, OWN_CLOUD_USERNAME);
    }
}