package com.nikhilbawane.dnbs;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Home extends Fragment {

    @BindString(R.string.dnbs_url) String DNBS_URL;

    @BindView(R.id.fabButton) FloatingActionButton fabButton;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.container) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.formFrameLayout) FrameLayout formFrameLayout;
    @BindView(R.id.formCardView) CardView formCardView;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.cancelButton) Button cancelButton;

    @BindView(R.id.titleEditText) EditText editTitle;
    @BindView(R.id.descEditText) EditText editDesc;
    @BindView(R.id.priorityRatingBar) RatingBar ratingBar;
    @BindView(R.id.tagRadioGroup) RadioGroup tagRadioGroup;
    @BindView(R.id.addYearRL) RelativeLayout addYearRL;
    @BindView(R.id.cbYear2) CheckBox cbYear2;
    @BindView(R.id.cbYear3) CheckBox cbYear3;
    @BindView(R.id.cbYear4) CheckBox cbYear4;

    private Animation animation;
    private ValueAnimator colorAnimation;
    private JSONObject userJSON;
    private String userRole;
    private int appStartFlag = 0;
    private int sortFlag = 0;

    // 'notices' contains the full set of Notices received from the server.
    // 'visibleNotices' uses the data from notices by default.
    // All sorting and filtering of Notices is reflected in visibleNotices.
    private List<Notice> notices;
    private List<Notice> visibleNotices;

    private final String FAB_FLAG = "fab_flag";
    private final String SORT_FLAG = "sort_flag";

    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate STARTED");

        setHasOptionsMenu(true);
        if(savedInstanceState != null) {
            // The appStartFlag is used to make sure the FAB's welcome animation is only shown once.
            appStartFlag = savedInstanceState.getInt(FAB_FLAG);
            sortFlag = savedInstanceState.getInt(SORT_FLAG);
        }

        notices = new ArrayList<>();
        visibleNotices = new ArrayList<>();

        settings = getActivity().getSharedPreferences("dnbsPrefs", Context.MODE_PRIVATE);

        try {
            userJSON = new JSONObject(settings.getString("user", null));
            log("userJSON = " + userJSON.toString());
            log("user = " + settings.getString("user", null));
            userRole = userJSON.getString("role");
        }
        catch (Exception e) {
            log("onCreate: JSON: " + e.getMessage());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView STARTED");
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, v);

        if (savedInstanceState != null) {
            appStartFlag = savedInstanceState.getInt(FAB_FLAG);
            sortFlag = savedInstanceState.getInt(SORT_FLAG);
        }

        try {
            if((userJSON.getString("role")).equals("student")) {
                fabButton.setVisibility(View.GONE);
                fabButton.setEnabled(false);
                fabButton.invalidate();
            }
        }
        catch(Exception e) {
            log("onCreateView: JSON: " + e.getMessage());
        }


        if(appStartFlag == 0) {

            fabButton.post(new Runnable() {
                          @Override
                          public void run() {
                              appStartFlag = 1;
                              log("fabButton animation STARTED");
                              animation = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_grow);
                              fabButton.startAnimation(animation);
                          }
                      }
            );
        }

        coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.container);

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        final int width = size.x;

        formCardView.setTranslationX(width);
        formCardView.setEnabled(false);

        Integer colorFrom = getResources().getColor(R.color.clear);
        Integer colorTo = getResources().getColor(R.color.shade);
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                formFrameLayout.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabButton.animate().setDuration(500).setInterpolator(new DecelerateInterpolator(1)).translationX(-width);
                formCardView.animate().setDuration(500).setInterpolator(new DecelerateInterpolator(1)).translationX(0);
                colorAnimation.setDuration(500).start();
                formCardView.setEnabled(true);
                formFrameLayout.setClickable(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_add_notice);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Close the software keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                //Make animations
                fabButton.animate().setDuration(500).setInterpolator(new DecelerateInterpolator(1)).translationX(0);
                formCardView.animate().setDuration(500).setInterpolator(new DecelerateInterpolator(1)).translationX(width);
                colorAnimation.setDuration(500).reverse();

                formCardView.setEnabled(false);
                formFrameLayout.setClickable(false);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_home);
            }
        });

        addYearRL.setVisibility(View.INVISIBLE);
        tagRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.circularsRB:
                        addYearRL.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.noticesRB:
                        addYearRL.setVisibility(View.VISIBLE);
                }
            }
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                refreshNoticeData();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(llm);

        recyclerView.addOnScrollListener(new RecyclerScroll() {
            @Override
            public void show() {
                fabButton.animate().translationY(0)
                        .setInterpolator(new DecelerateInterpolator(2))
                        .start();
            }

            @Override
            public void hide() {
                fabButton.animate().translationY(fabButton.getHeight() + getResources().getDimensionPixelSize(R.dimen.fab_margin))
                        .setInterpolator(new AccelerateInterpolator(2))
                        .start();
            }
        });

        if(appStartFlag == 0) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    loadNotices();
                }
            });

            if(checkNetwork()) {
                new UpdateApp(true).execute();
            }
        }
        else {
            initializeAdapter(notices);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        log("onSaveInstanceState STARTED");

        savedInstanceState.putInt(FAB_FLAG, appStartFlag);
        savedInstanceState.putInt(SORT_FLAG, sortFlag);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        log("onCreateOptionsMenu STARTED");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected STARTED");
        int id = item.getItemId();

        if(id == R.id.action_sort) {
            if(sortFlag == 0) {
                sortFlag = 1;
                initializeAdapter(sortNotices(visibleNotices));
            }
            else {
                sortFlag = 0;
                initializeAdapter(visibleNotices);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Sorts notices by priority.
    // Returns a sorted list without changing the original.
    private List<Notice> sortNotices(List<Notice> list) {
        log("sortNotices() STARTED");
        List<Notice> sortedList = new ArrayList<>();
        sortedList.addAll(list);
        Collections.sort(sortedList, new Comparator<Notice>() {
            @Override
            public int compare(Notice note1, Notice note2) {
                return note2.priority - note1.priority;
            }
        });
        return sortedList;
    }

    // Filters methods by department.
    // Department is set in the Settings tab.
    public void setFilter(String queryText) {
        try {
            swipeRefreshLayout.setEnabled(false);

            log("setFilter(\"" + queryText + "\") STARTED");

            switch (queryText) {
                case "U":
                    break;
                case "D":
                    queryText = ((Integer) userJSON.getInt("year")).toString();
                    log("setFilter: year = " + ((Integer) userJSON.getInt("year")).toString());
                    break;
            }

            log("setFilter: " + queryText);
            visibleNotices.clear();
            queryText = queryText.toLowerCase();

            for (Notice item : notices) {
                final String text = item.tag.toLowerCase();
                if (text.contains(queryText))
                    visibleNotices.add(item);
            }

            initializeAdapter(visibleNotices);
        }
        catch(Exception e) {
            log("setFilter: Exception: " + e.getMessage());
        }
    }

    // Initialises RecyclerView Adapter by passing 'list' as parameter.
    private void initializeAdapter(List<Notice> list){
        log("initializedAdapter() STARTED");
        RVAdapter adapter = new RVAdapter(list, coordinatorLayout, this.getContext(), userRole);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    // Resets visibleNotices to show all Notices.
    public void resetNotices() {
        swipeRefreshLayout.setEnabled(true);
        visibleNotices.clear();
        visibleNotices.addAll(notices);
        initializeAdapter(visibleNotices);
    }

    // Loads Notices from the server.
    // This method checks if device is connected to a network.
    // if connected -> loads Notices
    // else         -> displays Snackbar
    private void refreshNoticeData() {
        log("refreshNoticeData STARTED");

        if (checkNetwork()) {
            new NoticeTransaction(getActivity(), recyclerView,
                    swipeRefreshLayout, notices, 0, userRole).execute();

        } else {
            Snackbar.make(coordinatorLayout, "No network connection available.",
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }


    // Checks whether device is connected to a network.
    private boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

    // Load Notices from SharedPreferences.
    // if null -> Load from server.
    private void loadNotices() {
        if( settings.getString("theJson", null) != null) {
            try
            {
                if(checkNetwork()) {
                    String jason = settings.getString("theJson", null);

                    JSONArray jArray = new JSONArray(jason);
                    log("loadNotices: jArray length: " + jArray.length());

                    visibleNotices.clear();

                    for (int i = 0; i < jArray.length(); i++)
                    {
                        JSONObject json_data = jArray.getJSONObject(i);
                        log("loadNotices: json_data " + i + " length: " + json_data.length());
                        visibleNotices.add(new Notice(json_data.getInt("id"),
                                        json_data.getString("user"),
                                        json_data.getString("title"),
                                        json_data.getString("description"),
                                        json_data.getString("tag"),
                                        json_data.getInt("priority"),
                                        json_data.getString("date")
                                )
                        );
                    }

                    notices.clear();
                    notices.addAll(visibleNotices);
                    initializeAdapter(visibleNotices);
                }
                else {
                    Snackbar.make(coordinatorLayout, "No network connection available.", Snackbar.LENGTH_LONG)
                            .show();
                }
            }
            catch (Exception e)
            {
                log("loadNotices: JSON: " + e.getMessage());
            }
        }
        else {
            log("loadNotices: theJson is empty... initialising data");
            try {
                new NoticeTransaction(getActivity(), recyclerView, swipeRefreshLayout,
                        visibleNotices, 0, userRole).execute().get();
            }
            catch (Exception e) {
                log("loadNotices: Exception: " + e.getMessage());
            }
            notices.clear();
            notices.addAll(visibleNotices);
        }
    }

    @OnClick(R.id.submitButton)
    void sendNotice() {
        if(checkNetwork()) {
            String title = editTitle.getText().toString();
            String desc = editDesc.getText().toString();
            int priority = (int) ratingBar.getRating();
            int radioTag = tagRadioGroup.getCheckedRadioButtonId();

            String tag = null;

            switch(radioTag) {
                case R.id.circularsRB:
                    tag = "U";
                    break;

                case R.id.noticesRB:
                    if (cbYear2.isChecked() || cbYear3.isChecked() || cbYear4.isChecked()) {
                        tag = "";
                        if (cbYear2.isChecked()) {
                            tag = tag + " 2";
                        }
                        if (cbYear3.isChecked()) {
                            tag = tag + " 3";
                        }
                        if (cbYear4.isChecked()) {
                            tag = tag + " 4";
                        }
                    }
                    break;
            }

            if(!title.isEmpty() && !desc.isEmpty() && tag != null && (priority > 0 && priority <= 5)) {
                try {
                    String user = userJSON.getString("username");
                    log("sendNotice: user: " + userJSON.getString("username"));

                    String jason = "{"
                            +   "\"user\":"           + "\"" + user      + "\"" +   ","
                            +   "\"title\":"          + "\"" + title     + "\"" +   ","
                            +   "\"description\":"    + "\"" + desc      + "\"" +   ","
                            +   "\"tag\":"            + "\"" + tag       + "\"" +   ","
                            +   "\"priority\":"       + "\"" + priority  + "\""
                            + "}";

                    JSONObject mJSON = new JSONObject(jason);
                    new NoticeTransaction(getActivity(), mJSON, 1, userRole).execute();
                }
                catch (JSONException e) {
                    log("sendNotice: JSONException: " + e.getMessage());
                }

                editTitle.getText().clear();
                editDesc.getText().clear();
                ratingBar.setRating(0);
                tagRadioGroup.clearCheck();
                addYearRL.setVisibility(View.INVISIBLE);

                cancelButton.callOnClick();
            }
            else {
                Snackbar.make(coordinatorLayout, "Please enter all fields.", Snackbar.LENGTH_LONG)
                        .show();
            }
        }
        else {
            Snackbar.make(coordinatorLayout, "No network connection available.", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public void deleteNotice(final View view, final int noticeId, final Context context, final String role) {
        new AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this notice?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        log("deleteNotice: noticeId: " + noticeId + ", role: " + role);
                        try {
                            String jason = "{'id':'" + noticeId + "'}";

                            if((noticeId != -1) && (role != null)) {
                                JSONObject mJSON = new JSONObject(jason);
                                new NoticeTransaction(view, context, mJSON, 2, role).execute();
                            }
                        } catch (Exception e) {
                            log("deleteNotice: JSONException: " + e.getMessage());
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private class UpdateApp extends AsyncTask<Void, String, Integer> {
        boolean flag;
        ProgressDialog progDialog;

        UpdateApp(boolean flag) {
            this.flag = flag;
        }

        @Override
        protected void onPreExecute() {
            log("UpdateApp: onPreExecute: flag = " + flag);
            super.onPreExecute();
            if(!flag) {
                progDialog = new ProgressDialog(getActivity());
                progDialog.setMessage("Downloading...");
                progDialog.setIndeterminate(false);
                progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progDialog.setCancelable(false);
                progDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... arg) {
            if(flag) {
                try {
                    // Create a URL for the desired page
                    URL url = new URL(DNBS_URL + "/dnbs_update/update.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();

                    int response = conn.getResponseCode();
                    log("UpdateApp: checkVersion: The response is: " + response);

                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();

                    Scanner in = new Scanner(sb.toString()).useDelimiter("[^0-9]+");
                    int integer = in.nextInt();

                    return integer;
                }
                catch (MalformedURLException e) {
                    log("UpdateApp: checkVersion: MalformedURLException: " + e.getMessage());
                    return 0;
                }
                catch (IOException e) {
                    log("UpdateApp: checkVersion: IOException: " + e.getMessage());
                    return 0;
                }
                catch (Exception e) {
                    log("UpdateApp: checkVersion: Exception: " + e.getMessage());
                    return 0;
                }
            }
            else {
                try {
                    int count;
                    URL url = new URL(DNBS_URL + "/dnbs_update/dnbs.apk");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    // getting file length
                    int lengthOfFile = conn.getContentLength();

                    // input stream to read file - with 8k buffer
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    File file = new File(Environment.getExternalStorageDirectory() + "/dnbs/");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File outputFile = new File(file, "update.apk");

                    // Output stream to write file
                    OutputStream output = new FileOutputStream(outputFile);

                    byte data[] = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress(""+(int)((total*100)/lengthOfFile));

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();
                    // closing streams
                    output.close();
                    input.close();

                    return 0;
                }
                catch (IOException e) {
                    log("UpdateApp: Download: IOException: " + e.getMessage());
                    return 0;
                }
                catch (Exception e) {
                    log("UpdateApp: Download: Exception: " + e.getMessage());
                    return 0;
                }
            }
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        public void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if(flag) {
                log("UpdateApp: onPostExecute: i = " + i);
                try {
                    PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    int version = pInfo.versionCode;
                    log("UpdateApp: onPostExecute: version = " + version);
                    if (i > version && i != 404) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Update")
                                .setMessage("New app update is available.\nUpdate now?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        new UpdateApp(false).execute();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .show();
                    }
                } catch (Exception e) {
                    log("UpdateApp: onPostExecute: Exception: " + e.getMessage());
                }
            }
            else {
                progDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/dnbs/" + "update.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private void log(String log) {
        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("DNBS", "HOME: " + log);
        }
    }
}