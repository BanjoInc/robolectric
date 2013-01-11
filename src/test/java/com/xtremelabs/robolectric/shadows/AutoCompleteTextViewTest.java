package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WithTestDefaultsRunner.class)
public class AutoCompleteTextViewTest {

    private AutoCompleteTextView autoCompleteTextView;

    @Before
    public void setup() throws Exception {
        autoCompleteTextView = new AutoCompleteTextView(new Activity());
    }

    @Test
    public void shouldSetAdapterToAutoCompleteTextView() throws Exception {
        TestAutoCompleteAdapter adapter = new TestAutoCompleteAdapter();
        autoCompleteTextView.setAdapter(adapter);
        ShadowAutoCompleteTextView shadowAutoCompleteTextView = Robolectric.shadowOf(autoCompleteTextView);
        Assert.assertEquals(shadowAutoCompleteTextView.getAdapter(), adapter);
    }

    @Test
    public void shouldRecordShowDropDownCalled() throws Exception {
        ShadowAutoCompleteTextView shadowAutoCompleteTextView = Robolectric.shadowOf(autoCompleteTextView);
        Assert.assertFalse(shadowAutoCompleteTextView.isShowDropDownCalled());
        autoCompleteTextView.showDropDown();
        Assert.assertTrue(shadowAutoCompleteTextView.isShowDropDownCalled());
    }

    @Test
    public void shouldSetOnItemClickListener() throws Exception {
        ShadowAutoCompleteTextView shadowAutoCompleteTextView = Robolectric.shadowOf(autoCompleteTextView);
        TestOnItemClickListener onItemClickListener = new TestOnItemClickListener();
        autoCompleteTextView.setOnItemClickListener(onItemClickListener);
        Assert.assertEquals(shadowAutoCompleteTextView.getOnItemClickListener(), onItemClickListener);
    }

    private class TestAutoCompleteAdapter implements ListAdapter, Filterable {
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Filter getFilter() {
            return null;
        }
    }

    private class TestOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        }
    }
}
