package com.alphawallet.app.util;

import android.app.Activity;

import com.alphawallet.app.entity.FragmentMessenger;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

public class UpdateUtils {
    public static void checkForUpdates(Activity context, FragmentMessenger messenger) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)
            {
                messenger.updateReady(appUpdateInfo);
            }
        });
    }

    public static void pushUpdateDialog(Activity context, AppUpdateInfo updateInfo)
    {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);
        appUpdateManager.startUpdateFlow(updateInfo, context, AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build());
    }
}