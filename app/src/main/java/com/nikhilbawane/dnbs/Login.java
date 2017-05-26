package com.nikhilbawane.dnbs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    @BindView(R.id.editUser) EditText mEditUser;
    @BindView(R.id.editPass) EditText mEditPass;
    @BindView(R.id.button) Button mButton;
    @BindView(R.id.textResponse) TextView mTextResponse;

    private ProgressDialog progDialog;

    private String DNBS_URL;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate STARTED");
        setTheme(R.style.LoginTheme);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow(); // in Activity's onCreate() for instance
//            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }

        DNBS_URL = getResources().getString(R.string.dnbs_url);

        // Check whether the user is already logged in.
        // If yes: skip login screen and start DNBS activity.
        if(getSharedPreferences("dnbsPrefs", Context.MODE_PRIVATE).getBoolean("login", false)) {
            Intent intent = new Intent(this, DNBS.class);
            startActivity(intent);
            finish();
        }

        progDialog = new ProgressDialog(this);
    }

    @OnClick(R.id.button)
    public void loginButtonClicked() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Check network connectivity
        if(networkInfo != null && networkInfo.isConnected()) {
            String user = mEditUser.getText().toString();
            String pass = mEditPass.getText().toString();

            log("loginButtonClicked: user = " + user + " pass = " + pass);
            if(!user.isEmpty() && !pass.isEmpty()) {
                String jason = "{"
                        +   "\"user\":" + "\"" + user + "\"" + ","
                        +   "\"pass\":" + "\"" + pass + "\""
                        + "}";

                log("loginButtonCLicked: jason = " + jason);
                login(jason);
            }
            else {
                mTextResponse.setText("");
            }
        }
        else {
            mTextResponse.setText("Network connection unavailable");
        }
    }

    private void displayProgressDialog(boolean showDialog) {
        if(showDialog) {
            progDialog.setMessage("Loading...");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }
        else
            progDialog.dismiss();
    }

    private void setTextResponseError() {
        mTextResponse.setText("Incorrect username or password");
    }

    private void login(String jsonText) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, jsonText);
        Request request = new Request.Builder()
                .url(DNBS_URL + "/login.php")
                .post(body)
                .build();
        displayProgressDialog(true);

        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    log("login failure exception: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String result = response.body().string();
                    log("login response : " + result);

                    Login.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // The server returns an empty if there no users found
                            // Thus we can check if we have a valid user
                            if(result.isEmpty()) {
                                displayProgressDialog(false);
                                setTextResponseError();
                            }
                            else {
                                getSharedPreferences("dnbsPrefs", MODE_PRIVATE).edit()
                                        .putString("user", result)
                                        .putBoolean("login", true).apply();
                                Intent intent = new Intent(getBaseContext(), DNBS.class);
                                displayProgressDialog(false);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            });
        }
        catch (Exception e) {
            log("login exception: " + e.getMessage());
        }
    }

    private void log(String log) {
        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("DNBS ", "LOGIN: " + log);
        }
    }
}