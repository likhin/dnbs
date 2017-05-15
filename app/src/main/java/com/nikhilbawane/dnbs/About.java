package com.nikhilbawane.dnbs;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreatView STARTED");

        View v = inflater.inflate(R.layout.fragment_about, container, false);
        ((DNBS) getActivity()).getSupportActionBar().setTitle("About");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TextView aboutTitle = (TextView) v.findViewById(R.id.about_title);
            aboutTitle.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        }

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;

            TextView aboutVersion = (TextView) v.findViewById(R.id.about_version);
            aboutVersion.setText("v" + version);
        }
        catch (Exception e) {
            log("aboutVersion: Excpetion: " + e.getMessage());
        }
        return v;
    }

    private void log(String log) {
        Log.i("DNBS", "ABOUT: " + log);
    }
}
