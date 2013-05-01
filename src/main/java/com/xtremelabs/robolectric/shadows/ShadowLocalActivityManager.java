package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.view.Window;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.tester.android.view.TestWindow;

@Implements(LocalActivityManager.class)
public class ShadowLocalActivityManager {
    private boolean singleMode;
    private Activity parent;
    private Object newInstance;
    private Activity activity;

    public void __constructor__(Activity parent, boolean singleMode) {
        this.parent = parent;
        this.singleMode = singleMode;
    }
    
    @Implementation
    public Window startActivity(String id, Intent intent) {
        try {
            final String clazz = intent.getComponent().getClassName();
            final Class<? extends Activity> aClass = (Class<? extends Activity>) Class.forName(clazz);
            final Constructor<? extends Activity> ctor = aClass.getConstructor();
            activity = ctor.newInstance();
            final Method onCreateMethod = aClass.getDeclaredMethod("onCreate", Bundle.class);
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(activity, (Bundle) null);
            return activity.getWindow();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create class", e);
        }
    }
    
    @Implementation
    public Activity getCurrentActivity() {
        return activity;
    }
}
