package com.topjohnwu.magisk.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.topjohnwu.magisk.utils.Utils;

public class Module {

    private String mRemoveFile;
    private String mDisableFile;

    private String mName = null;
    private String mVersion = "(No version provided)";
    private String mDescription = "(No description provided)";
    private String mSupportUrl, mDonateUrl, mZipUrl, mAuthor, mLogUrl;
    private boolean mEnable = false, mRemove = false, mUpdateAvailable = false, mIsInstalled,
            mIsCacheModule = false;


    private String mId;
    private int mVersionCode;

    public Module(String path, Context context) {

        mRemoveFile = path + "/remove";
        mDisableFile = path + "/disable";
        for (String line : Utils.readFile(path + "/module.prop")) {
            String[] props = line.split("=", 2);
            if (props.length != 2) {
                continue;
            }

            String key = props[0].trim();
            if (key.charAt(0) == '#') {
                continue;
            }

            switch (props[0]) {
                case "id":
                    this.mId = props[1];
                    break;
                case "name":
                    this.mName = props[1];
                    break;
                case "version":
                    this.mVersion = props[1];
                    break;
                case "versionCode":
                    this.mVersionCode = Integer.parseInt(props[1]);
                    break;
                case "author":
                    this.mAuthor = props[1];
                    break;
                case "description":
                    this.mDescription = props[1];
                    break;
                case "support":
                    this.mSupportUrl = props[1];
                    break;
                case "donate":
                    this.mDonateUrl = props[1];
                    break;
                case "cacheModule":
                    this.mIsCacheModule = Boolean.parseBoolean(props[1]);
                    break;
                default:
                    Log.d("Magisk", "Module: Manifest string not recognized: " + props[0]);
                    break;
            }


        }
        Log.d("Magisk","Module: Loaded module with ID of " + this.mId + " or " + mId);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (this.mId != null && !this.mId.isEmpty()) {
            String preferenceString = "repo_" + this.mId;
            String preferenceKey = prefs.getString(preferenceString,"nope");
            Log.d("Magisk", "Module: Checking for preference named " + preferenceString);
            if (!preferenceKey.equals("nope")) {
                Log.d("Magisk", "Module: repo_" + mId + " found.");
                String entryString = prefs.getString("repo_" + mId, "");
                String[] subStrings = entryString.split("\n");
                for (String subKeys : subStrings) {
                    String[] idEntry = subKeys.split("=", 2);
                    if (idEntry[0].equals("id")) {
                        if (idEntry.length != 2) {
                            continue;
                        }

                        if (idEntry[1].equals(mId)) {
                            Log.d("Magisk", "Module: Hey, I know " + mId + " is online...");
                            mIsInstalled = true;
                        } else mIsInstalled = false;
                    }
                    if (idEntry[0].equals("logUrl")) {
                        mLogUrl = idEntry[1];
                    }
                    if (idEntry[0].equals("support")) {
                        mSupportUrl = idEntry[1];
                    }
                    if (idEntry[0].equals("zipUrl")) {
                        mZipUrl = idEntry[1];
                    }
                    if (idEntry[0].equals("donate")) {
                        mDonateUrl = idEntry[1];
                    }

                    if (idEntry[0].equals("versionCode")) {
                        if (idEntry.length != 2) {
                            continue;
                        }

                        if (Integer.valueOf(idEntry[1]) > mVersionCode) {
                            mUpdateAvailable = true;
                            Log.d("Magisk", "Module: Hey, I have an update...");
                        } else mUpdateAvailable = false;
                    }
                }


            }

            SharedPreferences.Editor editor = prefs.edit();
            if (mIsInstalled) {
                editor.putBoolean("repo-isInstalled_" + mId, true);
            } else {
                editor.putBoolean("repo-isInstalled_" + mId, false);
            }

            if (mUpdateAvailable) {
                editor.putBoolean("repo-canUpdate_" + mId, true);
            } else {
                editor.putBoolean("repo-canUpdate_" + mId, false);
            }
            editor.apply();
        }

        if (mName == null) {
            int sep = path.lastIndexOf('/');
            mName = path.substring(sep + 1);
            mId = mName;
        }

        mEnable = !Utils.fileExist(mDisableFile);
        mRemove = Utils.fileExist(mRemoveFile);

    }

//    public Module(Repo repo) {
//
//        mName = repo.getName();
//        mVersion = repo.getmVersion();
//        mDescription = repo.getDescription();
//        mId = repo.getId();
//        mVersionCode = repo.getmVersionCode();
//        mUrl = repo.getmZipUrl();
//        mEnable = true;
//        mRemove = false;
//
//    }



    public String getName() {
        return mName;
    }

    public String getVersion() {
        return mVersion;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getId() {return mId; }

    public String getmLogUrl() {return mLogUrl; }

    public String getDescription() {
        return mDescription;
    }

    public void createDisableFile() {
        mEnable = !Utils.createFile(mDisableFile);
    }

    public void removeDisableFile() {
        mEnable = Utils.removeFile(mDisableFile);
    }

    public boolean isEnabled() {
        return mEnable;
    }

    public void createRemoveFile() {
        mRemove = Utils.createFile(mRemoveFile);
    }

    public void deleteRemoveFile() {
        mRemove = !Utils.removeFile(mRemoveFile);
    }

    public boolean willBeRemoved() {
        return mRemove;
    }

    public boolean isCache() {
        return mIsCacheModule;
    }

    public void setCache() {
        mIsCacheModule = true;
    }

    public String getmDonateUrl() {
        return mDonateUrl;
    }

    public String getmZipUrl() { return mZipUrl; }

    public String getmSupportUrl() {
        return mSupportUrl;
    }

    public boolean isUpdateAvailable() { return mUpdateAvailable; }

}