package com.mradzinski.caster;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;

import androidx.annotation.CallSuper;

/**
 * Fullscreen media controls
 */
public class ExpandedControlsActivity extends ExpandedControllerActivity {

    @CallSuper
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        applyStyle();
    }

    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.caster_discovery, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.caster_media_route_menu_item);
        return true;
    }

    private void applyStyle() {
        ExpandedControlsStyle style = Caster.expandedControlsStyle;

        if (style != null) {
            if (style.getSeekbarLineColor() != 0) {
                getSeekBar().getProgressDrawable().setColorFilter(style.getSeekbarLineColor(), PorterDuff.Mode.SRC_ATOP);
            }

            if (style.getSeekbarThumbColor() != 0) {
                getSeekBar().getThumb().setColorFilter(style.getSeekbarThumbColor(), PorterDuff.Mode.SRC_ATOP);
            }

            if (style.getStatusTextColor() != 0) {
                getStatusTextView().setTextColor(style.getStatusTextColor());
            }
        }
    }
}
