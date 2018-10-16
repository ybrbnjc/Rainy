package one.sable.android.rainy;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {


    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> mDummyForecast = new ArrayList<String>();

        String[] mDummyArray = {"Today - Cloudy - 88/55","Tomorrow - Sunny - 90/75",
                "Friday - Clear - 98/37","Saturday - Cloudy - 88/55","Sunay - Sunny - 90/75",
                "Monday - Clear - 98/37"};
        for (String forecast : mDummyArray) {
            mDummyForecast.add(forecast);
        }

        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,R.id.list_item_forecast_textview,mDummyForecast);

        ListView mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mArrayAdapter);

        return rootView;
    }

    @Override
    @SuppressLint("ResourceType")
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.layout.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh){
            String param = "524901";
            new FetchWeatherTask().execute(param);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */

        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            final Map<String,String> mDayNamesTranslate = new HashMap<String, String>();
            mDayNamesTranslate.put("","");
            mDayNamesTranslate.put("","");
            mDayNamesTranslate.put("","");
            mDayNamesTranslate.put("","");
            mDayNamesTranslate.put("","");
            mDayNamesTranslate.put("","");
            mDayNamesTranslate.put("","");

            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E d.MM HH:mm");
            return format.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String fcJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_ITEMS_COUNT = "cnt";
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_MAIN_METRICS = "main";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "description";

            JSONObject forecastJson = new JSONObject(fcJsonStr);
            int mItemCount = forecastJson.getInt(OWM_ITEMS_COUNT);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[mItemCount];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_MAIN_METRICS);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + ", " + description + ", " + highAndLow;
            }
            Log.v(LOG_TAG, "ResultString is: " + resultStrs[0]);
            return resultStrs;
        }

        @Override
        protected Void doInBackground(String... param) {
            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
                final  String ID_PARAM = "id";
                final String APPID_PARAM = "appid";
                final String FORMAT_PARAM = "units";
                final String MODE_PARAM = "mode";
                final String LANGUAGE_PARAM ="lang";

                Uri builtUri = new Uri.Builder()
                        .scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendQueryParameter(ID_PARAM,param[0])
                        .appendQueryParameter(APPID_PARAM,"2bec85f095f36e589c16cc58de321265")
                        .appendQueryParameter(FORMAT_PARAM,"metric")
                        .appendQueryParameter(MODE_PARAM,"JSON")
                        .appendQueryParameter(LANGUAGE_PARAM,"ru")
                        .build();/*
                Uri.Builder builder = new Uri.Builder()
                        .scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendQueryParameter("appid", "2bec85f095f36e589c16cc58de321265")
                        .appendQueryParameter("id", param[0])
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("mode", "JSON");
                URL url = new URL(builder.build().toString());*/

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG,builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");

                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                try {
                    getWeatherDataFromJson(forecastJsonStr);
                } catch (org.json.JSONException e) {
                    Log.e(LOG_TAG,"JSON parsing err: "+ e.getMessage());

                };
                Log.v(LOG_TAG,forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        return null;
        }
    }
}
