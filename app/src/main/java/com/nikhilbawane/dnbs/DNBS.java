package com.nikhilbawane.dnbs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DNBS extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MyMaterialTheme);
        setContentView(R.layout.activity_dnbs);
        ButterKnife.bind(this);

        log("onCreate STARTED");

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new Home())
                    .commit();
        }

        mToolbar.setTitle(R.string.title_home);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item ) {
        log("onOptionsItemSelected STARTED");
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            setFragment(new Settings());
            return true;
        }
        else if (id == R.id.action_about) {
            setFragment(new About());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed(){
        log("onBackPressed STARTED");

        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            log("onBackPressed: BackStack entry count : " + fm.getBackStackEntryCount());
            mToolbar.setTitle(R.string.title_home);
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    public void setFragment(Fragment fragment) {
        log("setFragment(" + fragment.toString() + ") STARTED");
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    public static void log(String log) {
        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            Log.i("DNBS ", "DNBS: " + log);
        }
    }
}