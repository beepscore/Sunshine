package com.beepscore.android.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by stevebaker on 8/30/15.
 * Reference Lesson 5 Create your own view
 * https://developer.android.com/reference/android/view/View.html
 */
public class MyView extends View {

    Paint mBackgroundPaint = null;
    Paint mNeedlePaint = null;
    /**
     * Set mWindDegrees to orient needle
     */
    public double mWindDegrees = 0;

    /**
     * constructor for creating view through code
     */
    public MyView(Context context) {
        super(context);
        init();
    }

    /**
     * constructor for inflating a view from XML
     */
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * constructor for inflating a view from XML and
     * apply a class-specific base style from a theme attribute
     */
    public MyView(Context context, AttributeSet attrs, int defaultStyleAttr) {
        super(context, attrs, defaultStyleAttr);
        init();
    }

    private void init() {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackgroundPaint.setColor(Color.parseColor("#7777cc"));

        mNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNeedlePaint.setStrokeWidth(8);
        mBackgroundPaint.setColor(Color.parseColor("#cc7777"));
    }

    /**
     * http://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation
     */
    @Override
    protected void onMeasure(int wMeasureSpec,
                             int hMeasureSpec) {

        // base class View defaults to 100 x 100
        int hSpecMode = MeasureSpec.getMode(hMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(hMeasureSpec);

        int myHeight = hSpecSize;
        if (hSpecMode == MeasureSpec.EXACTLY) {
            myHeight = hSpecSize;
        } else if (hSpecMode == MeasureSpec.AT_MOST
                || hSpecMode == MeasureSpec.UNSPECIFIED) {
            // typically means layout_height was set to wrap_content
            myHeight = 200;
        }

        int wSpecMode = MeasureSpec.getMode(hMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(hMeasureSpec);
        int myWidth = wSpecSize;
        if (wSpecMode == MeasureSpec.EXACTLY) {
            myWidth = wSpecSize;
        } else if (wSpecMode == MeasureSpec.AT_MOST
                || wSpecMode == MeasureSpec.UNSPECIFIED) {
            // typically means layout_height was set to wrap_content
            myWidth = 200;
        }

        setMeasuredDimension(myWidth, myHeight);
    }

    /**
     * Note onDraw is called every time view is drawn, possibly many times per second
     * For performance, minimize creating and destroying objects within onDraw
     * Instead, can create them as class instance variables
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // add any custom drawing code below

        // http://stackoverflow.com/questions/17954596/how-to-draw-circle-by-canvas-in-android
        // http://developer.android.com/training/custom-views/custom-drawing.html

        int width = getWidth();
        int height = getHeight();

        float cx = width / 2;
        float cy = height / 2;
        float radius = width / 4;
        canvas.drawCircle(cx, cy, radius, mBackgroundPaint);

        float startX = cx;
        float startY = cy;
        // use DEGREES_PER_RADIAN instead of inverse to maintain more accuracy
        final double DEGREES_PER_RADIAN = 180. / Math.PI;
        // coordinate system start at 0, x right Y down
        float stopX = startX + (radius * (float)Math.sin(mWindDegrees/DEGREES_PER_RADIAN));
        float stopY = startX - (radius * (float)Math.cos(mWindDegrees/DEGREES_PER_RADIAN));
        canvas.drawLine(startX, startY, stopX, stopY, mNeedlePaint);
    }
}
