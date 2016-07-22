package de.treenote.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import de.treenote.R;

import static com.google.common.base.Preconditions.checkNotNull;


public class SingleFragmentActivity extends AppCompatActivity {

    public static final String FRAGMENT_CLASS = "FRAGMENT_CLASS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Bundle intentExtraBundle = getIntent().getExtras();
        Class fragmentClass = (Class) intentExtraBundle.getSerializable(FRAGMENT_CLASS);
        checkNotNull(fragmentClass);
        if (savedInstanceState == null) {
            Fragment fragment = Fragment.instantiate(this, fragmentClass.getName());
            fragment.setArguments(intentExtraBundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.singleFragmentContainer, fragment, fragmentClass.getName()).commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        checkNotNull(supportActionBar);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // same animation as back button and there better than "NavUtils.navigateUpFromSameTask(this);"
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}