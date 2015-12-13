package com.beepscore.android.sunshine;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * LocationEditTextPreference is a type of view
 * Created by stevebaker on 11/11/15.
 */
public class LocationEditTextPreference extends EditTextPreference {

    static final Integer DEFAULT_MINIMUM_LOCATION_LENGTH = 0;
    protected Integer mMinLength;

    /**
     * Constructor method
     * @param context
     * @param attrs
     * http://developer.android.com/training/custom-views/create-view.html#customattr
     * http://stackoverflow.com/questions/3441396/defining-custom-attrs
     * http://stackoverflow.com/questions/16357334/android-custom-view-obtainstyledattributes
     */
    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.LocationEditTextPreference, 0, 0);

        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,
                    DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
    }

}
