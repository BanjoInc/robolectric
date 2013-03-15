package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.RobolectricPackageManager;
import com.xtremelabs.robolectric.res.ViewLoaderTest;
import com.xtremelabs.robolectric.shadows.testing.OnMethodTestActivity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@RunWith(WithTestDefaultsRunner.class)
public class NavUtilsTest {
    private Application application;
    private ViewLoaderTest.ClickActivity sourceActivity;

    @Before
    public void setup() throws Exception {
        application = Robolectric.application;
        RobolectricPackageManager rpm = (RobolectricPackageManager) application.getPackageManager();
        ActivityInfo info = new ActivityInfo();
        info.targetActivity = "com.xtremelabs.robolectric.res.ViewLoaderTest.ClickActivity";
        info.parentActivityName = OnMethodTestActivity.class.getName();
        info.metaData = new Bundle();
        info.metaData.putString(ShadowNavUtils.PARENT_ACTIVITY, OnMethodTestActivity.class.getName());
        rpm.addActivityInfo(info);

        ComponentName componentName = new ComponentName(application, "com.xtremelabs.robolectric.res.ViewLoaderTest.ClickActivity");
        sourceActivity = new ViewLoaderTest.ClickActivity();
        shadowOf(sourceActivity).setComponentName(componentName);
    }

    @Test
    public void getParentActivityIntent_shouldReturnParentActivityIntent() throws Exception {
        Intent intent = NavUtils.getParentActivityIntent(sourceActivity);
        Class<?> intentClass = shadowOf(intent).getIntentClass();
        Assert.assertEquals(intentClass, OnMethodTestActivity.class);
    }
}
