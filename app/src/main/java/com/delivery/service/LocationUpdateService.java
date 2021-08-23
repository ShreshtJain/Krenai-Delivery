package com.delivery.service;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.delivery.R;
import com.delivery.utils.context.ApplicationContext;
import com.delivery.utils.network.VolleySingleton;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.DATA_PATH_REFRESH_LOCATION;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LONGITUDE;

public class LocationUpdateService extends Service {

    private static final String TAG = LocationUpdateService.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final long UPDATE_INTERVAL = 180000; // Every 180 seconds.
    private static final long FASTEST_UPDATE_INTERVAL = 30000; // Every 30 seconds
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3; // Every 9 minutes.
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    final static String CHANNEL_ID = "channel_01";
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        sharedPreferenceStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
    }

    public LocationUpdateService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand : Location");
        startForeground(1001, showNotification());
        createLocationRequest();
        return START_STICKY;
    }

    private Notification showNotification() {
        NotificationCompat.Builder notificationBuilder = null;
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),
                ApplicationContext.CHANNEL_ID)
                .setContentTitle("DELIVERY TRACKING")
                .setContentText("Location is important to access this app")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    SharedPreferences.Editor editor = sharedPreferenceStatus.edit();
                    editor.putString(LOCATION_UPDATE_LATITUDE, String.valueOf(location.getLatitude()));
                    editor.putString(LOCATION_UPDATE_LONGITUDE, String.valueOf(location.getLongitude()));
                    editor.apply();
                    refreshUserLocation(location.getLatitude(), location.getLongitude());
                }
            }
        }, null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyCalled");
    }

    private void refreshUserLocation(double latitude, double longitude) {
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        JSONObject requestObject = new JSONObject();
        try {
            Log.d(TAG, String.valueOf(latitude) + String.valueOf(longitude));
            requestObject.put(LOCATION_UPDATE_LATITUDE, latitude);
            requestObject.put(LOCATION_UPDATE_LONGITUDE, longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = CONNECTION_URL + DATA_PATH_REFRESH_LOCATION;
        Log.d(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse != null)
                            if (volleyError.networkResponse.statusCode == 409) {

                            } else {

                            }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + sharedPreferenceStatus.getString(TOKEN, ""));
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String json = new String(
                            response.data,
                            "UTF-8"
                    );
                    if (json.length() == 0) {
                        return Response.success(
                                null,
                                HttpHeaderParser.parseCacheHeaders(response)
                        );
                    } else {
                        return super.parseNetworkResponse(response);
                    }
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

}
