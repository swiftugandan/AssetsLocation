package com.cytrack.assetslocation;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by osboxes on 10/03/17.
 */

public class JSONParser {
    /**
     * TAGs Defined Here...
     */
    public static final String TAG = "TAG";
    /**
     * Response
     */
    private static Response response;
    /**
     * Get Table Booking Charge
     *
     * @return JSON Object
     */
    public static JSONObject getDataFromWeb(String url) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            response = client.newCall(request).execute();
            return new JSONObject(response.body().string());
        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        return null;
    }
}
