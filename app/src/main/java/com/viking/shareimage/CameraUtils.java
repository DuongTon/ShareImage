package com.viking.shareimage;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * Created by sonu on 19/01/18.
 */

public class CameraUtils {

    /**
     * method to check if Device support camera or not
     * @param context of calling class
     * @return if camera is supportable or not
     */
    public static boolean isDeviceSupportCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        Toast.makeText(context, "Device doesn't support camera.", Toast.LENGTH_SHORT).show();
        return false;
    }
}
