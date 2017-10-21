package com.ashwin.descriptionoverlay;

import android.app.Activity;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by ashwin on 16/10/17.
 */

public class DescriptionOverlaySequence implements DetachedListener {

    public interface OnSequenceItemShownListener {
        void onShow(DescriptionOverlayView itemView, int position);
    }

    public interface OnSequenceItemDismissedListener {
        void onDismiss(DescriptionOverlayView itemView, int position);
    }

    SharedPreferencesManager mSharedPreferencesManager;
    Queue<DescriptionOverlayView> mShowcaseQueue;
    private boolean mSingleUse = false;
    Activity mActivity;
    private DescriptionOverlayConfig mConfig;
    private int mSequencePosition = 0;

    private OnSequenceItemShownListener mOnItemShownListener = null;
    private OnSequenceItemDismissedListener mOnItemDismissedListener = null;

    public DescriptionOverlaySequence(Activity activity) {
        mActivity = activity;
        mShowcaseQueue = new LinkedList<>();
    }

    public DescriptionOverlaySequence(Activity activity, String sequenceID) {
        this(activity);
        this.singleUse(sequenceID);
    }

    public DescriptionOverlaySequence addSequenceItem(View targetView, String content, String dismissText) {
        addSequenceItem(targetView, "", content, dismissText);
        return this;
    }

    public DescriptionOverlaySequence addSequenceItem(View targetView, String title, String content, String dismissText) {

        DescriptionOverlayView sequenceItem = new DescriptionOverlayView.Builder(mActivity)
                .setTarget(targetView)
                .setTitleText(title)
                .setDismissText(dismissText)
                .setContentText(content)
                .build();

        if (mConfig != null) {
            sequenceItem.setConfig(mConfig);
        }

        mShowcaseQueue.add(sequenceItem);
        return this;
    }

    public DescriptionOverlaySequence addSequenceItem(DescriptionOverlayView sequenceItem) {
        mShowcaseQueue.add(sequenceItem);
        return this;
    }

    public void setConfig(DescriptionOverlayConfig config) {
        this.mConfig = config;
    }

    public DescriptionOverlaySequence singleUse(String sequenceID) {
        mSingleUse = true;
        mSharedPreferencesManager = new SharedPreferencesManager(mActivity, sequenceID);
        return this;
    }

    public void setOnItemShownListener(OnSequenceItemShownListener listener) {
        this.mOnItemShownListener = listener;
    }

    public void setOnItemDismissedListener(OnSequenceItemDismissedListener listener) {
        this.mOnItemDismissedListener = listener;
    }

    public boolean hasFired() {
        if (mSharedPreferencesManager.getSequenceStatus() == SharedPreferencesManager.SEQUENCE_FINISHED) {
            return true;
        }
        return false;
    }

    public void start() {
        /**
         * Check if we've already shot our bolt and bail out if so         *
         */
        if (mSingleUse) {
            if (hasFired()) {
                return;
            }

            /**
             * See if we have started this sequence before, if so then skip to the point we reached before
             * instead of showing the user everything from the start
             */
            mSequencePosition = mSharedPreferencesManager.getSequenceStatus();

            if (mSequencePosition > 0) {
                for (int i = 0; i < mSequencePosition; i++) {
                    mShowcaseQueue.poll();
                }
            }
        }


        // do start
        if (mShowcaseQueue.size() > 0)
            showNextItem();
    }

    @Override
    public void onDescriptionOverlayDetached(DescriptionOverlayView descriptionOverlayView, boolean wasDismissed) {
        descriptionOverlayView.setDetachedListener(null);

        /**
         * We're only interested if the showcase was purposefully dismissed
         */
        if (wasDismissed) {
            if (mOnItemDismissedListener != null) {
                mOnItemDismissedListener.onDismiss(descriptionOverlayView, mSequencePosition);
            }

            /**
             * If so, update the prefsManager so we can potentially resume this sequence in the future
             */
            if (mSharedPreferencesManager != null) {
                mSequencePosition++;
                mSharedPreferencesManager.setSequenceStatus(mSequencePosition);
            }

            showNextItem();
        }
    }

    private void showNextItem() {

        if (mShowcaseQueue.size() > 0 && !mActivity.isFinishing()) {
            DescriptionOverlayView sequenceItem = mShowcaseQueue.remove();
            sequenceItem.setDetachedListener(this);
            sequenceItem.show(mActivity);
            if (mOnItemShownListener != null) {
                mOnItemShownListener.onShow(sequenceItem, mSequencePosition);
            }
        } else {
            /**
             * We've reached the end of the sequence, save the fired state
             */
            if (mSingleUse) {
                mSharedPreferencesManager.setFired();
            }
        }
    }

}
