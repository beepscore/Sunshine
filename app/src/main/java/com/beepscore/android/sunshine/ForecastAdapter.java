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

import com.beepscore.android.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
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

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        updateWeatherProperties(cursor);

        LinearLayout list_item_layout = (LinearLayout)view;

        ImageView imageView = (ImageView)list_item_layout.findViewById(R.id.list_item_forecast_imageview);
        imageView.setImageResource(imageResourceForWeatherDescription(weatherDesc));

        RelativeLayout dayAndDescription = (RelativeLayout)list_item_layout.findViewById(R.id.list_item_forecast_day_description);

        TextView dayView = (TextView)dayAndDescription.findViewById(R.id.list_item_forecast_day);
        dayView.setText(weatherDay);

        TextView descriptionView = (TextView)dayAndDescription.findViewById(R.id.list_item_forecast_description);
        descriptionView.setText(weatherDesc);

        RelativeLayout temperaturesView = (RelativeLayout)list_item_layout.findViewById(R.id.list_item_temperatures);
        TextView temperatureMaxTextView = (TextView)temperaturesView.findViewById(R.id.temperature_max_text_view);
        String temperatureMax = Utility.formatTemperature(weatherTemperatureMax, true)
                + "°";
        temperatureMaxTextView.setText(temperatureMax);

        TextView temperatureMinTextView = (TextView)temperaturesView.findViewById(R.id.temperature_min_text_view);
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
}
