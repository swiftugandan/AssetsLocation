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
    private Button submitLocationBtn;
    private TextView resultTxt;
    private EditText assetId;
    private EditText assetNotes;
    private OkHttpClient client;
    private JSONObject data;
    private String url = "http://demo4252817.mockable.io/";

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
        submitLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                try
                {
                    Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse("cz.destil.gpsaveraging.AVERAGED_LOCATION"));
                    startActivityForResult(intent, 0);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
        client = new OkHttpClient();
    }

    private void submitLocation(String url, RequestBody requestBody){
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
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
            RequestBody requestBody = new FormBody.Builder()
                    .add("id", assetId.getText().toString())
                    .add("notes", assetNotes.getText().toString())
                    .add("lat", String.valueOf(bundle.getDouble("latitude")))
                    .add("lon", String.valueOf(bundle.getDouble("longitude")))
                    .add("alt", String.valueOf(bundle.getDouble("altitude")))
                    .add("acc", String.valueOf(bundle.getDouble("accuracy")))
                    .build();
            submitLocation(url, requestBody);
        }
        else
        {
            //handle cancel
        }
    }
}
