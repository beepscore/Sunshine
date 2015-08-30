package com.beepscore.android.sunshine;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by stevebaker on 8/30/15.
 * Reference Lesson 5 Create your own view
 * https://developer.android.com/reference/android/view/View.html
 */
public class MyView extends View {
    /**
     * constructor for creating view through code
     */
    public MyView(Context context) {
        super(context);
    }

    /**
     * constructor for inflating a view from XML
     */
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * constructor for inflating a view from XML and
     * apply a class-specific base style from a theme attribute
     */
    public MyView(Context context, AttributeSet attrs, int defaultStyleAttr) {
        super(context, attrs, defaultStyleAttr);
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
}
