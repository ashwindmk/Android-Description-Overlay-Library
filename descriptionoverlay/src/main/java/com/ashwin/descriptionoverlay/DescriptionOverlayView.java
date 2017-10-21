package com.ashwin.descriptionoverlay;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ashwin.descriptionoverlay.animations.AnimationFactory;
import com.ashwin.descriptionoverlay.animations.CircularRevealAnimationFactory;
import com.ashwin.descriptionoverlay.animations.FadeAnimationFactory;
import com.ashwin.descriptionoverlay.shapes.CircleShape;
import com.ashwin.descriptionoverlay.shapes.NoShape;
import com.ashwin.descriptionoverlay.shapes.RectangleShape;
import com.ashwin.descriptionoverlay.shapes.Shape;
import com.ashwin.descriptionoverlay.targets.Target;
import com.ashwin.descriptionoverlay.targets.ViewTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashwin on 12/10/17.
 */

public class DescriptionOverlayView extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

    private static final String TAG = DescriptionOverlayView.class.getSimpleName();
    List<DescriptionOverlayListener> mListeners;
    private UpdateOnGlobalLayout mLayoutListener;
    private Handler mHandler;
    private SharedPreferencesManager mSharedPreferencesManager;
    private int mMaskColor;
    private boolean mShouldRender = false, mRenderOverNav = false, mSingleUse = false, mDismissOnTouch = false, mTargetTouchable = false, mDismissOnTargetTouch = false, mUseFadeAnimation = false;
    private boolean mWasDismissed = false, mShouldAnimate = true;
    private int mOldHeight, mOldWidth;
    private int mGravity;
    private int mXPosition, mYPosition, mContentBottomMargin, mContentTopMargin, mBottomMargin = 0, mShapePadding = DescriptionOverlayConfig.DEFAULT_SHAPE_PADDING;
    private long mDelayInMillis = DescriptionOverlayConfig.DEFAULT_DELAY, mFadeDurationInMillis = DescriptionOverlayConfig.DEFAULT_FADE_TIME;
    private AnimationFactory mAnimationFactory;
    private DetachedListener mDetachedListener;

    private View mContentBox;
    private TextView mTitleTextView, mContentTextView, mDismissButton;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mEraser;
    private Target mTarget;
    private Shape mShape;

    public DescriptionOverlayView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DescriptionOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DescriptionOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DescriptionOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);

        mListeners = new ArrayList<>();

        // Make sure we add a global layout listener so we can adapt to changes
        mLayoutListener = new UpdateOnGlobalLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

        // Consume touch events
        setOnTouchListener(this);

        mMaskColor = Color.parseColor(DescriptionOverlayConfig.DEFAULT_MASK_COLOR);
        setVisibility(INVISIBLE);

        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.description_overlay_content, this, true);
        mContentBox = contentView.findViewById(R.id.content_box);
        mTitleTextView = (TextView) contentView.findViewById(R.id.tv_title);
        mContentTextView = (TextView) contentView.findViewById(R.id.tv_content);
        mDismissButton = (TextView) contentView.findViewById(R.id.tv_dismiss);
        mDismissButton.setOnClickListener(this);
    }

    /**
     * REDRAW LISTENER - this ensures we redraw after activity finishes laying out
     */
    private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            setTarget(mTarget);
        }
    }

    /**
     * Tells us about the "Target" which is the view we want to anchor to.
     * We figure out where it is on screen and (optionally) how big it is.
     * We also figure out whether to place our content and dismiss button above or below it.
     *
     * @param target
     */
    public void setTarget(Target target) {
        mTarget = target;

        // Update dismiss button state
        updateDismissButton();

        if (mTarget != null) {
            /**
             * If we're on lollipop then make sure we don't draw over the nav bar
             */
            if (!mRenderOverNav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBottomMargin = getSoftButtonsBarSizePort((Activity) getContext());
                FrameLayout.LayoutParams contentLP = (LayoutParams) getLayoutParams();

                if (contentLP != null && contentLP.bottomMargin != mBottomMargin)
                    contentLP.bottomMargin = mBottomMargin;
            }

            // Apply the target position
            Point targetPoint = mTarget.getPoint();
            Rect targetBounds = mTarget.getBounds();
            setPosition(targetPoint);

            // Now figure out whether to put content above or below it
            int height = getMeasuredHeight();
            int midPoint = height / 2;
            int yPos = targetPoint.y;

            int radius = Math.max(targetBounds.height(), targetBounds.width()) / 2;
            if (mShape != null) {
                mShape.updateTarget(mTarget);
                radius = mShape.getHeight() / 2;
            }

            if (yPos > midPoint) {
                // Target is in lower half of screen, we'll sit above it
                mContentTopMargin = 0;
                mContentBottomMargin = (height - yPos) + radius + mShapePadding;
                mGravity = Gravity.BOTTOM;
            } else {
                // Target is in upper half of screen, we'll sit below it
                mContentTopMargin = yPos + radius + mShapePadding;
                mContentBottomMargin = 0;
                mGravity = Gravity.TOP;
            }
        }

        applyLayoutParams();
    }

    private void updateDismissButton() {
        // Hide or show button
        if (mDismissButton != null) {
            if (TextUtils.isEmpty(mDismissButton.getText())) {
                mDismissButton.setVisibility(GONE);
            } else {
                mDismissButton.setVisibility(VISIBLE);
            }
        }
    }

    private void applyLayoutParams() {
        if (mContentBox != null && mContentBox.getLayoutParams() != null) {
            FrameLayout.LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

            boolean layoutParamsChanged = false;

            if (contentLP.bottomMargin != mContentBottomMargin) {
                contentLP.bottomMargin = mContentBottomMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.topMargin != mContentTopMargin) {
                contentLP.topMargin = mContentTopMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.gravity != mGravity) {
                contentLP.gravity = mGravity;
                layoutParamsChanged = true;
            }

            /**
             * Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
             */
            if (layoutParamsChanged) {
                mContentBox.setLayoutParams(contentLP);
            }
        }
    }

    void setPosition(Point point) {
        setPosition(point.x, point.y);
    }

    void setPosition(int x, int y) {
        mXPosition = x;
        mYPosition = y;
    }

    private void setTargetTouchable(boolean targetTouchable){
        mTargetTouchable = targetTouchable;
    }

    private void setDismissOnTargetTouch(boolean dismissOnTargetTouch){
        mDismissOnTargetTouch = dismissOnTargetTouch;
    }

    private void setTitleText(CharSequence contentText) {
        if (mTitleTextView != null && !contentText.equals("")) {
            mContentTextView.setAlpha(0.5F);
            mTitleTextView.setText(contentText);
        }
    }

    private void setContentText(CharSequence contentText) {
        if (mContentTextView != null) {
            mContentTextView.setText(contentText);
        }
    }

    private void setDismissText(CharSequence dismissText) {
        if (mDismissButton != null) {
            mDismissButton.setText(dismissText);
            updateDismissButton();
        }
    }

    private void setDismissStyle(Typeface dismissStyle) {
        if (mDismissButton != null) {
            mDismissButton.setTypeface(dismissStyle);
            updateDismissButton();
        }
    }

    private void setTitleTextColor(int textColor) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(textColor);
        }
    }

    private void setContentTextColor(int textColor) {
        if (mContentTextView != null) {
            mContentTextView.setTextColor(textColor);
        }
    }

    private void setDismissTextColor(int textColor) {
        if (mDismissButton != null) {
            mDismissButton.setTextColor(textColor);
        }
    }

    private void setShapePadding(int padding) {
        mShapePadding = padding;
    }

    private void setDismissOnTouch(boolean dismissOnTouch) {
        mDismissOnTouch = dismissOnTouch;
    }

    private void setShouldRender(boolean shouldRender) {
        mShouldRender = shouldRender;
    }

    private void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
    }

    private void setDelay(long delayInMillis) {
        mDelayInMillis = delayInMillis;
    }

    private void setFadeDuration(long fadeDurationInMillis) {
        mFadeDurationInMillis = fadeDurationInMillis;
    }

    public void addDescriptionOverlayListener(DescriptionOverlayListener descriptionOverlayListener) {
        if (mListeners != null)
            mListeners.add(descriptionOverlayListener);
    }

    public void setShape(Shape mShape) {
        this.mShape = mShape;
    }

    private void setUseFadeAnimation(boolean useFadeAnimation) {
        mUseFadeAnimation = useFadeAnimation;
    }

    public void setAnimationFactory(AnimationFactory animationFactory) {
        this.mAnimationFactory = animationFactory;
    }

    void setDetachedListener(DetachedListener detachedListener) {
        this.mDetachedListener = detachedListener;
    }

    public void setConfig(DescriptionOverlayConfig config) {
        this.setDelay(config.getDelay());
        this.setFadeDuration(config.getFadeDuration());
        this.setContentTextColor(config.getContentTextColor());
        this.setDismissTextColor(config.getDismissTextColor());
        this.setDismissStyle(config.getDismissTextStyle());
        this.setMaskColor(config.getMaskColor());
        this.setShape(config.getShape());
        this.setShapePadding(config.getShapePadding());
        this.setRenderOverNavigationBar(config.getRenderOverNavigationBar());
    }

    /**
     * BUILDER CLASS
     * Gives us a builder utility class with a fluent API for eaily configuring showcase views
     */
    public static class Builder {
        private static final int CIRCLE_SHAPE = 0;
        private static final int RECTANGLE_SHAPE = 1;
        private static final int NO_SHAPE = 2;

        private boolean fullWidth = false;
        private int shapeType = CIRCLE_SHAPE;

        final DescriptionOverlayView descriptionView;

        private final Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;
            descriptionView = new DescriptionOverlayView(activity);
        }

        /**
         * Set the title text shown on the description overlay view
         */
        public Builder setTarget(View target) {
            descriptionView.setTarget(new ViewTarget(target));
            return this;
        }

        /**
         * Set the title text shown on the description overlay view
         */
        public Builder setDismissText(int resId) {
            return setDismissText(activity.getString(resId));
        }

        public Builder setDismissText(CharSequence dismissText) {
            descriptionView.setDismissText(dismissText);
            return this;
        }

        public Builder setDismissStyle(Typeface dismissStyle) {
            descriptionView.setDismissStyle(dismissStyle);
            return this;
        }

        /**
         * Set the content text shown on the description overlay view
         */
        public Builder setContentText(int resId) {
            return setContentText(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the description overlay view
         */
        public Builder setContentText(CharSequence text) {
            descriptionView.setContentText(text);
            return this;
        }

        /**
         * Set the title text shown on the DescriptionView
         */
        public Builder setTitleText(int resId) {
            return setTitleText(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the description overlay view as the title.
         */
        public Builder setTitleText(CharSequence text) {
            descriptionView.setTitleText(text);
            return this;
        }

        /**
         * Set whether or not the target view can be touched while the description overlay view is visible.
         * False by default.
         */
        public Builder setTargetTouchable(boolean targetTouchable){
            descriptionView.setTargetTouchable(targetTouchable);
            return this;
        }

        /**
         * Set whether or not the showcase should dismiss when the target is touched.
         * True by default.
         */
        public Builder setDismissOnTargetTouch(boolean dismissOnTargetTouch){
            descriptionView.setDismissOnTargetTouch(dismissOnTargetTouch);
            return this;
        }

        public Builder setDismissOnTouch(boolean dismissOnTouch) {
            descriptionView.setDismissOnTouch(dismissOnTouch);
            return this;
        }

        public Builder setMaskColor(String color) {
            int maskColor = Color.parseColor(color);
            descriptionView.setMaskColor(maskColor);
            return this;
        }

        public Builder setMaskColor(int maskColor) {
            descriptionView.setMaskColor(maskColor);
            return this;
        }

        public Builder setTitleTextColor(int textColor) {
            descriptionView.setTitleTextColor(textColor);
            return this;
        }

        public Builder setContentTextColor(int textColor) {
            descriptionView.setContentTextColor(textColor);
            return this;
        }

        public Builder setDismissTextColor(int textColor) {
            descriptionView.setDismissTextColor(textColor);
            return this;
        }

        public Builder setDelay(int delayInMillis) {
            descriptionView.setDelay(delayInMillis);
            return this;
        }

        public Builder setFadeDuration(int fadeDurationInMillis) {
            descriptionView.setFadeDuration(fadeDurationInMillis);
            return this;
        }

        public Builder setListener(DescriptionOverlayListener listener) {
            descriptionView.addDescriptionOverlayListener(listener);
            return this;
        }

        public Builder singleUse(String showcaseID) {
            descriptionView.singleUse(showcaseID);
            return this;
        }

        public Builder setShape(Shape shape) {
            descriptionView.setShape(shape);
            return this;
        }

        public Builder withCircleShape() {
            shapeType = CIRCLE_SHAPE;
            return this;
        }

        public Builder withoutShape() {
            shapeType = NO_SHAPE;
            return this;
        }

        public Builder setShapePadding(int padding) {
            descriptionView.setShapePadding(padding);
            return this;
        }

        public Builder withRectangleShape() {
            return withRectangleShape(false);
        }

        public Builder withRectangleShape(boolean fullWidth) {
            shapeType = RECTANGLE_SHAPE;
            this.fullWidth = fullWidth;
            return this;
        }

        public Builder renderOverNavigationBar() {
            // Note: This only has an effect in Lollipop or above.
            descriptionView.setRenderOverNavigationBar(true);
            return this;
        }

        public Builder useFadeAnimation() {
            descriptionView.setUseFadeAnimation(true);
            return this;
        }

        public DescriptionOverlayView build() {
            if (descriptionView.mShape == null) {
                switch (shapeType) {
                    case RECTANGLE_SHAPE: {
                        descriptionView.setShape(new RectangleShape(descriptionView.mTarget.getBounds(), fullWidth));
                        break;
                    }
                    case CIRCLE_SHAPE: {
                        descriptionView.setShape(new CircleShape(descriptionView.mTarget));
                        break;
                    }
                    case NO_SHAPE: {
                        descriptionView.setShape(new NoShape());
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unsupported shape type: " + shapeType);
                }
            }

            if (descriptionView.mAnimationFactory == null) {
                // Create our animation factory
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !descriptionView.mUseFadeAnimation) {
                    descriptionView.setAnimationFactory(new CircularRevealAnimationFactory());
                } else {
                    descriptionView.setAnimationFactory(new FadeAnimationFactory());
                }
            }

            return descriptionView;
        }

        public DescriptionOverlayView show() {
            build().show(activity);
            return this.descriptionView;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't bother drawing if we're not ready
        if (!mShouldRender)
            return;

        // Get current dimensions
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        // Don't bother drawing if there is nothing to draw on
        if (width <= 0 || height <= 0)
            return;

        // Build a new canvas if needed i.e first pass or new dimensions
        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {
            if (mBitmap != null)
                mBitmap.recycle();

            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        // Save our 'old' dimensions
        mOldWidth = width;
        mOldHeight = height;

        // Clear canvas
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Draw solid background
        mCanvas.drawColor(mMaskColor);

        // Prepare eraser Paint if needed
        if (mEraser == null) {
            mEraser = new Paint();
            mEraser.setColor(0xFFFFFFFF);
            mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        // Draw (erase) shape
        mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);

        // Draw the bitmap on our views canvas
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    public void onClick(View view) {
        hide();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mDismissOnTouch) {
            hide();
        }
        if (mTargetTouchable && mTarget.getBounds().contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
            if (mDismissOnTargetTouch) {
                hide();
            }
            return false;
        }
        return true;
    }

    private void singleUse(String showcaseID) {
        mSingleUse = true;
        mSharedPreferencesManager = new SharedPreferencesManager(getContext(), showcaseID);
    }

    /**
     * Reveal the description overlay view. Returns a boolean telling us whether we actually did show anything
     */
    public boolean show(final Activity activity) {
        /**
         * If we're in single use mode and have already shot our bolt then do nothing
         */
        if (mSingleUse) {
            if (mSharedPreferencesManager.hasFired()) {
                return false;
            } else {
                mSharedPreferencesManager.setFired();
            }
        }

        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setShouldRender(true);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mShouldAnimate) {
                    animateIn();
                } else {
                    setVisibility(VISIBLE);
                    notifyOnDisplayed();
                }
            }
        }, mDelayInMillis);

        updateDismissButton();

        return true;
    }

    public void hide() {
        /**
         * This flag is used to indicate to onDetachedFromWindow that the showcase view was dismissed purposefully (by the user or programmatically)
         */
        mWasDismissed = true;
        if (mShouldAnimate) {
            animateOut();
        } else {
            removeFromWindow();
        }
    }

    public void animateIn() {
        setVisibility(INVISIBLE);
        mAnimationFactory.animateInView(this, mTarget.getPoint(), mFadeDurationInMillis,
                new AnimationFactory.AnimationStartListener() {
                    @Override
                    public void onAnimationStart() {
                        setVisibility(View.VISIBLE);
                        notifyOnDisplayed();
                    }
                }
        );
    }

    public void animateOut() {
        mAnimationFactory.animateOutView(this, mTarget.getPoint(), mFadeDurationInMillis,
                new AnimationFactory.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        setVisibility(INVISIBLE);
                        removeFromWindow();
                    }
                }
        );
    }

    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    private void setRenderOverNavigationBar(boolean mRenderOverNav) {
        this.mRenderOverNav = mRenderOverNav;
    }

    private void notifyOnDisplayed() {
        if (mListeners != null) {
            for (DescriptionOverlayListener listener : mListeners) {
                listener.onDescriptionOverlayDisplayed(this);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /**
         * If we're being detached from the window without the mWasDismissed flag then we weren't purposefully dismissed
         * Probably due to an orientation change or user backed out of activity.
         * Ensure we reset the flag so the showcase display again.
         */
        if (!mWasDismissed && mSingleUse && mSharedPreferencesManager != null) {
            mSharedPreferencesManager.resetShowcase();
        }
        notifyOnDismissed();
    }

    private void notifyOnDismissed() {
        if (mListeners != null) {
            for (DescriptionOverlayListener listener : mListeners) {
                listener.onDescriptionOverlayDismissed(this);
            }
            mListeners.clear();
            mListeners = null;
        }

        /**
         * Internal listener used by sequence for storing progress within the sequence
         */
        if (mDetachedListener != null) {
            mDetachedListener.onDescriptionOverlayDetached(this, mWasDismissed);
        }
    }

    public void removeFromWindow() {
        if (getParent() != null && getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mEraser = null;
        mAnimationFactory = null;
        mCanvas = null;
        mHandler = null;

        getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
        mLayoutListener = null;

        if (mSharedPreferencesManager != null)
            mSharedPreferencesManager.close();

        mSharedPreferencesManager = null;
    }
}
