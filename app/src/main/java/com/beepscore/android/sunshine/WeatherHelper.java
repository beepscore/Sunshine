package com.beepscore.android.sunshine;

import android.content.Context;
import android.net.Uri;

/**
 * Created by stevebaker on 6/22/15.
 */
public class WeatherHelper {

    /**
     * URI for weather forecast query from OpenWeatherMap
     * requests result format as json and units as metric
     * @param postalCode specifies a geographic location
     * @param numberOfDays e.g. 7
     * @return uri
     * http://openweathermap.org/API#forecast
     */
    protected static Uri weatherJsonMetricUri(String postalCode, Integer numberOfDays) {
        return weatherUri(postalCode, "json", "metric", numberOfDays);
    }

    /**
     * URI for weather forecast query from OpenWeatherMap
     * @param postalCode specifies a geographic location
     * @param format web service desired result format e.g. json, xml
     * @param units web service desired result units e.g. metric
     * @param numberOfDays e.g. 7
     * @return uri
     * http://openweathermap.org/API#forecast
     */
    public static Uri weatherUri(String postalCode,
                                    String format, String units, Integer numberOfDays) {

        final String SCHEME = "http";

        ////////////////////////////////////////////////////////////////
        // use google.com to test app with a service that returns invalid result FileNotFoundException
        //final String BASE_URL = "google.com";
        // use google.com? to test app with a service that returns invalid result UnknownHostException
        //final String BASE_URL = "google.com?";
        // use google.com/ping to test app with a service that returns empty result
        //final String BASE_URL = "google.com/ping";

        final String BASE_URL = "api.openweathermap.org";
        ////////////////////////////////////////////////////////////////

        final String DATA = "data";
        final String API_VERSION = "2.5";
        final String FORECAST = "forecast";
        final String DAILY = "daily";
        final String QUERY_PARAM = "q";
        final String MODE_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String COUNT_PARAM = "cnt";

        final String APPID_PARAM = "APPID";

        // http://stackoverflow.com/questions/19167954/use-uri-builder-in-android-or-create-url-with-variables
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(BASE_URL)
                .appendPath(DATA)
                .appendPath(API_VERSION)
                .appendPath(FORECAST)
                .appendPath(DAILY)
                .appendQueryParameter(QUERY_PARAM, postalCode)
                .appendQueryParameter(MODE_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(COUNT_PARAM, Integer.toString(numberOfDays))
                .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY);

        Uri uri = builder.build();
        return uri;
    }

    /*
     * @param windDegrees compass direction wind is coming from
     * e.g. 0 degrees is N, increases clockwise
     * @return compass direction wind is coming from e.g. NW, S
     */
    public static String windDirectionCompassPointForWindDegrees(double windDegrees) {
        String windDirectionCompassPoint = "";
        final float DEGREES_PER_EIGHTH_REVOLUTION = 45.f;
        // divide revolution into 8ths. range 0-8 (0 and 8 will both map to North)
        int windDirectionEighths = Math.round(((float)windDegrees/DEGREES_PER_EIGHTH_REVOLUTION));
        // range 0-7
        int windCompassPointIndex = windDirectionEighths % 8;
        switch (windCompassPointIndex) {
            case 0: {
                windDirectionCompassPoint = "N";
                break;
            }
            case 1: {
                windDirectionCompassPoint = "NE";
                break;
            }
            case 2: {
                windDirectionCompassPoint = "E";
                break;
            }
            case 3: {
                windDirectionCompassPoint = "SE";
                break;
            }
            case 4: {
                windDirectionCompassPoint = "S";
                break;
            }
            case 5: {
                windDirectionCompassPoint = "SW";
                break;
            }
            case 6: {
                windDirectionCompassPoint = "W";
                break;
            }
            case 7: {
                windDirectionCompassPoint = "NW";
                break;
            }
            default:
                windDirectionCompassPoint = "";
        }
        return windDirectionCompassPoint;
    }

    /*
     * @param windDegrees compass direction wind is coming from
     * e.g. 0 degrees is N, increases clockwise
     * @return compass direction wind is coming from e.g. northwest, south
     * suitable for accessibility text to speech
     */
    public static String windWordDirectionCompassPointForWindDegrees(double windDegrees) {
        String windDirectionCompassPoint = "";
        final float DEGREES_PER_EIGHTH_REVOLUTION = 45.f;
        // divide revolution into 8ths. range 0-8 (0 and 8 will both map to North)
        int windDirectionEighths = Math.round(((float)windDegrees/DEGREES_PER_EIGHTH_REVOLUTION));
        // range 0-7
        int windCompassPointIndex = windDirectionEighths % 8;
        switch (windCompassPointIndex) {
            case 0: {
                windDirectionCompassPoint = "north";
                break;
            }
            case 1: {
                windDirectionCompassPoint = "northeast";
                break;
            }
            case 2: {
                windDirectionCompassPoint = "east";
                break;
            }
            case 3: {
                windDirectionCompassPoint = "southeast";
                break;
            }
            case 4: {
                windDirectionCompassPoint = "south";
                break;
            }
            case 5: {
                windDirectionCompassPoint = "southwest";
                break;
            }
            case 6: {
                windDirectionCompassPoint = "west";
                break;
            }
            case 7: {
                windDirectionCompassPoint = "northwest";
                break;
            }
            default:
                windDirectionCompassPoint = "";
        }
        return windDirectionCompassPoint;
    }

    public static String windContentDescriptionForSpeedAndDegrees(double windSpeed,
                                                                  double windDegrees) {
        String description = "Wind: "
                + String.valueOf((int)windSpeed)
                + " km per hr "
                + WeatherHelper.windWordDirectionCompassPointForWindDegrees(windDegrees);
        return description;
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * https://gist.github.com/anonymous/cde59615245aa5beb6b0
     *
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        // http://openweathermap.org/weather-conditions
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * https://gist.github.com/anonymous/cde59615245aa5beb6b0
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        // http://openweathermap.org/weather-conditions
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    /**
     * Helper method to provide url according to the weather condition id returned
     * by the OpenWeatherMap call.
     * https://gist.github.com/anonymous/cde59615245aa5beb6b0
     * @param context
     * @param weatherId from OpenWeatherMap API response
     * @return resource url for the corresponding image. null if no relation is found.
     */
    public static Uri getArtUrlForWeatherCondition(Context context, int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        // http://openweathermap.org/weather-conditions
        // http://openweathermap.org/img/w/10d.png returns image size 50x50 pixels

        String img_name = "01d.png";

        if (weatherId >= 200 && weatherId <= 232) {
            //storm
            img_name = "11d.png";
        } else if (weatherId >= 300 && weatherId <= 321) {
            //light_rain
            img_name = "09d.png";
        } else if (weatherId >= 500 && weatherId <= 504) {
            //rain
            img_name = "10d.png";
        } else if (weatherId == 511) {
            //freezing_rain
            img_name = "13d.png";
        } else if (weatherId >= 520 && weatherId <= 531) {
            //rain
            img_name = "09d.png";
        } else if (weatherId >= 600 && weatherId <= 622) {
            //snow
            img_name = "13d.png";
        } else if (weatherId >= 701 && weatherId <= 761) {
            //fog
            img_name = "50d.png";
        } else if (weatherId == 761 || weatherId == 781) {
            //storm
            img_name = "50d.png";
        } else if (weatherId == 800) {
            //clear
            img_name = "01d.png";
        } else if (weatherId == 801) {
            //light_clouds
            img_name = "02d.png";
        } else if (weatherId >= 802 && weatherId <= 804) {
            //clouds
            img_name = "03d.png";
        }

        final String SCHEME = "http";
        final String BASE_URL = "api.openweathermap.org";
        final String IMG = "img";
        final String W = "w";

        // http://stackoverflow.com/questions/19167954/use-uri-builder-in-android-or-create-url-with-variables
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(BASE_URL)
                .appendPath(IMG)
                .appendPath(W)
                .appendPath(img_name);

        Uri uri = builder.build();
        return uri;
    }
}
