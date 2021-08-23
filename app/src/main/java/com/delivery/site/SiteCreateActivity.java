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
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.delivery.R;
import com.delivery.fragment.task.Complete;
import com.delivery.site.fragment.adapter.MarketAdapter;
import com.delivery.site.fragment.entity.Market;
import com.delivery.utils.jkeys.Keys;
import com.delivery.utils.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.AUTOCOMPLETE_REQUEST_CODE;
import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_SITE_MANAGER;
import static com.delivery.utils.jkeys.Keys.CommonResources.DEFAULT_ZOOM;
import static com.delivery.utils.jkeys.Keys.CommonResources.PLACES_API_KEY;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.SiteManager.DATA_PATH_MARKET;
import static com.delivery.utils.jkeys.Keys.SiteManager.DATA_PATH_SITE_CREATE;
import static com.delivery.utils.jkeys.Keys.SiteManager.MARKET_ID;
import static com.delivery.utils.jkeys.Keys.SiteManager.MARKET_NAME;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_DISPLAY_TYPE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_LONGITUDE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_MARKET_ID;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_NAME;

public class SiteCreateActivity extends AppCompatActivity implements View.OnClickListener, MarketAdapter.AdapterCallback {
    private static final String TAG = SiteCreateActivity.class.getSimpleName();
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;

    MapView mMapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private Double lat;
    private Double lang;
    private TextView textViewSiteLocation, textViewSiteMarket;
    private LinearLayout imageViewTakePicture, profileImageUploadLinearLayout;
    private Button linearLayoutCreateSiteButton;
    private String address;
    private AppBarLayout appBarLayout;
    private ImageView imageViewMyLocation, imageViewChangeMapType, imageViewSiteImage,
            imageViewCloseButton, imageViewSearchLocation, imageViewEditMarket;
    private int mapType = 0;
    private int returnStatus = 0;
    private AwesomeValidation validation;
    private AlertDialog alertDialog;
    private List<Market> markets;
    private EditText editTextSiteName;
    private RadioGroup radioGroupDisplayType;
    private String siteName, displayType, latitude, longitude, imageUrl;
    private LinearLayout progress;
    private ImageView arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_location);
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        appBarLayout = findViewById(R.id.app_bar);
        getMarketData();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);
        getReference();
        Places.initialize(getApplicationContext(), PLACES_API_KEY);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        initMap();
    }

    private void getReference() {
        textViewSiteLocation = findViewById(R.id.site_location);
        imageViewTakePicture = findViewById(R.id.take_picture);
        imageViewMyLocation = findViewById(R.id.my_location);
        imageViewChangeMapType = findViewById(R.id.change_map_type);
        imageViewSiteImage = findViewById(R.id.site_image);
        profileImageUploadLinearLayout = findViewById(R.id.profile_image_upload_layout);
        linearLayoutCreateSiteButton = findViewById(R.id.create_site_button);
        imageViewCloseButton = findViewById(R.id.close_button);
        imageViewSearchLocation = findViewById(R.id.search_location);
        textViewSiteMarket = findViewById(R.id.site_market);
        imageViewEditMarket = findViewById(R.id.edit_market);
        textViewSiteMarket.setText(sharedPreferencesStatus.getString(MARKET_NAME, "NOT FOUND"));
        imageViewEditMarket.setOnClickListener(this);
        progress = findViewById(R.id.login_progress);
        arrow = findViewById(R.id.arrow);
        imageViewTakePicture.setOnClickListener(this);
        imageViewSearchLocation.setOnClickListener(this);
        imageViewMyLocation.setOnClickListener(this);
        imageViewChangeMapType.setOnClickListener(this);
        linearLayoutCreateSiteButton.setOnClickListener(this);
        imageViewCloseButton.setOnClickListener(this);

        validation = new AwesomeValidation(ValidationStyle.COLORATION);
        editTextSiteName = findViewById(R.id.site_name);
        radioGroupDisplayType = findViewById(R.id.display_group);
        radioGroupDisplayType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = (RadioButton) findViewById(i);
                displayType = rb.getText().toString();
            }
        });
        validation.addValidation(this, R.id.site_name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.errorMessageName);
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
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(SiteCreateActivity.this, R.raw.style_json));
                                new GetLocationAsync(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())
                                        .execute();
                                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                    @Override
                                    public void onCameraChange(CameraPosition cameraPosition) {
                                        lat = googleMap.getCameraPosition().target.latitude;
                                        lang = googleMap.getCameraPosition().target.longitude;
                                        googleMap.clear();
                                        new GetLocationAsync(lat, lang)
                                                .execute();
                                    }
                                });
                            } else {
                                //isLocationServicesEnabled();
                            }
                        } else {
//                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
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
            case R.id.take_picture:
                intent = new Intent(this, SiteImageActivity.class);
                startActivityForResult(intent, 2);
                break;
            case R.id.search_location:
                searchLocation();
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
            case R.id.create_site_button:
                if (validation.validate()) {
                    createSite();
                }
                break;
            case R.id.close_button:
                finish();
                break;
            case R.id.edit_market:
                showNotificationAccept();
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
            geocoder = new Geocoder(SiteCreateActivity.this, Locale.getDefault());
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
        if (requestCode == 2 && data != null) {
            Uri image = Uri.parse(data.getStringExtra("Image"));
            imageViewSiteImage.setImageURI(image);
            imageUrl = data.getStringExtra("imageUrl");
        }
    }

    private void createSite() {
        progress.setVisibility(View.VISIBLE);
        arrow.setVisibility(View.GONE);
        siteName = editTextSiteName.getText().toString().trim();
        latitude = String.valueOf(lat);
        longitude = String.valueOf(lang);
        if (siteName == null || latitude == null || longitude == null || imageUrl == null || displayType == null) {
            Snackbar snackbar = Snackbar
                    .make(linearLayoutCreateSiteButton, "ALL FIELDS ARE REQUIRED ", Snackbar.LENGTH_SHORT)
                    .setAction("CLOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
            snackbar.show();
            return;
        }
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put(SITE_NAME, siteName);
            requestObject.put(SITE_LATITUDE, latitude);
            requestObject.put(SITE_LONGITUDE, longitude);
            requestObject.put(SITE_DISPLAY_TYPE, displayType);
            requestObject.put(SITE_IMAGE_URL, imageUrl);
            requestObject.put(SITE_MARKET_ID, sharedPreferencesStatus.getString(MARKET_ID, "0"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = CONNECTION_URL_SITE_MANAGER + DATA_PATH_SITE_CREATE;
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        progress.setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);
                        parseJSONObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse.statusCode == 409) {
                            progress.setVisibility(View.GONE);
                            arrow.setVisibility(View.VISIBLE);
                            Toast.makeText(SiteCreateActivity.this, "Unknown Error.", Toast.LENGTH_SHORT).show();
                        } else {
                            progress.setVisibility(View.GONE);
                            arrow.setVisibility(View.VISIBLE);
                            Toast.makeText(SiteCreateActivity.this, "Network Error !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + sharedPreferencesStatus.getString(TOKEN, ""));
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
        finish();
    }

    public void showNotificationAccept() {
        LayoutInflater inflater = this.getLayoutInflater();
        View requestAccepted = inflater.inflate(R.layout.custom_popup_accept_reuqest, null);
        ImageView imageViewCloseButton = requestAccepted.findViewById(R.id.close_button);
      //  RecyclerView marketList = requestAccepted.findViewById(R.id.market_list);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(requestAccepted);
        alertDialog = alertDialogBuilder.create();
        imageViewCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
        if (markets.size() == 0 || markets == null) {
        } else {
          //  marketList.setLayoutManager(new LinearLayoutManager(this));
            MarketAdapter adapter = new MarketAdapter(SiteCreateActivity.this, markets, this);
          //  marketList.setAdapter(adapter);
        }
    }

    @Override
    public void marketSelect(Market market) {
        alertDialog.dismiss();
        textViewSiteMarket.setText(market.getName());
    }

    private void getMarketData() {
        markets = new ArrayList<>();
        String url = CONNECTION_URL_SITE_MANAGER + DATA_PATH_MARKET;
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        Log.v(TAG, url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        parseJsonObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-correlation-id", "true");
                headers.put("Authorization", "Bearer " + sharedPreferencesStatus.getString(TOKEN, ""));
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
                        if (response.statusCode == 409) {
                        } else {
                        }
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

    private void parseJsonObject(JSONObject response) {
        if (response == null || response.length() == 0) {
            return;
        } else {
            try {
                JSONArray jsonArray = response.getJSONArray("body");
                for (int i = 0; i < jsonArray.length(); i++) {
                    Market market = new Market();
                    JSONObject currentObj = jsonArray.getJSONObject(i);
                    market.setMarketId(currentObj.getString("id"));
                    market.setName(currentObj.getString("marketName"));
                    markets.add(market);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
