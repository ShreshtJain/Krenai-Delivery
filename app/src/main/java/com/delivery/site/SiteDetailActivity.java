package com.delivery.site;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.delivery.fragment.task.adapter.TaskAdapter;
import com.delivery.fragment.task.adapter.cartProductsAdapter;
import com.delivery.fragment.task.entity.ProductsEntity;
import com.delivery.fragment.task.entity.TaskEntity;
import com.delivery.utils.jkeys.Keys;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.delivery.R;
import com.delivery.site.fragment.entity.SiteEntity;
import com.delivery.utils.database.DatabaseHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import static android.app.PendingIntent.getActivity;
import static com.delivery.utils.jkeys.Keys.CommonResources.DEFAULT_ZOOM;

public class SiteDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SiteDetailActivity.class.getSimpleName();
    private Button buttonSiteUpdateLocation;
    private ImageView imageViewClose, imageViewSiteEdit,expand,profile_image;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private TextView textViewName, textViewMode, textViewCost, updateProductButton,textViewAddress;
    private int toggle = 0;
    MapView mMapView;
    private GoogleMap googleMap;
    private RecyclerView productRecycler;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private Double lat;
    private Double lang;
    private List<ProductsEntity> productList;
    private JSONArray cartProductRequests;
    public JSONObject jsonObjectRefund;
    private DatabaseHelper databaseHelper;
    private String imageUrl, latitude, longitude, customerLatitude, customerLongitude;
    private String name, cartId, cost, mode, address,status,refund;
    private int state;
    private cartProductsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        initMap();
        getExtraData();
    }

    private void getExtraData() {
        Bundle extra = getIntent().getExtras();

//        intent.putExtra("site_name", taskEntity.getSupplier());
//        intent.putExtra("cart_id", taskEntity.getRequestId());
//        intent.putExtra("description", taskEntity.getLocation());
//        intent.putExtra("basePrice", taskEntity.getAmount());
//        intent.putExtra("customer", taskEntity.getMode());
//        intent.putExtra("customerPhone", taskEntity.getMode());
//        intent.putExtra("modeOfPayment", taskEntity.getMode());
//        intent.putExtra("supplier_lat", taskEntity.getMode());
//        intent.putExtra("supplier_lang", taskEntity.getMode());
//

        name = extra.getString("site_name");
        cartId = extra.getString("cart_id");
        cost = extra.getString("basePrice");
        refund=extra.getString("refundedProductsAmount");
        latitude = extra.getString("supplier_lat");
        longitude = extra.getString("supplier_lang");
        customerLatitude = extra.getString("customer_lat");
        customerLongitude = extra.getString("customer_lang");
        mode = extra.getString("modeOfPayment");
        address = extra.getString("description");
        imageUrl = extra.getString("image");
        state = Integer.parseInt(extra.getString("state"));
        status=extra.getString("status");
        textViewName = findViewById(R.id.site_name);
        textViewCost = findViewById(R.id.total_booking_amount);
        textViewAddress = findViewById(R.id.shop_address);
        textViewMode = findViewById(R.id.mode_of_payment);

        productRecycler=findViewById(R.id.cart_products_recycler);

        expand=findViewById(R.id.expand);
        expand.setOnClickListener(this);

        textViewAddress.setText(address);
        if(status.toUpperCase().equals("SHIPPED")) {
            textViewCost.setText("₹ " + cost);
        }
        else if(status.toUpperCase().equals("REFUND PICKUP"))
        {
            textViewCost.setText("₹ " + refund);
        }
        textViewName.setText(name);
        textViewMode.setText("MODE OF PAYMENT : " + String.valueOf(mode).toUpperCase());

        imageViewSiteEdit = findViewById(R.id.site_edit_button);
        imageViewSiteEdit.setOnClickListener(this);

        updateProductButton=findViewById(R.id.update_button);
        updateProductButton.setOnClickListener(this);

        imageViewClose = findViewById(R.id.close_button);
        imageViewClose.setOnClickListener(this);


//        if (!imageUrl.equals("")) {
//            Picasso.with(this).load(imageUrl).into(profile_image);
//        }
        if(status.toUpperCase().equals("SHIPPED")) {
            setUpDeliveryRecycler();
        }
        else if(status.toUpperCase().equals("REFUND PICKUP"))
        {
            setUpRefundRecycler();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.close_button:
                finish();
                break;
            case R.id.site_edit_button:
                showUpdateOption();
                break;
            case R.id.update_button:
                Intent i = new Intent(getApplication(), otpUpdateActivity.class);
                i.putExtra("json",adapter.jsonObject.toString());
                i.putExtra("status",status);
                Log.i("json",adapter.jsonObject.toString());
                startActivity(i);
            case R.id.expand:
                if(toggle==0)
                {
                    toggle =1;
                    expand.setBackgroundResource(R.drawable.expand_less);
                    productRecycler.setVisibility(View.GONE);
                }
                else{
                    toggle=0;
                    expand.setBackgroundResource(R.drawable.expand_more);
                    productRecycler.setVisibility(View.VISIBLE);

                }
        }
    }

    public void showUpdateOption() {
        final Intent[] intent = new Intent[1];
        LayoutInflater inflater = getLayoutInflater();
        View requestAccepted = inflater.inflate(R.layout.custom_popup_update_site, null);
        buttonSiteUpdateLocation = requestAccepted.findViewById(R.id.site_update_location);
        if(status.equals("Refund Pickup")){
        buttonSiteUpdateLocation.setText("START Refund Pickup");}
        else if(status.equals("Shipped")){
            buttonSiteUpdateLocation.setText("START Delivery Process");
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(requestAccepted);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        buttonSiteUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent[0] = new Intent(getApplication(), UpdateLocationActivity.class);
                intent[0].putExtra("site_id", cartId);
                intent[0].putExtra("lat", latitude);
                intent[0].putExtra("lang", longitude);
                intent[0].putExtra("customer_lat", customerLatitude);
                intent[0].putExtra("customer_lang", customerLongitude);
                intent[0].putExtra("state", String.valueOf(state));
                intent[0].putExtra("status", status);
                startActivity(intent[0]);
                finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

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
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Setting mode
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyD_L8g3AcwXBKnEjhvLJwBXwI3L51LjQUU";

        return url;
    }
    private void getDeviceLocation() {
            if(customerLatitude !=null && customerLongitude!=null)
            {

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(Double.parseDouble(customerLatitude),
                                Double.parseDouble(customerLongitude))).zoom(DEFAULT_ZOOM).tilt(60).build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

            }
    }

    private void  setUpDeliveryRecycler()
    {
        productRecycler.setLayoutManager(new LinearLayoutManager((this)));
        productList=new ArrayList<>();
        try {
            cartProductRequests = new JSONArray( getIntent().getExtras().getString("cartProductRequests"));
            Log.v("cart",String.valueOf(cartProductRequests.length()));
            for(int i=0;i<cartProductRequests.length();i++)
            {
               ProductsEntity productsEntity = new ProductsEntity();
                JSONObject currentObj = cartProductRequests.getJSONObject(i);
               productsEntity.setAmount(currentObj.getString("sellingPrice"));
                productsEntity.setProductQuantity(currentObj.getString("quantity"));
                productsEntity.setProductId(currentObj.getInt("id"));
                productsEntity.setProductListingId(currentObj.getInt("productListingId"));
               JSONObject product=currentObj.getJSONObject("productListing").getJSONObject("product");

               productsEntity.setSkuCode(currentObj.getJSONObject("productListing").getString("skuCode"));

               productsEntity.setProductName(product.getString("name"));
                productsEntity.setBrandName(product.getJSONObject("brand").getString("name"));
                if(currentObj.getJSONArray("medias").length()>0)
                    productsEntity.setImageUrl(currentObj.getJSONArray("medias").getJSONObject(0).getString("mediaUrl"));
                else
                    productsEntity.setImageUrl(null);
                Log.i("productsEntity",productsEntity.toString());
               productList.add(productsEntity);
            }

        }
        catch(JSONException ex) {
            System.out.println("Error: " + ex);
        }
         adapter = new cartProductsAdapter(SiteDetailActivity.this, productList,Integer.parseInt(cartId),status);
        productRecycler.setAdapter(adapter);
    }
    private void setUpRefundRecycler()
    {
        productRecycler=findViewById(R.id.cart_products_recycler);
        productRecycler.setLayoutManager(new LinearLayoutManager((this)));
        productList=new ArrayList<>();
        try {
            cartProductRequests = new JSONArray( getIntent().getExtras().getString("refundedProducts"));
            Log.i("cart",cartProductRequests.toString());
            for(int i=0;i<cartProductRequests.length();i++)
            {
                ProductsEntity productsEntity = new ProductsEntity();
                JSONObject currentObj = cartProductRequests.getJSONObject(i);
                productsEntity.setAmount(currentObj.getString("sellingPrice"));
                productsEntity.setProductName(currentObj.getString("productName"));
                productsEntity.setBrandName(currentObj.getString("brandName"));
                productsEntity.setImageUrl(currentObj.getString("productImage"));
                productsEntity.setProductQuantity(currentObj.getString("quantity"));
                productsEntity.setSkuCode(currentObj.getString("skuCode"));
                productsEntity.setProductId(currentObj.getInt("id"));
                productsEntity.setProductListingId(currentObj.getInt("productListingId"));
                Log.i("productsEntity",productsEntity.toString());
                productList.add(productsEntity);
            }

        }
        catch(JSONException ex) {
            System.out.println("Error: " + ex);
        }
         adapter = new cartProductsAdapter(SiteDetailActivity.this, productList,Integer.parseInt(cartId),status);
        productRecycler.setAdapter(adapter);
    }


}
