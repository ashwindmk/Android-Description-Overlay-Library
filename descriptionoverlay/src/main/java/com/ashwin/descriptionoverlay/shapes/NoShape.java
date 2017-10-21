package com.ashwin.descriptionoverlay.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.ashwin.descriptionoverlay.targets.Target;

/**
 * Created by ashwin on 13/10/17.
 */

public class NoShape implements Shape {

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
        // Do nothing
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void updateTarget(Target target) {
        // Do nothing
    }
}
