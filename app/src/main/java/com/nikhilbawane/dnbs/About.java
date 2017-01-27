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

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by nikhil on 28/6/15.
 */
public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreatView STARTED");

        View v = inflater.inflate(R.layout.fragment_about, container, false);

        //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_about);

        ((DNBS) getActivity()).resetActionBar(true, DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
