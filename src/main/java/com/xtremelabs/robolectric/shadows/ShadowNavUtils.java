package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NavUtils;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static android.content.pm.PackageManager.NameNotFoundException;

@Implements(NavUtils.class)
public class ShadowNavUtils {
    public static final String PARENT_ACTIVITY = "android.support.PARENT_ACTIVITY";

    @Implementation
    public static Intent getParentActivityIntent(Activity sourceActivity) {
        String parentActivity = getParentActivityName(sourceActivity);
        if (parentActivity == null) return null;
        return new Intent(Intent.ACTION_MAIN).setClassName(sourceActivity, parentActivity);
    }

    @Implementation
    public static String getParentActivityName(Activity sourceActivity) {
        try {
            return getParentActivityName(sourceActivity, sourceActivity.getComponentName());
        } catch (NameNotFoundException e) {
            // Component name of supplied activity does not exist...?
            throw new IllegalArgumentException(e);
        }
    }

    @Implementation
    public static String getParentActivityName(Context context, ComponentName componentName) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        ActivityInfo info = pm.getActivityInfo(componentName, PackageManager.GET_META_DATA);
        if (info.metaData == null) return null;
        String parentActivity = info.metaData.getString(PARENT_ACTIVITY);
        if (parentActivity == null) return null;
        if (parentActivity.charAt(0) == '.') {
            parentActivity = context.getPackageName() + parentActivity;
        }
        return parentActivity;
    }

    @Implementation
    public static boolean shouldUpRecreateTask(Activity sourceActivity, Intent targetIntent) {
        String action = sourceActivity.getIntent().getAction();
        return action != null && !action.equals(Intent.ACTION_MAIN);
    }
}
