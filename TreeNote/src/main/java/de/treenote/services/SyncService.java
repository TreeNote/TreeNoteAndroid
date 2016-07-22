package de.treenote.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.widget.Toast;

import com.google.gson.Gson;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.ReadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import de.treenote.R;
import de.treenote.fragments.TreeNotePreferenceFragment;
import de.treenote.pojo.TreeNodeRoot;
import de.treenote.util.Constants;
import de.treenote.util.TreeNoteDataHolder;

public class SyncService extends IntentService implements OnRemoteOperationListener {

    public static final String SYNC_SERVICE_NAME = "SyncService";

    public static final String TREE_NODE_RESULT_RECEIVER_KEY = "TREE_NODE_RESULT_RECEIVER_KEY";
    public static final String TREE_NODE_KEY = "TREE_NODE_KEY";
    public static final String SAVE_ACTION = "SAVE_ACTION";
    public static final String LOAD_ACTION = "LOAD_ACTION";
    public static final String SYNC_ACTION = "SYNC_ACTION";
    public static final int RESULT_CODE_SUCCESSFUL = 0;

    private OwnCloudClient client;
    private Handler handler = new Handler();

    private static final Gson gson = new Gson();

    ResultReceiver resultReceiver;

    public SyncService() {
        super(SYNC_SERVICE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case LOAD_ACTION:
                resultReceiver = intent.getParcelableExtra(TREE_NODE_RESULT_RECEIVER_KEY);
                returnTree();
                break;
            case SAVE_ACTION:
                saveTreeNodeRoot();
                break;
            case SYNC_ACTION:
                toast(getString(R.string.start_sync), Toast.LENGTH_SHORT);
                resultReceiver = intent.getParcelableExtra(TREE_NODE_RESULT_RECEIVER_KEY);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String url = sharedPref.getString(TreeNotePreferenceFragment.OWN_CLOUD_URL, "");
                Uri serverUri = Uri.parse(url);
                String username = sharedPref.getString(TreeNotePreferenceFragment.OWN_CLOUD_USERNAME, "");
                String password = sharedPref.getString(TreeNotePreferenceFragment.OWN_CLOUD_PASSWORD, "");
                client = OwnCloudClientFactory.createOwnCloudClient(serverUri, this, true);
                client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, password));
                if (Patterns.WEB_URL.matcher(url).matches() && !("".equals(password))) {
                    startFolderCreation(Constants.OWNCLOUD_FOLDER);
                } else {
                    toast(getString(R.string.check_owncloud_url), Toast.LENGTH_LONG);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + intent.getAction());
        }
    }

    private void returnTree() {
        Bundle resultData = new Bundle(1);
        TreeNodeRoot reedTreeNodeRoot = readTreeNodeRoot();
        resultData.putSerializable(TREE_NODE_KEY, reedTreeNodeRoot);
        resultReceiver.send(RESULT_CODE_SUCCESSFUL, resultData);
    }

    /**
     * @return den zuletzt abgespeicherten Baum oder einen neuen Baum, falls es noch keinen Baum gibt
     */
    private TreeNodeRoot readTreeNodeRoot() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(getFilesDir() + File.separator + Constants.OWNCLOUD_FILE_NAME));
        } catch (IOException e) {
            System.out.println("No JSON file found. Loading example tree...");
            int example_tree;
            if (Locale.getDefault().getLanguage().equals("de")) {
                example_tree = R.raw.guide_tree_de;
            } else {
                example_tree = R.raw.guide_tree_en;
            }
            InputStream stream = getApplicationContext().getResources().openRawResource(example_tree);
            br = new BufferedReader(new InputStreamReader(stream), 8092);
        }
        TreeNodeRoot jsonNode = gson.fromJson(br, TreeNodeRoot.class);
        return jsonNode;
    }

    private void saveTreeNodeRoot() {
        File internalFile = new File(getFilesDir(), Constants.OWNCLOUD_FILE_NAME);
        try (FileWriter fileWriter = new FileWriter(internalFile.getAbsolutePath())) {
            String treeNodeRootAsJsonString = gson.toJson(TreeNoteDataHolder.getTreeNodeRoot());
//            System.out.println(treeNodeRootAsJsonString);
            fileWriter.write(treeNodeRootAsJsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void toast(final String text, final int length) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, length).show();
            }
        });
    }

    // ------------------------------------- Owncloud Kram -------------------------------------

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (operation instanceof CreateRemoteFolderOperation) {
            startReadFileProperties(Constants.OWNCLOUD_PATH);
        } else if (operation instanceof ReadRemoteFileOperation) {
            if (result.isSuccess()) {
                ArrayList<Object> files = result.getData();
                RemoteFile remoteFile = (RemoteFile) files.get(0);
                File localFile = new File(getFilesDir() + File.separator + Constants.OWNCLOUD_FILE_NAME);
                if (remoteFile.getModifiedTimestamp() > localFile.lastModified()) {
                    startDownload(Constants.OWNCLOUD_PATH, getCacheDir());
                    toast(getString(R.string.cloud_tree_is_newer), Toast.LENGTH_LONG);
                    return;
                } else {
                    saveTreeNodeRoot();
                    File internalFile = new File(getFilesDir(), Constants.OWNCLOUD_FILE_NAME);
                    String remotePath = Constants.OWNCLOUD_PATH;
                    String mimeType = "application/json";
                    startUpload(internalFile, remotePath, mimeType);
                    toast(getString(R.string.local_tree_is_newer), Toast.LENGTH_LONG);
                }
            } else if (result.getLogMessage().equals("Unknown host exception")) {
                toast(getString(R.string.check_internet_connection),
                        Toast.LENGTH_LONG);
            } else if (result.getLogMessage().equals("Operation finished with HTTP status code 401 (fail)")) {
                toast(getString(R.string.check_username_and_password), Toast.LENGTH_LONG);
            } else {
                toast(getString(R.string.check_owncloud_url), Toast.LENGTH_LONG);
            }
            returnTree();
        } else if (operation instanceof DownloadRemoteFileOperation) {
            if (result.isSuccess()) {
                File localFile = new File(getFilesDir() + File.separator + Constants.OWNCLOUD_FILE_NAME);
                File remoteFile = new File(getCacheDir() + File.separator + Constants.OWNCLOUD_PATH);
                //noinspection ResultOfMethodCallIgnored
                remoteFile.renameTo(localFile);
            }
            returnTree();
        }
    }

    private void startDownload(String filePath, File targetDirectory) {
        DownloadRemoteFileOperation downloadOperation = new DownloadRemoteFileOperation(filePath, targetDirectory.getAbsolutePath() + File.separator);
        downloadOperation.execute(client, this, handler);
    }

    private void startFolderCreation(String newFolderPath) {
        CreateRemoteFolderOperation createOperation = new CreateRemoteFolderOperation(newFolderPath, false);
        createOperation.execute(client, this, handler);
    }

    private void startReadFileProperties(String filePath) {
        ReadRemoteFileOperation readOperation = new ReadRemoteFileOperation(filePath);
        readOperation.execute(client, this, handler);
    }

    private void startUpload(File fileToUpload, String remotePath, String mimeType) {
        UploadRemoteFileOperation uploadOperation = new UploadRemoteFileOperation(fileToUpload.getAbsolutePath(), remotePath, mimeType);
        uploadOperation.execute(client, this, handler);
    }
}
