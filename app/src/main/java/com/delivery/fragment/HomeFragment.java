package com.delivery.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.delivery.R;
import com.delivery.activity.media.MediaActivity;
import com.delivery.fragment.adapter.NearbySiteAdapter;
import com.delivery.fragment.entity.NearbySiteEntity;
import com.delivery.utils.jkeys.Keys;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.ui.IconGenerator;
import com.delivery.utils.network.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.delivery.utils.jkeys.Keys.CommonResources.AUTOCOMPLETE_REQUEST_CODE;
import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_CAMPAIGN;
import static com.delivery.utils.jkeys.Keys.CommonResources.DEFAULT_ZOOM;
import static com.delivery.utils.jkeys.Keys.CommonResources.PLACES_API_KEY;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LONGITUDE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.BRAND_LOGO_IMAGE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.BRAND_NAME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_DATA_LIST_NAME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_LATITUDE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_LONGITUDE;

public class HomeFragment extends Fragment implements View.OnClickListener, GoogleMap.OnMarkerClickListener {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = HomeFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap googleMap;
    MapView mMapView;
    String address;
    private View view;
    private LatLng mDefaultLocation = new LatLng(28.495982, 77.1779033);
    private Double lat;
    private Double lang;
    private OnFragmentInteractionListener mListener;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private LinearLayout linearLayoutAddressHolder, linearLayoutNearbySiteList;
    private FrameLayout frameLayoutMapHolder;
    private ImageView siteShoListingHolder;
    private TextView addressTextView, textViewViewRequest,
            textViewViewTask, textViewBack, textViewAlertMessage;
    private ImageView imageViewMyLocation, imageViewChangeMapType;
    private LocationManager mLocationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private Fragment currentFragment = null;
    private FragmentTransaction ft;
    private int mapType = 0;
    private RecyclerView recyclerViewNearBySites;
    private List<NearbySiteEntity> nearbySiteEntities;

    public HomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_site_shot, container, false);
        sharedPreferencesStatus = getActivity().getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        Places.initialize(getActivity().getApplicationContext(), PLACES_API_KEY);
        getReferenceId(view);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        initMap();
        return view;
    }

    private void getReferenceId(View view) {
        siteShoListingHolder = view.findViewById(R.id.site_shot_listing_holder);
        imageViewMyLocation = view.findViewById(R.id.my_location);
        addressTextView = view.findViewById(R.id.address);
        linearLayoutAddressHolder = view.findViewById(R.id.address_holder);
        textViewViewRequest = view.findViewById(R.id.view_requests);
        textViewViewTask = view.findViewById(R.id.view_task);
        imageViewChangeMapType = view.findViewById(R.id.change_map_type);
        linearLayoutNearbySiteList = view.findViewById(R.id.list_holder);
        frameLayoutMapHolder = view.findViewById(R.id.maps_holder);
        textViewBack = view.findViewById(R.id.back_button);
        textViewAlertMessage = view.findViewById(R.id.message_text);
        textViewBack.setOnClickListener(this);
        textViewViewTask.setOnClickListener(this);
        textViewViewRequest.setOnClickListener(this);
        imageViewMyLocation.setOnClickListener(this);
        siteShoListingHolder.setOnClickListener(this);
        linearLayoutAddressHolder.setOnClickListener(this);
        imageViewChangeMapType.setOnClickListener(this);
    }

    private void initMap() {
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
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
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
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
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
                                new GetLocationAsync(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())
                                        .execute();
                                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                    @Override
                                    public void onCameraChange(CameraPosition cameraPosition) {
                                        lat = googleMap.getCameraPosition().target.latitude;
                                        lang = googleMap.getCameraPosition().target.longitude;
                                        googleMap.clear();
                                        getNearBySites();
                                        new GetLocationAsync(lat, lang)
                                                .execute();
                                    }
                                });

                            } else {
                                isLocationServicesEnabled();
                            }

                        } else {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
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
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
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
            addressTextView.setText(address);
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        CameraPosition cameraPosition;
        Intent intent;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        switch (view.getId()) {
            case R.id.site_shot_listing_holder:
                linearLayoutNearbySiteList.setVisibility(View.VISIBLE);
                frameLayoutMapHolder.setVisibility(View.GONE);
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
            case R.id.address_holder:
                searchLocation();
                break;
            case R.id.view_requests:
                currentFragment = new TaskFragment();
                ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_layout, currentFragment);
                ft.commit();
                break;
            case R.id.view_task:
                currentFragment = new TaskFragment();
                ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_layout, currentFragment);
                ft.commit();
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
            case R.id.back_button:
                linearLayoutNearbySiteList.setVisibility(View.GONE);
                frameLayoutMapHolder.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void searchLocation() {
        int AUTOCOMPLETE_REQUEST_CODE = 1;
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(getActivity());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                addressTextView.setText(place.getAddress());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(place.getLatLng()).zoom(DEFAULT_ZOOM).tilt(60).build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int indexValue = -1;
        Double Latitude = marker.getPosition().latitude;

        for (int i = 0; i < nearbySiteEntities.size(); i++) {
            NearbySiteEntity clickMarker = nearbySiteEntities.get(i);
            double mLat = Double.parseDouble(clickMarker.getLatitude());
            if (mLat == Latitude) {
                indexValue = i;
            }
        }
        if (indexValue > 0) {
            NearbySiteEntity clickedSite = nearbySiteEntities.get(indexValue);
            showNotificationAccept(clickedSite);
        }
        return true;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public boolean isLocationServicesEnabled() {
        try {
            mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                showSettingsAlert();
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void showSettingsAlert() {
        if (this == null || getActivity().isFinishing()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        getActivity());
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
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                int which) {
                                Toast.makeText(getActivity(), "App need your location", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        });
                alertDialog.create().show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void setMarkers(String mLat, String mLang, int category) {
        double mLatitude = Double.parseDouble(mLat);
        double mLongitude = Double.parseDouble(mLang);

        IconGenerator iconFactory = new IconGenerator(getActivity());
        iconFactory.setRotation(0);
        iconFactory.setContentRotation(90);
        iconFactory.setStyle(IconGenerator.STYLE_DEFAULT);
        addIcon(iconFactory, category, new LatLng(mLatitude, mLongitude));
    }

    private void addIcon(IconGenerator iconFactory, int cat, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(cat, ""))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        Marker marker = googleMap.addMarker(markerOptions);
        googleMap.setOnMarkerClickListener(this);
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    private Bitmap writeTextOnDrawable(int drawableId, String text) {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getActivity(), 11));
        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);
        Canvas canvas = new Canvas(bm);
        if (textRect.width() >= (canvas.getWidth() - 4))
            paint.setTextSize(convertToPixels(getActivity(), 7));
        int xPos = (canvas.getWidth() / 2) - 2;
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(text, xPos, yPos, paint);
        return bm;
    }

    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;
        return (int) ((nDP * conversionScale) + 0.5f);
    }

    public void showNotificationAccept(final NearbySiteEntity nearbySiteEntity) {
        LayoutInflater inflater = getLayoutInflater();
        View requestAccepted = inflater.inflate(R.layout.custom_popup_nearby_reuqest, null);
        TextView textViewLocation = requestAccepted.findViewById(R.id.site_shot_location);
        TextView textViewDistance = requestAccepted.findViewById(R.id.distance);
        ImageView imageViewBrand = requestAccepted.findViewById(R.id.brand_image);
        LinearLayout imageViewTakePicture = requestAccepted.findViewById(R.id.take_picture_button);
        textViewLocation.setText(nearbySiteEntity.getLocation());
        String imageUrl = nearbySiteEntity.getImageUrl();
        if (!imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(imageViewBrand);
        }
        textViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.streetview:cbll=" + nearbySiteEntity.getLatitude() + "," + nearbySiteEntity.getLongitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                getActivity().startActivity(mapIntent);
            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(requestAccepted);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        imageViewTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                startActivity(intent);
                alertDialog.hide();
            }
        });
        alertDialog.show();
    }
    //Get Nearby Site Data

    private void getNearBySites() {
        recyclerViewNearBySites = view.findViewById(R.id.nearby_site);
        nearbySiteEntities = new ArrayList<>();
        recyclerViewNearBySites.setLayoutManager(new LinearLayoutManager(getActivity()));
        getData();

    }

    private void getData() {
        googleMap.clear();
        Uri.Builder builder = Uri.parse(CONNECTION_URL_CAMPAIGN).buildUpon();
        builder.appendQueryParameter(LOCATION_UPDATE_LATITUDE, String.valueOf(lat));
        builder.appendQueryParameter(LOCATION_UPDATE_LONGITUDE, String.valueOf(lang));
        String url = builder.build().toString();
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        recyclerViewNearBySites.setVisibility(View.VISIBLE);
                        parseJsonObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        recyclerViewNearBySites.setVisibility(View.GONE);
                        textViewAlertMessage.setVisibility(View.VISIBLE);
                        textViewAlertMessage.setText("NETWORK ERROR! PLEASE CHECK YOUR INTERNET CONNECTION ");
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
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    private void parseJsonObject(JSONObject response) {
        if (response == null || response.length() == 0) {
            recyclerViewNearBySites.setVisibility(View.GONE);
            textViewAlertMessage.setVisibility(View.VISIBLE);
            return;
        } else {
            try {
                JSONArray jsonArray = response.getJSONArray(SITE_SHOT_DATA_LIST_NAME);
                for (int i = 0; i < jsonArray.length(); i++) {
                    NearbySiteEntity nearbySiteEntity = new NearbySiteEntity();
                    JSONObject currentObj = jsonArray.getJSONObject(i);
                    nearbySiteEntity.setLatitude(String.valueOf(currentObj
                            .getString(SITE_SHOT_LATITUDE)));
                    nearbySiteEntity.setLongitude(String.valueOf(currentObj
                            .getString(SITE_SHOT_LONGITUDE)));
                    nearbySiteEntity.setLocation("Chhattarpur Extension Road, Ram Colony, Block G, Chhatarpur, New Delhi, Delhi");
                    setMarkers(nearbySiteEntity.getLatitude(), nearbySiteEntity.getLongitude(), R.drawable.billboard);
                    nearbySiteEntities.add(nearbySiteEntity);
                    Log.d(TAG, nearbySiteEntity.toString());
                }
            } catch (JSONException e) {
                recyclerViewNearBySites.setVisibility(View.GONE);
                textViewAlertMessage.setVisibility(View.VISIBLE);
                textViewAlertMessage.setText("SERVER ERROR! PLEASE CHECK YOUR INTERNET CONNECTION ");
                e.printStackTrace();
            }
        }
        if (nearbySiteEntities.size() == 0 || nearbySiteEntities == null) {
            recyclerViewNearBySites.setVisibility(View.GONE);
            textViewAlertMessage.setVisibility(View.VISIBLE);
        } else {
            textViewAlertMessage.setVisibility(View.GONE);
            NearbySiteAdapter adapter = new NearbySiteAdapter(getActivity(), nearbySiteEntities);
            recyclerViewNearBySites.setAdapter(adapter);
        }
    }
}
