/*
 * *************************************************************************
 *  Permissions.java
 * **************************************************************************
 *  Copyright © 2015 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */

package com.jarvanmo.exoplayerview.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permissions {

    public static final int PERMISSION_STORAGE_TAG = 255;
    public static final int PERMISSION_SETTINGS_TAG = 254;


    public static final int PERMISSION_SYSTEM_RINGTONE = 42;
    public static final int PERMISSION_SYSTEM_BRIGHTNESS = 43;
    public static final int PERMISSION_SYSTEM_DRAW_OVRLAYS = 44;

    /*
     * Marshmallow permission system management
     */

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean canDrawOverlays(Context context) {
        return !AndroidUtil.isMarshMallowOrLater() || Settings.canDrawOverlays(context);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean canWriteSettings(Context context) {
        return !AndroidUtil.isMarshMallowOrLater() || Settings.System.canWrite(context);
    }

    public static boolean canReadStorage(Context context) {
        return !AndroidUtil.isMarshMallowOrLater() || ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    private static void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_STORAGE_TAG);
    }
}
