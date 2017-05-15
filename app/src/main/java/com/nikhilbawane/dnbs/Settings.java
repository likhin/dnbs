package com.nikhilbawane.dnbs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

public class Settings extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView STARTED");

        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        ((DNBS) getActivity()).getSupportActionBar().setTitle("Settings");

        final SharedPreferences settings = getActivity().getSharedPreferences("dnbsPrefs", Context.MODE_PRIVATE);

        try {
            TextView name = (TextView) v.findViewById(R.id.user_name);
            TextView email = (TextView) v.findViewById(R.id.user_email);
            TextView role = (TextView) v.findViewById(R.id.user_role);
            TextView year = (TextView) v.findViewById(R.id.user_year);

            JSONObject userJSON = new JSONObject(settings.getString("user", null));

            name.setText("Name:\t" + userJSON.getString("name"));
            email.setText("Email:\t" + userJSON.getString("email"));
            role.setText("Role:\t" + userJSON.getString("role"));
            switch (userJSON.getInt("year")) {
                case 2 :
                    year.setText("Year:\t\tSecond");
                    break;
                case 3 :
                    year.setText("Year:\t\tThird");
                    break;
                case 4 :
                    year.setText("Year:\t\tFinal");
                    break;
                default:
                    year.setVisibility(View.GONE);
            }
        }
        catch (Exception e) {
            log("UserDetails: Exception: " + e.getMessage());
        }

        Button logout = (Button) v.findViewById(R.id.button_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Clears all user data
                                settings.edit().clear().apply();

                                Intent intent = new Intent(getActivity(), Login.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }
        });

        return v;
    }

    private void log(String log) {
        Log.i("DNBS ", "SETTINGS: " + log);
    }
}
