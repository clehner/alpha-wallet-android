package com.alphawallet.app.util;

import android.os.Environment;

import java.io.File;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

public class SnapshotUtil {
    public static void take(String testName) {
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
        if (!path.exists()) {
            path.mkdirs();
        }
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.takeScreenshot(new File(path, System.currentTimeMillis() + ".png"));
    }
}
