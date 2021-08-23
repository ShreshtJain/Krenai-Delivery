package com.delivery.activity.authentication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.delivery.R;
import com.delivery.utils.jkeys.Keys;

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int READ_LOCATION_PERMISSIONS_REQUEST = 1;
    private Button buttonLocation;
    private TextView buttonLogin;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        //updateScreenResolution();
        //buttonLocation = findViewById(R.id.access_location_button);
//        buttonLocation.setOnClickListener(this);
        buttonLogin = findViewById(R.id.login_button);
        buttonLogin.setOnClickListener(this);
        getLocationPermission();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Keys.CommonResources.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
           /* case R.id.access_location_button:
                intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;*/
            case R.id.login_button:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

//    private void updateScreenResolution() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow(); // in Activity's onCreate() for instance
//            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }
//    }

}

