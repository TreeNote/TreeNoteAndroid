package de.treenote.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import de.treenote.R;
import de.treenote.pojo.TreeNodeRoot;
import de.treenote.receiver.TreeNoteResultReceiver;
import de.treenote.services.SyncService;
import de.treenote.services.TreeNoteAlarmService;
import de.treenote.util.TreeNoteDataHolder;

/**
 * Die SplashActivity wird angezeigt, wenn die App gestartet wird.
 * Sobald das Laden der TreeNodes erfolgreich war, wird die TreeNoteMainActivity gestartet.
 * So wird sichergestellt, dass alle benötigten Daten vorhanden sind.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        loadAllTreeNodes();
        startAlarmService();
    }

    private void loadAllTreeNodes() {
        TreeNoteResultReceiver treeNoteResultReceiver = new TreeNoteResultReceiver(new Handler());
        treeNoteResultReceiver.setReceiver(new TreeNoteResultReceiver.Receiver() {

            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == SyncService.RESULT_CODE_SUCCESSFUL) {
                    TreeNodeRoot treeNodeRoot = (TreeNodeRoot) resultData.getSerializable(SyncService.TREE_NODE_KEY);
                    TreeNoteDataHolder.setData(treeNodeRoot);
                    startActivity(new Intent(SplashActivity.this, TreeNoteMainActivity.class));
                } else {
                    throw new IllegalStateException("Error while reading TreeNodeRoot");
                }
            }
        });

        Intent loadTreeNodeRootIntent = new Intent(this, SyncService.class);
        loadTreeNodeRootIntent.setAction(SyncService.LOAD_ACTION);
        loadTreeNodeRootIntent.putExtra(SyncService.TREE_NODE_RESULT_RECEIVER_KEY, treeNoteResultReceiver);
        this.startService(loadTreeNodeRootIntent);
    }

    private void startAlarmService() {
        Intent setupAlarmServiceIntent = new Intent(this, TreeNoteAlarmService.class);
        setupAlarmServiceIntent.setAction(TreeNoteAlarmService.SETUP_ALARM_ACTION);
        this.startService(setupAlarmServiceIntent);
    }
}
