package com.delivery.site;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

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
import com.delivery.activity.authentication.ForgotPasswordActivity;
import com.delivery.activity.authentication.LoginActivity;
import com.delivery.activity.authentication.OneTimePasswordActivity;
import com.delivery.fragment.HomeFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.delivery.R;
import com.delivery.fragment.task.Complete;
import com.delivery.utils.jkeys.Keys;
import com.delivery.utils.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.AUTOCOMPLETE_REQUEST_CODE;
import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_DEMAND;
import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_SITE_MANAGER;
import static com.delivery.utils.jkeys.Keys.CommonResources.DEFAULT_ZOOM;
import static com.delivery.utils.jkeys.Keys.CommonResources.PLACES_API_KEY;
import static com.delivery.utils.jkeys.Keys.LoginKeys.ONE_TIME_PASSWORD;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;
import static com.delivery.utils.jkeys.Keys.SiteManager.DATA_PATH_SITE_UPDATE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_LONGITUDE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DATA_PATH_STATUS_UPDATE;

public class UpdateLocationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    MapView mMapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private Double lat, currentLat, customerLatitude, customerLongitude;
    private Double lang, currentLang;
    private String siteId;
    private TextView textViewSiteLocation, textViewLocationUpdate;
    private ImageView imageViewTakePicture, imageViewCloseButton, imageViewMyLocation, imageViewChangeMapType, imageViewSearchLocation;
    private String address,status;
    private Button buttonUpdateLocation;
    private int mapType = 0, state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_location);
        sharedPreferenceStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        getReference();
        Places.initialize(getApplicationContext(), PLACES_API_KEY);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mMapView = (MapView) findViewById(R.id.map);
        Bundle extra = getIntent().getExtras();
        siteId = extra.getString("site_id");
        status=extra.getString("status");
//        lat = Double.parseDouble(extra.getString("lat"));
  //      lang = Double.parseDouble(extra.getString("lang"));
        customerLatitude = Double.parseDouble(extra.getString("customer_lat"));
        customerLongitude = Double.parseDouble(extra.getString("customer_lang"));
        state = Integer.parseInt(extra.getString("state"));
        if(status.equals("Refund Pickup")) {
            buttonUpdateLocation.setText("ORDER PICKED ?");
        }
        else if(status.equals("Shipped")){
            buttonUpdateLocation.setText("ORDER DELIVERED ?");
        }

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        initMap();
    }

    private void getReference() {
        textViewSiteLocation = findViewById(R.id.location_type);
        imageViewCloseButton = findViewById(R.id.close_button);
        buttonUpdateLocation = findViewById(R.id.update_site_location);
        imageViewChangeMapType = findViewById(R.id.change_map_type);
        imageViewMyLocation = findViewById(R.id.my_location);
        imageViewSearchLocation = findViewById(R.id.search_location);
        buttonUpdateLocation.setOnClickListener(this);
        imageViewCloseButton.setOnClickListener(this);
        imageViewSearchLocation.setOnClickListener(this);
        imageViewMyLocation.setOnClickListener(this);
        imageViewChangeMapType.setOnClickListener(this);
    }

    private void initMap() {
        try {
            MapsInitializer.initialize(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                getLocationPermission();
                updateLocation();
                getDeviceLocation();
            }
        });
    }

    private void updateLocation() {
        if (googleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case Keys.CommonResources.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
        updateLocation();
    }

    private void getDeviceLocation() {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(customerLatitude,
                            customerLongitude)).zoom(DEFAULT_ZOOM).tilt(60).build();
            googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
            new GetLocationAsync(customerLatitude, customerLongitude)
                    .execute();


        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(UpdateLocationActivity.this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                currentLat = mLastKnownLocation.getLatitude();
                                currentLang = mLastKnownLocation.getLongitude();
                            }

                        } else {
                            Toast.makeText(UpdateLocationActivity.this, "MAP LOADING ERROR", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

    }

    @Override
    public void onClick(View view) {
        CameraPosition cameraPosition;
        Intent intent;
        switch (view.getId()) {
            case R.id.close_button:
                finish();
                break;
            case R.id.update_site_location:
                verifyOTP();
                //updateSite();
               // finish();
                break;
            case R.id.search_location:
                    showDirections(currentLat, currentLang, customerLatitude, customerLongitude);
                break;
            case R.id.my_location:
                cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude())).zoom(DEFAULT_ZOOM).tilt(60).build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                new GetLocationAsync(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())
                        .execute();
                break;
            case R.id.change_map_type:
                if (mapType == 0) {
                    mapType = 1;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else {
                    mapType = 0;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                break;
        }
    }


    //Async Task Class
    private class GetLocationAsync extends AsyncTask<String, Void, String> {
        double x, y;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {

            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(UpdateLocationActivity.this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(x, y, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null) {
                address = addresses.get(0).getAddressLine(0);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            textViewSiteLocation.setText(address);
        }
    }

    private void searchLocation() {
        int AUTOCOMPLETE_REQUEST_CODE = 1;
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                textViewSiteLocation.setText(place.getAddress());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(place.getLatLng()).zoom(DEFAULT_ZOOM).tilt(60).build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }
        private void verifyOTP()
        {
            Intent intent = new Intent(UpdateLocationActivity.this, OneTimePasswordActivity.class);
            intent.putExtra("cartId",siteId);
            intent.putExtra("status",status);
            startActivity(intent);
        }
    private void updateSite() {
        String userId = sharedPreferenceStatus.getString(USER_ID, "");
        int uId = (int) Float.parseFloat(userId);

        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("cartId", siteId);
            requestObject.put("deliveryboyId", uId);

                requestObject.put("status", "DELIVERED");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(requestObject);
        String url = CONNECTION_URL_DEMAND + DATA_PATH_STATUS_UPDATE;
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        parseJSONObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse.statusCode == 409) {
                            Toast.makeText(UpdateLocationActivity.this, "Unknown Error.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UpdateLocationActivity.this, "Network Error !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-correlation-id", "true");
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

    private void parseJSONObject(JSONObject jsonObject) {
        startActivity(new Intent(this, Complete.class));
    }

    public void showDirections(double lat, double lng, double lat1, double lng1) {

        final Intent intent = new
                Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" +
                "saddr=" + lat + "," + lng + "&daddr=" + lat1 + "," +
                lng1));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);

    }

}
