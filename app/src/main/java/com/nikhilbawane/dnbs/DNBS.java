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

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Created by nikhil on 27/6/15.
 */
public class DNBS extends AppCompatActivity {

    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnbs);

        log("onCreate STARTED");

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new Home())
                    .commit();
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.title_home);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                mToolbar,  /* Toolbar object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            public void onDrawerSlide(View drawerView, float offset) {
                super.onDrawerSlide(drawerView, offset);
                if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Home) {
                    ((Home) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                            .mFAB.setTranslationX(offset * 300);
                }
            }

        };
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Home) {
                    (getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container))
                            .getView().findViewById(R.id.form_cancel).callOnClick();
                } else {
                    onBackPressed();
                }
            }
        });

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        init();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item ) {
        log("onOptionsItemSelected STARTED");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            setFragment(new Settings());
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        log("onPostCreate STARTED");
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log("onConfigurationChanged STARTED");
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed(){
        log("onBackPressed STARTED");

        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            log("onBackPressed: BackStack entry count : " + fm.getBackStackEntryCount());
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mToolbar.setTitle(R.string.title_home);
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mNavigationView.getMenu().findItem(R.id.nav_all).setChecked(true);
            mNavigationView.getMenu().findItem(R.id.nav_circ).setChecked(false);
            mNavigationView.getMenu().findItem(R.id.nav_note).setChecked(false);
            mNavigationView.getMenu().findItem(R.id.nav_settings).setChecked(false);
            mNavigationView.getMenu().findItem(R.id.nav_about).setChecked(false);
        } else {
            log("onBackPressed: nothing on backstack, calling super");
            Toast mToast = Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT);
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                mToast.cancel();
                super.onBackPressed();
                return;
            }
            else {
                mToast.show();
            }
            mBackPressed = System.currentTimeMillis();
        }
        log("onBackPressed: BackStack entry count check : " + fm.getBackStackEntryCount());
    }

    public void resetActionBar(boolean childAction, int drawerMode)
    {
        log("resetActionBar(" + childAction + ", " + drawerMode + ") STARTED");

        if (childAction) {
            log("resetActionBar HOME ENABLED TRUE");
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        mDrawerLayout.setDrawerLockMode(drawerMode);
    }

    public void setFragment(Fragment fragment) {
        log("setFragment(" + fragment.toString() + ") STARTED");
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    int checkedMenuItem;

    private void init() {
        log("init() STARTED");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);

        mNavigationView.getMenu().findItem(R.id.nav_all).setChecked(true);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_all:
                        if (menuItem.getItemId() != checkedMenuItem) {
                            ((Home) getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container))
                                    .resetNotices();
                            mNavigationView.getMenu().findItem(R.id.nav_all).setChecked(true);
                            mNavigationView.getMenu().findItem(R.id.nav_circ).setChecked(false);
                            mNavigationView.getMenu().findItem(R.id.nav_note).setChecked(false);

                        }
                        break;

                    case R.id.nav_circ:
                        if (menuItem.getItemId() != checkedMenuItem) {
                            ((Home) getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container))
                                    .setFilter("U");
                            mNavigationView.getMenu().findItem(R.id.nav_all).setChecked(false);
                            mNavigationView.getMenu().findItem(R.id.nav_circ).setChecked(true);
                            mNavigationView.getMenu().findItem(R.id.nav_note).setChecked(false);
                        }
                        break;

                    case R.id.nav_note:
                        if (menuItem.getItemId() != checkedMenuItem) {
                            ((Home) getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container))
                                    .setFilter("D");
                            mNavigationView.getMenu().findItem(R.id.nav_all).setChecked(false);
                            mNavigationView.getMenu().findItem(R.id.nav_circ).setChecked(false);
                            mNavigationView.getMenu().findItem(R.id.nav_note).setChecked(true);
                        }
                        break;

                    case R.id.nav_settings:
                        setFragment(new Settings());
                        mToolbar.setTitle(menuItem.getTitle());
                        break;

                    case R.id.nav_about:
                        setFragment(new About());
                        mToolbar.setTitle(menuItem.getTitle());
                        break;

                }
                checkedMenuItem = menuItem.getItemId();
                //menuItem.setChecked(true);
                return true;
            }
        });
    }

    public static void log(String log) {
        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("DNBS ", "DNBS: " + log);
        }
    }
}