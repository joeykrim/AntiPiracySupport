/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antipiracy.support;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import static org.antipiracy.support.utils.AntiPiracyConstants.*;

/** 
 * This service blocks the install of known piracy/malware apps. Please report new piracy
 * apps to ROM developers deploying this code.
 * @author github.com/AlmightyMegadeth00 - activethrasher00@gmail.com
 * Note: the current source can be found in github.com/ContentGuard
 */
public class AntiPiracyInstallReceiver extends BroadcastReceiver {
    private static final String TAG = "ContentGuard: Install receiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Intent notifyService = new Intent(ctx, AntiPiracyNotifyService.class);
        if (DEBUG) Log.i(TAG, "install check event");
        boolean displayToast = false;

        for (String app : PACKAGES) {
            if (DEBUG) Log.e(TAG, "PACKAGE " + app + " testing for install");
            
            if (isInstalled(ctx, app)) {
                Log.i("(╯°□°)╯︵ ┻━┻", "Blacklisted packages found: " + app);
                
                // Change the system setting to disable non-market app installs.  This isn't
                // a measure of security as much as it is to increase the inconvenience factor
        		Settings.Global.putInt(ctx.getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS, 0);
                
                if (!isServiceRunning(AntiPiracyNotifyService.class, ctx)) {
                    ctx.startService(notifyService);
                    displayToast = true;
                }
                break;
            }
        }
        
        if (displayToast) {
			Toast.makeText(ctx, "ContentGuard activated", Toast.LENGTH_LONG).show();
		}
    }

    private boolean isServiceRunning(Class<?> serviceClass, Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (DEBUG) Log.i(TAG, "Check service already running");
                return true;
            }
        }
        
        if (DEBUG) Log.i(TAG, "Check service not running");
        return false;
    }

    private boolean isInstalled(Context ctx, final String packageName) {
        final PackageManager pm = ctx.getPackageManager();
        String mVersion;
        
        try {
            mVersion = pm.getPackageInfo(packageName, 0).versionName;
            if (mVersion.equals(null)) {
                return false;
            }
        } catch (NameNotFoundException e) {
            if (DEBUG) Log.e(TAG, "Package " + packageName + " NameNotFoundException" + e);
            return false;
        }
        
        return true;
    }
}
