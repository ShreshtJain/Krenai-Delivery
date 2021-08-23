package com.delivery.site;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.delivery.R;
import com.delivery.activity.media.MediaActivity;
import com.delivery.fragment.ProfileFragment;
import com.delivery.site.fragment.HomeSiteManager;
import com.delivery.site.fragment.SiteListing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.MenuItem;
import android.widget.Toast;

public class SiteManagerActivity extends AppCompatActivity {

    Fragment currentFragment = null;
    FragmentTransaction ft;
    private long onRecentBackPressedTime;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    currentFragment = new HomeSiteManager();
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frame_layout, currentFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_site_listing:
                    currentFragment = new SiteListing();
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frame_layout, currentFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_notifications:
                    currentFragment = new ProfileFragment();
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frame_layout, currentFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_siteshot:
                    Intent intent = new Intent(SiteManagerActivity.this, MediaActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_manager_activity);

        currentFragment = new HomeSiteManager();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_layout, currentFragment);
        ft.commit();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() - onRecentBackPressedTime > 2000) {
            onRecentBackPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

}
