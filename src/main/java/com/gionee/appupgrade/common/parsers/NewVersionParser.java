package com.gionee.appupgrade.common.parsers;

import android.content.Context;
import com.gionee.appupgrade.common.GnAppUpgradeImple;
import com.gionee.appupgrade.common.NewVersion;
import com.gionee.appupgrade.common.NewVersion.VersionType;
import com.gionee.appupgrade.common.utils.LogUtils;
import com.gionee.appupgrade.common.utils.Utils;
import com.gionee.note.app.NewNoteActivity;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewVersionParser {
    private static final String TAG = "NewVersionParser";

    public static void parse(String currentVersionInfo, Context context, String clientName) throws JSONException {
        LogUtils.log(TAG, LogUtils.getThreadName());
        NewVersion app = new NewVersion(context, clientName);
        if (currentVersionInfo != null) {
            String releaseNote = "";
            String displayVersion = "";
            String strUrl = "";
            String fileSize = "";
            String upgradeMode = VersionType.NORMAL_VERSION;
            String totalFileSize = VersionType.NORMAL_VERSION;
            boolean isPatchFile = false;
            String md5 = "";
            String fullMd5 = "";
            String patchId = "";
            JSONObject jsonObj = new JSONObject(currentVersionInfo);
            releaseNote = jsonObj.getString("releaseNote");
            displayVersion = jsonObj.getString("displayVersion");
            JSONArray jsonObjs = jsonObj.getJSONArray("models");
            upgradeMode = jsonObj.getString("upgrademode");
            for (int i = 0; i < jsonObjs.length(); i++) {
                JSONObject jsonObj1 = (JSONObject) jsonObjs.get(i);
                strUrl = jsonObj1.getString(NewNoteActivity.NOTE_ITEM_PATH);
                fileSize = jsonObj1.getString("size");
                isPatchFile = jsonObj1.getBoolean("patch");
                if (isPatchFile) {
                    totalFileSize = jsonObj1.getString("fullSize");
                    md5 = jsonObj1.getString("md5");
                    fullMd5 = jsonObj1.getString("fullMd5");
                    patchId = jsonObj1.getString("patchId");
                }
            }
            app.setOldApkMd5(Utils.getFileMd5(new File(Utils.getClientApkPath(GnAppUpgradeImple.getClientContext(context, clientName)))));
            app.setReleaseNote(releaseNote);
            app.setDisplayVersion(displayVersion);
            app.setStrUrl(strUrl);
            app.setFileSize(fileSize);
            app.setUpgradeMode(upgradeMode);
            app.setIsPatchFile(isPatchFile);
            app.setTotalFileSize(totalFileSize);
            app.setMd5(md5);
            app.setFullPackageMd5(fullMd5);
            app.setPatchId(patchId);
        }
    }
}
