package com.cytrack.assetslocation;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    private Button submitLocationBtn;
    private TextView resultTxt;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // TODO: delete this once asynctask is used for network access
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        resultTxt = (TextView) findViewById(R.id.result);
        submitLocationBtn = (Button) findViewById(R.id.submitLocationBtn);
        submitLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                submitLocation();
            }
        });

        client = new OkHttpClient();
    }

    private void submitLocation(){
        final Request request = new Request.Builder().url("http://demo1680352.mockable.io/").build();

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
}
