/*
 * Copyright 2015 Steven Mulder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.treenote.activities;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MenuItem;

import de.treenote.R;
import de.treenote.fragments.AboutFragment;
import de.treenote.fragments.TreeFragment;
import de.treenote.fragments.TreeNotePreferenceFragment;
import de.treenote.services.SyncService;
import de.treenote.util.CurrentFilterHolder;

import static de.treenote.services.SyncService.SYNC_ACTION;

public class TreeNoteMainActivity extends NavigationDrawerActivity {

    private static final TreeNotePreferenceFragment preferenceFragment = new TreeNotePreferenceFragment();
    private static final AboutFragment aboutFragment = new AboutFragment();
    public static final String SHOW_TASKS_FOR_TODAY_ACTION = "de.treenote.SHOW_TASKS_FOR_TODAY";

    private BroadcastReceiver showTasksForTodayIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            navigate(intent.getIntExtra(NAVIGATION_ITEM_ID, R.id.action_all_items));
        }
    };

    private IntentFilter showTasksForTodayIntentFilter = new IntentFilter(SHOW_TASKS_FOR_TODAY_ACTION);

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(showTasksForTodayIntentReceiver, showTasksForTodayIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(showTasksForTodayIntentReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.sync_action) {
            Intent syncIntent = new Intent(this, SyncService.class);
            syncIntent.setAction(SYNC_ACTION);
            startService(syncIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Performs the actual navigation logic, updating the main content fragment.
     */
    protected void navigate(final int itemId) {

        Fragment fragmentInMainContent;

        switch (itemId) {
            case R.id.action_all_items:
                CurrentFilterHolder.currentFilter = CurrentFilterHolder.Filter.None;
                fragmentInMainContent = TreeFragment.newInstance(getString(R.string.action_all_items));
                break;
            case R.id.action_today:
                CurrentFilterHolder.currentFilter = CurrentFilterHolder.Filter.Today;
                fragmentInMainContent = TreeFragment.newInstance(getString(R.string.action_today));
                break;
            case R.id.action_upcoming:
                CurrentFilterHolder.currentFilter = CurrentFilterHolder.Filter.Upcoming;
                fragmentInMainContent = TreeFragment.newInstance(getString(R.string.action_upcoming));
                break;
            case R.id.action_todos:
                CurrentFilterHolder.currentFilter = CurrentFilterHolder.Filter.Todos;
                fragmentInMainContent = TreeFragment.newInstance(getString(R.string.action_todos));
                break;
            case R.id.action_preferences:
                fragmentInMainContent = preferenceFragment;
                break;
            case R.id.action_about:
                fragmentInMainContent = aboutFragment;
                break;
            default:
                return;
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.navigationDrawerMainContent, fragmentInMainContent)
                .commit();
    }
}
