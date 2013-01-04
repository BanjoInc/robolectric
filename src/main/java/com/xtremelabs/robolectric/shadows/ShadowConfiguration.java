package com.xtremelabs.robolectric.shadows;

import android.content.res.Configuration;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.Locale;

@Implements(Configuration.class)
public class ShadowConfiguration {

    @RealObject
    private Configuration realConfiguration;
    
    public int screenLayout;
    public int touchscreen;
    public int orientation;

    @Implementation
    public void setToDefaults() {
        realConfiguration.screenLayout = Configuration.SCREENLAYOUT_LONG_NO |
                Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }
    
    public void setLocale( Locale l ) {
    	realConfiguration.locale = l;
    }

    /**
     * Non-Android accessor.  Use to set screenLayout value
     */
    public void setScreenLayout(int screenLayout) {
        this.screenLayout = screenLayout;
    }
}
