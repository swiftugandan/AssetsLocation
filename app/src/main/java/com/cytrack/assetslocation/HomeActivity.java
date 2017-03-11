package com.cytrack.assetslocation;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
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
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    private Button submitLocationBtn;
    private EditText assetId;
    private OkHttpClient client;
    private String url = "http://www.cy-track.com/uFind/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        assetId = (EditText) findViewById(R.id.assetid);
        submitLocationBtn = (Button) findViewById(R.id.submitLocationBtn);

        final TextInputLayout assetIdLayout = (TextInputLayout) findViewById(R.id.assetidLayout);

        client = new OkHttpClient();

        submitLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (assetId.getText().length() > 0) {
                    assetIdLayout.setErrorEnabled(false);
                    try {
                        Intent intent = new Intent("cz.destil.gpsaveraging.AVERAGED_LOCATION");
                        startActivityForResult(intent, 0);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    assetIdLayout.setErrorEnabled(true);
                    assetIdLayout.setError("Please enter an Account Number");
                }

            }
        });
    }

    private void checkAccount(String url) {
        final Request request = new Request.Builder()
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showSnackBarAlert("Request Failed!");
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String error_code = jsonObject.getString(Keys.KEY_ERROR_CODE);
                            String account_name = jsonObject.getString(Keys.KEY_ACCOUNT_NAME);
                            // Check the response code and show the appropriate dialog
                            Log.d(TAG, responseBody);
                            if (error_code.equalsIgnoreCase("02")) {
                                submitAccountDialog(account_name);
                            } else {
                                msgDialog("Invalid Account",
                                        "The account number you submitted is does not exist.");
                            }

                        } catch (IOException ioe) {
                            showSnackBarAlert("Invalid Response!");
                        } catch (JSONException e) {
                            showSnackBarAlert("Invalid Response!");
                        }

                    }
                });
            }
        });
    }


    private void submitAccount(String url) {
        final Request request = new Request.Builder()
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showSnackBarAlert("Request Failed!");
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String error_code = jsonObject.getString(Keys.KEY_ERROR_CODE);
                            Log.d(TAG, responseBody);
                            if (error_code.equalsIgnoreCase("02")) {
                                msgDialog("Account Submitted",
                                        "The account has been successfully updated.");
                                assetId.setText("");
                            } else {
                                msgDialog("Failed",
                                        "An error occured while submitting the account.");
                            }

                        } catch (IOException ioe) {
                            showSnackBarAlert("Invalid Response!");
                        } catch (JSONException e) {
                            showSnackBarAlert("Invalid Response!");
                        }

                    }
                });
            }
        });
    }

    private void showSnackBarAlert(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.parentview), message, Snackbar.LENGTH_LONG);
        ColoredSnackBar.alert(snackbar).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Bundle locationBundle = intent.getExtras();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("acc_number", assetId.getText().toString());
            urlBuilder.addQueryParameter("lat", String.valueOf(locationBundle.getDouble("latitude")));
            urlBuilder.addQueryParameter("lng", String.valueOf(locationBundle.getDouble("longitude")));
            urlBuilder.addQueryParameter("alt", String.valueOf(locationBundle.getDouble("altitude")));
            urlBuilder.addQueryParameter("acc", String.valueOf(locationBundle.getDouble("accuracy")));
            urlBuilder.addQueryParameter("cmd", "numValidity");
            checkAccount(urlBuilder.build().toString());
        } else {
            //handle cancel
        }
    }

    public void submitAccountDialog(String account_name) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.submit_acc_dialog, null);
        final String acc_name = account_name;
        dialogBuilder.setView(dialogView);
        final TextView tv = (TextView) dialogView.findViewById(R.id.accountDetails);
        final EditText accountNotes = (EditText) dialogView.findViewById(R.id.assetNotes);
        tv.setText("This account belongs to " + account_name + ".");
        dialogBuilder.setTitle("Valid Account");
        dialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Submit the validated account, also add notes from accountNotes
                HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
                urlBuilder.addQueryParameter("acc_number", assetId.getText().toString());
                urlBuilder.addQueryParameter("acc_name", acc_name);
                urlBuilder.addQueryParameter("acc_notes", accountNotes.getText().toString());
                urlBuilder.addQueryParameter("cmd", "submitLoc");

                submitAccount(urlBuilder.build().toString());
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    public void msgDialog(String title, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}

