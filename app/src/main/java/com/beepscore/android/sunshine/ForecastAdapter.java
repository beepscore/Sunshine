package com.beepscore.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 * Data source is a cursor from database, destination is a list item.
 */
public class ForecastAdapter extends CursorAdapter {

    private String weatherDay = "";
    private String weatherDesc = "";
    private double weatherTemperatureMax;
    private double weatherTemperatureMin;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     *  Must override abstract methods from CursorAdapter
     *  @return a list item view without data values
     *  Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /**
     *  Must override abstract methods from CursorAdapter
     *  This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        updateWeatherProperties(cursor);

        ImageView imageView = (ImageView)view.findViewById(R.id.list_item_icon);
        imageView.setImageResource(imageResourceForWeatherDescription(weatherDesc));

        TextView dayView = (TextView)view.findViewById(R.id.list_item_date_textview);
        dayView.setText(weatherDay);

        TextView descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        descriptionView.setText(weatherDesc);

        TextView temperatureMaxTextView = (TextView)view.findViewById(R.id.list_item_high_text_view);
        String temperatureMax = Utility.formatTemperature(weatherTemperatureMax, true)
                + "°";
        temperatureMaxTextView.setText(temperatureMax);

        TextView temperatureMinTextView = (TextView)view.findViewById(R.id.list_item_low_text_view);
        String temperatureMin = Utility.formatTemperature(weatherTemperatureMin, true)
                + "°";
        temperatureMinTextView.setText(temperatureMin);
    }

    private void updateWeatherProperties(Cursor cursor) {
        weatherDay = Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
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
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask -
        but now we go straight from the cursor to the string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

}
