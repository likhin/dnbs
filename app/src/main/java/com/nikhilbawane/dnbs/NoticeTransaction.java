package com.nikhilbawane.dnbs;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NoticeTransaction extends AsyncTask<Void, Void, Void> {

    private String DNBS_URL;

    private int flag = 0;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Notice> noticeList;
    private Context context;
    private JSONObject jsonObject;
    private View view;
    String role;

    //flag 0 means get and 1 means post.(By default it is get.)
    NoticeTransaction(Context context, RecyclerView mRecyclerView,
                      SwipeRefreshLayout mSwipeRefreshLayout, List<Notice> list,
                      int flag, String role) {
        this.context = context;
        this.mRecyclerView = mRecyclerView;
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        this.noticeList = list;
        this.flag = flag;
        this.role = role;
        DNBS_URL = context.getResources().getString(R.string.dnbs_url);
    }

    NoticeTransaction(Context context, JSONObject jsonObject, int flag, String role) {
        this.jsonObject = jsonObject;
        this.flag = flag;
        this.role = role;
        DNBS_URL = context.getResources().getString(R.string.dnbs_url);
    }

    NoticeTransaction(View view, Context context, JSONObject jsonObject, int flag, String role) {
        this.view = view;
        this.jsonObject = jsonObject;
        this.flag = flag;
        this.role = role;
        DNBS_URL = context.getResources().getString(R.string.dnbs_url);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (flag == 0) {
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... arg) {
        if(flag == 0){ //means get notices
            try{
                log("GET: doInBackground STARTED");

                String link = DNBS_URL + "/getNotice.php";
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                int response = conn.getResponseCode();
                log("GET: The response is: " + response);

                String mStringResponse = convertStreamToString(conn.getInputStream());

                try
                {
                    JSONArray jArray = new JSONArray(mStringResponse);
                    log("GET: jArray length: " + jArray.length());

                    noticeList.clear();

                    for (int i = 0; i < jArray.length(); i++)
                    {
                        JSONObject json_data = jArray.getJSONObject(i);
                        log("GET: json_data " + i + " length: " + json_data.length());
                        noticeList.add(new Notice(json_data.getInt("id"),
                                        json_data.getString("user"),
                                        json_data.getString("title"),
                                        json_data.getString("description"),
                                        json_data.getString("tag"),
                                        json_data.getInt("priority"),
                                        json_data.getString("date")
                                )
                        );
                    }

                    context.getSharedPreferences("dnbsPrefs", Context.MODE_PRIVATE).edit()
                            .putString("theJson",jArray.toString()).apply();
                }
                catch (Exception e)
                {
                    log("GET: JSON : " + e.getMessage());
                }

                conn.disconnect();
            }
            catch(Exception e){
                log("Exception(Get): " + e.getMessage());
            }
        }
        else if(flag == 1){ //means send notice
            try{
                log("POST send: doInBackground STARTED");

                String link = DNBS_URL + "/sendNotice.php";
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoOutput(true);
                conn.connect();

                log("POST send: jsonObject = " + jsonObject.toString());

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                String output = jsonObject.toString();
                writer.write(output);
                writer.flush();
                writer.close();

                log("POST send: jsonObject written to output stream.");

                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                log("POST send: result = " + result.toString());

                conn.disconnect();

            }
            catch(Exception e){
                log("Exception(Post): " + e.getMessage());
            }
        }
        else if(flag == 2){ //means delete notice
            try{
                log("POST delete: doInBackground STARTED");

                String link = DNBS_URL + "/deleteNotice.php";
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoOutput(true);
                conn.connect();

                log("POST delete: jsonObject = " + jsonObject.toString());

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                String output = jsonObject.toString();
                writer.write(output);
                writer.flush();
                writer.close();

                log("POST delete: jsonObject written to output stream.");

                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                log("POST delete: result = " + result.toString());

                conn.disconnect();

            }
            catch(Exception e){
                log("Exception(Post): " + e.getMessage());
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voidArg){
        super.onPostExecute(voidArg);
        if(flag == 0) {
            log("noticeList size is " + noticeList.size());
            RVAdapter adapter = new RVAdapter(noticeList, view, context, role);
            mRecyclerView.setAdapter(adapter);
            mSwipeRefreshLayout.setRefreshing(false);
        } else if(flag == 2) {
            Snackbar.make(view, "Notice deleted.", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void log(String log) {
        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("DNBS ", "NOTICETRANSACTION: " + log);
        }
    }
}
