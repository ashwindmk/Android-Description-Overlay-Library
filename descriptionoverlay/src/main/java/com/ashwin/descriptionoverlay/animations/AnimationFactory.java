package com.ashwin.descriptionoverlay.animations;

import android.graphics.Point;
import android.view.View;

import com.ashwin.descriptionoverlay.DescriptionOverlayView;

/**
 * Created by ashwin on 12/10/17.
 */

public interface AnimationFactory {

    void animateInView(View target, Point point, long duration, AnimationStartListener listener);

    void animateOutView(View target, Point point, long duration, AnimationEndListener listener);

    void animateTargetToPoint(DescriptionOverlayView descriptionOverlayView, Point point);

    public interface AnimationStartListener {
        void onAnimationStart();
    }

    public interface AnimationEndListener {
        void onAnimationEnd();
    }
}
