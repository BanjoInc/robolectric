package com.xtremelabs.robolectric.shadows;

import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ListAdapter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(AutoCompleteTextView.class)
public class ShadowAutoCompleteTextView extends ShadowEditText{
    private boolean showDropDownCalled;
    private ListAdapter adapter;
    private AdapterView.OnItemClickListener onItemClickListener;

    @Implementation
    public <T extends ListAdapter & Filterable>  void setAdapter(T adapter) {
        this.adapter = adapter;
    }

    @Implementation
    public ListAdapter getAdapter() {
        return adapter;
    }

    @Implementation
    public void showDropDown() {
        showDropDownCalled = true;
    }

    /* Non-Android Accessor*/
    public boolean isShowDropDownCalled() {
        return showDropDownCalled;
    }

    @Implementation
    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /* Deprecated android accessor*/
    @Implementation @Deprecated
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    /* Non-Android Accessor
    *  Reset Drop Down state
    * */
    public void resetDropDown(){
        showDropDownCalled = false;
    }
}
