package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Implementation;

@Implements(ImageSpan.class)
public class ShadowImageSpan {

    private Drawable drawable;

    public void __constructor__(Drawable drawable, int verticalAlignment) {
        this.drawable = drawable;
    }

    @Implementation
    public Drawable getDrawable() {
        return drawable;
    }
}
