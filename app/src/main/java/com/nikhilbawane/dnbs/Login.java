/*
 * Copyright (C) 2016 Nikhil Bawane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nikhilbawane.dnbs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by nikhil on 30/7/15.
 */

public class Login extends AppCompatActivity {

    private EditText mEditUser;
    private EditText mEditPass;
    private Button mButton;
    private TextView mTextResponse;

    private String DNBS_URL;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate STARTED");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        DNBS_URL = getResources().getString(R.string.dnbs_url);

        if(getSharedPreferences("dnbsPrefs", Context.MODE_PRIVATE).getBoolean("login", false)) {
            Intent intent = new Intent(this, DNBS.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_login);

        mEditUser = (EditText) findViewById(R.id.editUser);
        mEditPass = (EditText) findViewById(R.id.editPass);
        mButton = (Button) findViewById(R.id.button);
        mTextResponse = (TextView) findViewById(R.id.textResponse);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connMgr = (ConnectivityManager)
                        getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if(networkInfo != null && networkInfo.isConnected()) {
                    String user = mEditUser.getText().toString();
                    String pass = mEditPass.getText().toString();

                    log("user = " + user + " pass = " + pass);

                    if(!user.isEmpty() && !pass.isEmpty()) {
                        String jason = "{"
                                +   "\"user\":" + "\"" + user + "\"" + ","
                                +   "\"pass\":" + "\"" + pass + "\""
                                + "}";
                        log("jason = " + jason);
                        new LoginTask(jason).execute();
                    }
                    else {
                        mTextResponse.setText("");
                    }
                }
                else {
                    mTextResponse.setText("Network connection unavailable");
                }

            }
        });

    }

    private void setTextResponseError() {
        mTextResponse.setText("Incorrect username or password");
    }


    public class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private String jason;
        private ProgressDialog progDialog;

        public LoginTask(String jason) {
            this.jason = jason;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(Login.this);
            progDialog.setMessage("Loading...");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg) {
            try {
                log("LoginTask STARTED");

                String link = DNBS_URL + "/login.php";
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.connect();

                JSONObject jsonObject = new JSONObject(jason);

                log("LoginTask: JSON: " + jsonObject.toString());

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                String output = jsonObject.toString();
                writer.write(output);
                writer.flush();
                writer.close();

                log("LoginTask: jsonObject written to output stream.");

                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;
                log("LoginTask: reader = " + reader.toString());

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                log("LoginTask: result = " + result);
                log("LoginTask: result = " + result.toString());

                input.close();
                conn.disconnect();

                getSharedPreferences("dnbsPrefs", MODE_PRIVATE).edit()
                    .putString("user", result.toString()).apply();

                // The server returns an empty if there no users found
                // Thus we can check if we have a valid user
                if(!result.toString().equals("")) {
                    // user found
                    return true;
                }
                else {
                    // invalid user (or password)
                    return false;
                }
            }
            catch (JSONException e) {
                log("LoginTask: JSONException: " + e.getMessage());
            }
            catch (IOException e) {
                log("LoginTask: IOException: " + e.getMessage());
            }
            catch(Exception e) {
                log("LoginTask: Exception: " + e.getMessage());
            }

            return false;
        }

        @Override
        public void onPostExecute(Boolean arg) {
            super.onPostExecute(arg);
            log("LoginTask: onPostExecute: arg = " + arg.toString());
            // The doInBackground passes return value to onPostExecute
            // Here we get a Boolean value
            // if the value is true, it means the login was successful
            //    and thus we can enter the app by calling an Intent to DNBS class.
            // else we show an error

            if(arg) {
                getSharedPreferences("dnbsPrefs", MODE_PRIVATE).edit().putBoolean("login", true).apply();
                Intent intent = new Intent(getBaseContext(), DNBS.class);
                startActivity(intent);
                finish();
            }
            else {
                setTextResponseError();
            }

            progDialog.dismiss();
        }
    }

    private void log(String log) {
        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("DNBS ", "LOGIN: " + log);
        }
    }
}