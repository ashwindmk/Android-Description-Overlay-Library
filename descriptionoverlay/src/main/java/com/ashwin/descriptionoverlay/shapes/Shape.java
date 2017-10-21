package com.ashwin.descriptionoverlay.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.ashwin.descriptionoverlay.targets.Target;

/**
 * Created by ashwin on 12/10/17.
 */

public interface Shape {

    /**
     * Draw shape on the canvas with the center at (x, y) using Paint object provided.
     */
    void draw(Canvas canvas, Paint paint, int x, int y, int padding);

    /**
     * Get width of the shape.
     */
    int getWidth();

    /**
     * Get height of the shape.
     */
    int getHeight();

    /**
     * Update shape bounds if necessary
     */
    void updateTarget(Target target);

}
