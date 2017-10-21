package com.ashwin.descriptionoverlay;

import android.graphics.Color;
import android.graphics.Typeface;

import com.ashwin.descriptionoverlay.shapes.CircleShape;
import com.ashwin.descriptionoverlay.shapes.Shape;

/**
 * Created by ashwin on 12/10/17.
 */

public class DescriptionOverlayConfig {

    public static final String DEFAULT_MASK_COLOR = "#dd335075";
    private static final String DEFAULT_CONTENT_TEXT_COLOR = "#ffffff";
    private static final String DEFAULT_DISMISS_TEXT_COLOR = "#ffffff";
    public static final long DEFAULT_FADE_TIME = 300;
    public static final long DEFAULT_DELAY = 0;
    public static final Shape DEFAULT_SHAPE = new CircleShape();
    public static final int DEFAULT_SHAPE_PADDING = 10;

    private long mDelay = DEFAULT_DELAY;
    private static int mMaskColor;
    private Typeface mDismissTextStyle = Typeface.DEFAULT_BOLD;

    private int mContentTextColor;
    private int mDismissTextColor;
    private long mFadeDuration = DEFAULT_FADE_TIME;
    private Shape mShape = DEFAULT_SHAPE;
    private int mShapePadding = DEFAULT_SHAPE_PADDING;
    private boolean renderOverNav = false;

    public DescriptionOverlayConfig() {
        mMaskColor = Color.parseColor(DEFAULT_MASK_COLOR);
        mContentTextColor = Color.parseColor(DEFAULT_CONTENT_TEXT_COLOR);
        mDismissTextColor = Color.parseColor(DEFAULT_DISMISS_TEXT_COLOR);
    }

    public long getDelay() {
        return mDelay;
    }

    public void setDelay(long delay) {
        this.mDelay = delay;
    }

    public static int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
    }

    public int getContentTextColor() {
        return mContentTextColor;
    }

    public void setContentTextColor(int mContentTextColor) {
        this.mContentTextColor = mContentTextColor;
    }

    public int getDismissTextColor() {
        return mDismissTextColor;
    }

    public void setDismissTextColor(int dismissTextColor) {
        this.mDismissTextColor = dismissTextColor;
    }

    public Typeface getDismissTextStyle() {
        return mDismissTextStyle;
    }

    public void setDismissTextStyle(Typeface dismissTextStyle) {
        this.mDismissTextStyle = dismissTextStyle;
    }

    public long getFadeDuration() {
        return mFadeDuration;
    }

    public void setFadeDuration(long fadeDuration) {
        this.mFadeDuration = fadeDuration;
    }

    public Shape getShape() {
        return mShape;
    }

    public void setShape(Shape shape) {
        this.mShape = shape;
    }

    public void setShapePadding(int padding) {
        this.mShapePadding = padding;
    }

    public int getShapePadding() {
        return mShapePadding;
    }

    public boolean getRenderOverNavigationBar() {
        return renderOverNav;
    }

    public void setRenderOverNavigationBar(boolean renderOverNav) {
        this.renderOverNav = renderOverNav;
    }

}
