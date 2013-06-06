package com.xtremelabs.robolectric.shadows;

import android.view.Gravity;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
    private int orientation;
    private int gravity = Gravity.TOP | Gravity.START;
    private float weightSum;

    public ShadowLinearLayout() {
        setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    @Implementation
    public int getOrientation() {
        return orientation;
    }

    @Implementation
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getGravity() {
        return gravity;
    }

    @Implementation
    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    @Implementation
    public void setWeightSum(float weightSum) {
        this.weightSum = weightSum;
    }

    @Implementation
    public float getWeightSum() {
        return weightSum;
    }


}
