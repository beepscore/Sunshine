package com.beepscore.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 * Data source is a cursor from database, destination is a list item.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    private int weatherId;
    private String weatherDay = "";
    private String weatherDesc = "";
    private double weatherTemperatureMax;
    private double weatherTemperatureMin;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int posiition) {
        return (posiition == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /**
     *  Must override abstract methods from CursorAdapter
     *  @return a list item view without data values
     *  Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == 0) {
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        // tag can store any object
        view.setTag(viewHolder);

        return view;
    }

    /**
     *  Must override abstract methods from CursorAdapter
     *  This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        updateWeatherProperties(context, cursor);

        ViewHolder viewHolder = (ViewHolder)view.getTag();

        // TODO: Consider use weatherId to get weather icon
        viewHolder.iconView.setImageResource(imageResourceForWeatherDescription(weatherDesc));

        viewHolder.dateView.setText(weatherDay);

        viewHolder.descriptionView.setText(weatherDesc);

        String temperatureMax = Utility.formatTemperature(context,
                weatherTemperatureMax,
                Utility.isMetric(context));
        viewHolder.temperatureMaxView.setText(temperatureMax);

        String temperatureMin = Utility.formatTemperature(context,
                weatherTemperatureMin,
                Utility.isMetric(context));
        viewHolder.temperatureMinView.setText(temperatureMin);
    }

    private void updateWeatherProperties(Context context, Cursor cursor) {

        // get weatherId as shown in Lesson 5 gist
        // https://gist.github.com/udacityandroid/b23e847ec824b62877d4
        weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        //weatherDay = Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        weatherDay = Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        weatherTemperatureMax = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        weatherTemperatureMin = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
    }

    private int imageResourceForWeatherDescription(String weatherDescription) {
        int imageResource = -1;
        switch (weatherDescription) {
            case "Clear": {
                imageResource = R.drawable.ic_clear;
                break;
            }
            case "Clouds": {
                imageResource = R.drawable.ic_cloudy;
                break;
            }
            case "Extreme": {
                imageResource = R.drawable.ic_storm;
                break;
            }
            case "Rain": {
                imageResource = R.drawable.ic_rain;
                break;
            }
            case "Snow": {
                imageResource = R.drawable.ic_snow;
                break;
            }
            default:
                imageResource = R.drawable.ic_launcher;
        }
        return imageResource;
    }

    /**
     * View holder for list item view
     * Searches view hierarchy and caches children as properties to eliminate repeated searching
     * http://developer.android.com/training/improving-layouts/smooth-scrolling.html
     * http://developer.android.com/training/contacts-provider/display-contact-badge.html#ListView
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView temperatureMaxView;
        public final TextView temperatureMinView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            temperatureMaxView = (TextView) view.findViewById(R.id.list_item_high_text_view);
            temperatureMinView = (TextView) view.findViewById(R.id.list_item_low_text_view);
        }
    }
}
