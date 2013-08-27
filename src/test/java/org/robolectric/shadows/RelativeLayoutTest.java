package org.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.Arrays;

import static junit.framework.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class RelativeLayoutTest {

  @Test
  public void getRules_shouldShowAddRuleData() throws Exception {
    ImageView imageView = new ImageView(Robolectric.application);
    RelativeLayout layout = new RelativeLayout(Robolectric.application);
    layout.addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, 1234);
    assertTrue(Arrays.equals(new int[] { 0, 0, 0, 0, 0, 0, 1234, 0, 0, 0, 0, -1, 0, 0, 0, 0 }, layoutParams.getRules()));
  }
}
