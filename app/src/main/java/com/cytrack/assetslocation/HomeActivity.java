package com.cytrack.assetslocation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    private Button submitLocationBtn;
    private TextView resultTxt;
    private EditText assetId;
    private EditText assetNotes;
    private OkHttpClient client;
    private String url = "http://www.cy-track.com/uFind/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // TODO: delete this once asynctask is used for network access
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        resultTxt = (TextView) findViewById(R.id.result);
        assetId = (EditText) findViewById(R.id.assetid);
        assetNotes = (EditText) findViewById(R.id.assetNotes);
        submitLocationBtn = (Button) findViewById(R.id.submitLocationBtn);
        client = new OkHttpClient();

        submitLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                try
                {
                    Intent intent = new Intent("cz.destil.gpsaveraging.AVERAGED_LOCATION");
                    startActivityForResult(intent, 0);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void submitLocation(String url){
        final Request request = new Request.Builder()
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();

        // TODO: We should use the asynctask instead of running on the main thread
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultTxt.setText("Request Failed!");
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response)  {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            resultTxt.setText(response.body().string());
                        }catch (IOException ioe){
                            resultTxt.setText("Invalid Response!");
                        }

                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = intent.getExtras();

            // Update the location TextView and show the client TextView
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("acc_number", assetId.getText().toString());
            urlBuilder.addQueryParameter("notes", assetNotes.getText().toString());
            urlBuilder.addQueryParameter("lat", String.valueOf(bundle.getDouble("latitude")));
            urlBuilder.addQueryParameter("lng", String.valueOf(bundle.getDouble("longitude")));
            urlBuilder.addQueryParameter("alt", String.valueOf(bundle.getDouble("altitude")));
            urlBuilder.addQueryParameter("acc", String.valueOf(bundle.getDouble("accuracy")));
            urlBuilder.addQueryParameter("cmd", "numValidity");

            submitLocation(urlBuilder.build().toString());
        }
        else {
            //handle cancel
        }
    }
}

