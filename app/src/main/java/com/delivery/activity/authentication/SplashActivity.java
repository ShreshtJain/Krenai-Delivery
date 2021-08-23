package com.delivery.activity.authentication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.delivery.HomeActivity;
import com.delivery.R;

import static com.delivery.utils.jkeys.Keys.LoginKeys.STATUS;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    private int returnStatus = 0;
    private LinearLayout alertMessage;
    private LocationManager mLocationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splace);
        updateScreenResolution();
        CheckLocationStatus();
        sharedPreferenceStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        returnStatus = sharedPreferenceStatus.getInt(STATUS, 0);
    }

    private void CheckLocationStatus() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("RETURN STATUS", ":: " + returnStatus);
                if (returnStatus == 0 || returnStatus == 2 || returnStatus == 1) {
                    Intent i = new Intent(SplashActivity.this, LaunchActivity.class);
                    startActivity(i);
                    finish();
                } else if (returnStatus == 4) {
                    if (isOnline()) {
                        if (isLocationServicesEnabled()) {
                            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showSettingsAlert();
                        }
                    } else {
                        alertMessage = findViewById(R.id.alert_message);
                        alertMessage.setVisibility(View.VISIBLE);
                        checkForConnectivity();
                    }
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void checkForConnectivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("connection", "Checking Connection");
                if (isOnline()) {
                    if (isLocationServicesEnabled()) {
                        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    checkForConnectivity();
                }

            }
        }, 2000);
    }

    public boolean isLocationServicesEnabled() {
        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(SplashActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void showSettingsAlert() {

        if ( SplashActivity.this.isFinishing()) {
            return;
        }
        SplashActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        SplashActivity.this);
                alertDialog.setTitle("GPS is settings");
                alertDialog
                        .setMessage("Your GPS is disabled, Enable GPS in settings or continue with approximate location");
                alertDialog.setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                Intent intentRedirectionGPSSettings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intentRedirectionGPSSettings.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivityForResult(intentRedirectionGPSSettings, 0);
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                SplashActivity.this.startActivity(intent);
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                int which) {
                                Toast.makeText(SplashActivity.this, "App need your location", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                alertDialog.create().show();
            }
        });
    }

    private void updateScreenResolution() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

}
