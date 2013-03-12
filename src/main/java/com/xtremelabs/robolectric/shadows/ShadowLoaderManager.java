package com.xtremelabs.robolectric.shadows;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(LoaderManager.class)
public class ShadowLoaderManager extends LoaderManager {
    private int restartLoaderId;
    private LoaderCallbacks<Object> lastestLoaderCallbacks;

    @Override
    public <D> Loader<D> initLoader(int i, Bundle bundle, LoaderCallbacks<D> dLoaderCallbacks) {
        return null;
    }

    @Override
    public <D> Loader<D> restartLoader(int id, Bundle bundle, LoaderCallbacks<D> dLoaderCallbacks) {
        restartLoaderId = id;
        lastestLoaderCallbacks = (LoaderCallbacks<Object>) dLoaderCallbacks;
        return null;
    }

    @Override
    public void destroyLoader(int i) {
    }

    @Override
    public <D> Loader<D> getLoader(int i) {
        return null;
    }

    @Override
    public void dump(String s, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strings) {
    }
}
