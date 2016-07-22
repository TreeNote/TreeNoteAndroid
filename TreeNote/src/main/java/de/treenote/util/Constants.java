package de.treenote.util;

import com.owncloud.android.lib.resources.files.FileUtils;

public class Constants {
    public static final String OWNCLOUD_FOLDER = "TreeNote";
    public static final String OWNCLOUD_FILE_NAME = "tree.json";
    public static final String OWNCLOUD_PATH = OWNCLOUD_FOLDER + FileUtils.PATH_SEPARATOR + OWNCLOUD_FILE_NAME;

    public static final int MIN_VELOCITY_X = 1000;
    public static final int MAX_VELOCITY_Y = 2000;
}
