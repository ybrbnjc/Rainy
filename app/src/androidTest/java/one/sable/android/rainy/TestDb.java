package one.sable.android.rainy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import one.sable.android.rainy.data.WeatherContract.*;
import one.sable.android.rainy.data.WeatherDbHelper;


@RunWith(AndroidJUnit4.class)
public class TestDb {

    private final static String LOG_TAG = TestDb.class.getSimpleName();
    public static String TEST_CITY_NAME = "North Pole";

    @Test
    public void CreateDbTest() {
        Context mContext = InstrumentationRegistry.getTargetContext();
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(mContext,
                WeatherDbHelper.DATABASE_NAME,null,1)
                .getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    ContentValues getLocationContentValues() {
        String testLocationSettings = "99705";
        double testLatitude = 64.772;
        double testLongitude = -147.335;
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        values.put(LocationEntry.COLUMN_LOCATION_SETTINGS, testLocationSettings);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);
        return values;
    }

    ContentValues getWeatherValues(long locRowId) {
        String testDate = "20141205";
        double testDegrees = 1.1;
        double testHumidity =1.2;
        double testPressure = 1.3;
        int testMaxTemp = 75;
        int testMinTemp = 65;
        String testShortDesc = "Asteroids";
        double testWindSpeed = 5.5;
        int testWeatherId = 321;

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, testDate);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, testDegrees);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY,testHumidity);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, testPressure);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, testMaxTemp);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, testMinTemp);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, testShortDesc);
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, testWindSpeed);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, testWeatherId);
        return weatherValues;
    }

    public static void validateCursor (ContentValues expectedValues, Cursor valueCursor) {
        Set<Map.Entry<String,Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String,Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(-1 == idx);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }

    @Test
    public void testInsertReadDb() {
        Context mContext = InstrumentationRegistry.getTargetContext();

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext, WeatherDbHelper.DATABASE_NAME,
                null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long locationRowId;
        ContentValues values = getLocationContentValues();
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            validateCursor(values, cursor);

            ContentValues weatherValues = getWeatherValues(locationRowId);
            long weatherRawId;
            weatherRawId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
            assertTrue(weatherRawId != -1);
            Log.d(LOG_TAG,"WeatherRawId is: " + weatherRawId);

            Cursor weatherCursor = db.query(
                    WeatherEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (weatherCursor.moveToFirst()) {
                validateCursor(weatherValues, weatherCursor);
            } else {
                Log.d(LOG_TAG, "No values in weather table");
            }
        } else {
            Log.d(LOG_TAG, "No values returned:(");
        }
    }
}
