package com.jarvanmo.exoplayerview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.jarvanmo.exoplayerview.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mo on 16-12-5.
 * this package is com.jarvanmo.exoplayerview.widget
 */

public class SuperAspectRatioFrameLayout extends FrameLayout {

    /**
     * Either the width or height is decreased to obtain the desired aspect ratio.
     */
    public static final int RESIZE_MODE_FIT = 0;
    /**
     * The width is fixed and the height is increased or decreased to obtain the desired aspect ratio.
     */
    public static final int RESIZE_MODE_FIXED_WIDTH = 1;
    /**
     * The height is fixed and the width is increased or decreased to obtain the desired aspect ratio.
     */
    public static final int RESIZE_MODE_FIXED_HEIGHT = 2;

    /**
     * no resize
     * */
    public static final int RESIZE_MODE_NONE = 3;
    @Retention(RetentionPolicy.SOURCE )
    @IntDef({RESIZE_MODE_NONE,RESIZE_MODE_FIXED_HEIGHT,RESIZE_MODE_FIXED_WIDTH, RESIZE_MODE_FIT})
    public @interface ResizeMode{}

    /**
     * The {@link FrameLayout} will not resize itself if the fractional difference between its natural
     * aspect ratio and the requested aspect ratio falls below this threshold.
     * <p>
     * This tolerance allows the view to occupy the whole of the screen when the requested aspect
     * ratio is very close, but not exactly equal to, the aspect ratio of the screen. This may reduce
     * the number of view layers that need to be composited by the underlying system, which can help
     * to reduce power consumption.
     */
    private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

    private float videoAspectRatio;
    private int resizeMode;

    public SuperAspectRatioFrameLayout(Context context) {
        this(context, null);
    }

    public SuperAspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resizeMode = RESIZE_MODE_FIT;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.SuperAspectRatioFrameLayout, 0, 0);
            try {
                resizeMode = a.getInt(R.styleable.SuperAspectRatioFrameLayout_resizeMode, RESIZE_MODE_FIT);
            } finally {
                a.recycle();
            }
        }
    }

    /**
     * Set the aspect ratio that this view should satisfy.
     *
     * @param widthHeightRatio The width to height ratio.
     */
    public void setAspectRatio(float widthHeightRatio) {
        if (this.videoAspectRatio != widthHeightRatio) {
            this.videoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

    /**
     * Sets the resize mode which can be of value {@link #RESIZE_MODE_FIT},
     * {@link #RESIZE_MODE_FIXED_HEIGHT} or {@link #RESIZE_MODE_FIXED_WIDTH}.
     *
     * @param resizeMode The resize mode.
     */
    public void setResizeMode(@SuperAspectRatioFrameLayout.ResizeMode int resizeMode) {
        if (this.resizeMode != resizeMode) {
            this.resizeMode = resizeMode;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (videoAspectRatio == 0) {
            // Aspect ratio not set.
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float viewAspectRatio = (float) width / height;
        float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
        if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
            // We're within the allowed tolerance.
            return;
        }

        switch (resizeMode) {
            case RESIZE_MODE_FIXED_WIDTH:
                height = (int) (width / videoAspectRatio);
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                width = (int) (height * videoAspectRatio);
                break;

            case RESIZE_MODE_NONE:
               return;
            default:
                if (aspectDeformation > 0) {
                    height = (int) (width / videoAspectRatio);
                } else {
                    width = (int) (height * videoAspectRatio);
                }
                break;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

}
