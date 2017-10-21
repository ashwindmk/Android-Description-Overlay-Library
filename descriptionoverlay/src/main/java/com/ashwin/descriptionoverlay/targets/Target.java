package com.ashwin.descriptionoverlay.targets;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by ashwin on 12/10/17.
 */

public interface Target {

    Point getPoint();

    Rect getBounds();

    Target NONE = new Target() {
        @Override
        public Point getPoint() {
            return new Point(1000000, 1000000);
        }

        @Override
        public Rect getBounds() {
            Point p = getPoint();
            return new Rect(p.x - 190, p.y - 190, p.x + 190, p.y + 190);
        }
    };

}
