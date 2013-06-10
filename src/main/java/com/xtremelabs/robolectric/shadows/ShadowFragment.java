package com.xtremelabs.robolectric.shadows;

import java.lang.Object;
import java.lang.String;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.view.View;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Fragment.class)
public class ShadowFragment {
    protected View view;
    protected FragmentActivity activity;
    private String tag;
    private Bundle savedInstanceState;
    private int containerViewId;
    private boolean shouldReplace;
    private Bundle arguments;
    private boolean attached;
    private boolean hasMenu;
    private boolean isAdded;
    private boolean isResumed;

    public void setView(View view) {
        this.view = view;
    }

    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    @Implementation
    public View getView() {
        return view;
    }

    @Implementation
    public FragmentActivity getActivity() {
        return activity;
    }

    @Implementation
    public void startActivity(Intent intent) {
        new FragmentActivity().startActivity(intent);
    }

    @Implementation
    public void startActivityForResult(Intent intent, int requestCode) {
        activity.startActivityForResult(intent, requestCode);
    }

    @Implementation
    final public FragmentManager getFragmentManager() {
        return activity.getSupportFragmentManager();
    }

    @Implementation
    public String getTag() {
        return tag;
    }

    @Implementation
    public Resources getResources() {
        if (activity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        return activity.getResources();
    }

    @Implementation
    public String getString(int id) {
        if (activity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        return getResources().getString(id);
    }

    @Implementation
    public final String getString(int resId, Object... formatArgs) {
        if (activity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        return getResources().getString(resId, formatArgs);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setSavedInstanceState(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
    }

    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }

    public void setContainerViewId(int containerViewId) {
        this.containerViewId = containerViewId;
    }

    public int getContainerViewId() {
        return containerViewId;
    }

    public void setShouldReplace(boolean shouldReplace) {
        this.shouldReplace = shouldReplace;
    }

    public boolean getShouldReplace() {
        return shouldReplace;
    }

    @Implementation
    public Bundle getArguments() {
        return arguments;
    }

    @Implementation
    public void setArguments(Bundle arguments) {
        this.arguments = arguments;
    }

    public void setAttached(boolean isAttached) {
        attached = isAttached;
    }

    public boolean isAttached() {
        return attached;
    }

    @Implementation
    public void setHasOptionsMenu(boolean hasMenu) {
        this.hasMenu = hasMenu;
    }

    public boolean hasMenu() {
        return hasMenu;
    }

    @Implementation
    public final boolean isAdded() {
        return isAdded;
    }

    public void setIsAdded(boolean isAdded) {
        this.isAdded = isAdded;
    }

    @Implementation
    public final boolean isResumed() {
        return isResumed;
    }

    public void setResumed(boolean resumed) {
        isResumed = resumed;
    }

    @Implementation
    public LoaderManager getLoaderManager() {
        return new ShadowLoaderManager();
    }

    @Implementation
    public static Fragment instantiate(Context context, String fname, Bundle args) {
        try {
            Class<?> clazz = Fragment.class.getClassLoader().loadClass(fname);
            Fragment f = (Fragment) clazz.newInstance();
            if (args != null) {
                args.setClassLoader(f.getClass().getClassLoader());
                f.setArguments(args);
            }
            return f;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Implementation
    public static Fragment instantiate(Context context, String fname) {
        return instantiate(context, fname, null);
    }
}
