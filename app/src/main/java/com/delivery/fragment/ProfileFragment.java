package com.delivery.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.delivery.R;
import com.delivery.activity.authentication.DocumentUploadActivity;
import com.delivery.activity.authentication.LaunchActivity;
import com.delivery.activity.authentication.OneTimePasswordActivity;
import com.delivery.service.LocationUpdateService;
import com.delivery.site.otpUpdateActivity;
import com.delivery.utils.network.VolleySingleton;
import com.squareup.picasso.Picasso;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.LoginKeys.STATUS;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;
import static com.delivery.utils.jkeys.Keys.Setting.DATA_PATH_USER_DETAILS;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = ProfileFragment.class.getSimpleName();

    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private String mParam1;
    private String mParam2;
    private TextView textViewUserName, textViewAddressHeading,
            textViewPhoneNumber, textViewAddress, textViewName;
    private ImageView imageViewLogout, imageViewProfilePicture;
    private String mobile;
    private OnFragmentInteractionListener mListener;
    public ProfileFragment() {

    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        sharedPreferencesStatus = getActivity().getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        imageViewLogout = view.findViewById(R.id.logout);
        imageViewProfilePicture = view.findViewById(R.id.profile_image_user);
        textViewUserName = view.findViewById(R.id.user_name);
        textViewAddressHeading = view.findViewById(R.id.default_address);
        textViewAddress = view.findViewById(R.id.user_address);
        textViewPhoneNumber = view.findViewById(R.id.phone_number);
        textViewName = view.findViewById(R.id.login_as);

        imageViewProfilePicture = view.findViewById(R.id.edit_profile);
        getProfile();
        imageViewLogout.setOnClickListener(this);
        imageViewProfilePicture.setOnClickListener(this);
        return view;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferencesStatus.edit();
        editor.clear();
        editor.putInt(STATUS, 0);
        editor.apply();
        Intent intent = new Intent(getActivity(), LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        stopLocationService();
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
        Intent intent;
        switch (view.getId()) {
            case R.id.logout:
                logout();
                break;
            case R.id.edit_profile:
                intent = new Intent(getActivity(), DocumentUploadActivity.class);
                intent.putExtra("phone", mobile);
                startActivity(intent);
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        getProfile();
    }

    public void getProfile() {

        String userId = sharedPreferencesStatus.getString(USER_ID, "");
        int uId = (int) Float.parseFloat(userId);

        String url = CONNECTION_URL + DATA_PATH_USER_DETAILS + uId;
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        System.out.println("" + jsonObject);
                        parseJsonObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse.statusCode == 409) {

                        } else {
                            Toast.makeText(getActivity(), "Network Error !", Toast.LENGTH_SHORT).show();
                        }
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
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject currentObject = response.getJSONArray("object").getJSONObject(0);
                Log.i("object",currentObject.toString());
                JSONObject supplier=currentObject.getJSONObject("supplier");
                Log.i("supplier",supplier.toString());
                JSONArray storeList=supplier.getJSONArray("storeList");
                Log.i("storeList",storeList.toString());

                String addressLine1;
                String addressLine2;
                String city;
                String state;
                String pinCode;

                if (storeList.getJSONObject(0).getString("addressLine1").equals( "null")) {
                    addressLine1 = "";
                } else {
                    addressLine1 = storeList.getJSONObject(0).getString("addressLine1")+",";
                    Log.i("addresLine1",addressLine1);
                }

                if (storeList.getJSONObject(0).getString("addressLine2").equals( "null")||storeList.getJSONObject(0).getString("addressLine2").equals( "null")) {
                    addressLine2 = "";
                } else {
                    addressLine2 = storeList.getJSONObject(0).getString("addressLine2")+",";
                    Log.i("addresLine2",addressLine2);
                }

                if (storeList.getJSONObject(0).getString("city").equals( "null")) {
                    city = "";
                } else {
                    city = storeList.getJSONObject(0).getString("city")+",";
                    Log.i("city",city);
                }

                if (storeList.getJSONObject(0).getString("addressState").equals( "null")) {
                    state = "";
                } else {
                    state = storeList.getJSONObject(0).getString("addressState")+",";
                    Log.i("state",state);
                }

                if (storeList.getJSONObject(0).getString("pincode").equals( "null")) {
                    pinCode = "";
                } else {
                    pinCode = storeList.getJSONObject(0).getString("pincode");
                    Log.i("pinCode",pinCode);
                }
                String address = addressLine1  + addressLine2  + city  + state  + pinCode;
                Log.i("address",address);
                textViewUserName.setText(supplier.getString("firstName"));
                textViewAddressHeading.setText(address);
                textViewPhoneNumber.setText(supplier.getString("phoneNo"));
                mobile = supplier.getString("phoneNo");
                textViewAddress.setText(address);
                textViewName.setText("Signed in as " + supplier.getString("firstName"));
//                String imageUrl = currentObject.getString("photoImageUrl");
//                if (!imageUrl.equals("")) {
//                    Picasso.with(getActivity()).load(imageUrl).into(imageViewProfilePicture);
//                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationService() {
        Intent intent = new Intent(getActivity(), LocationUpdateService.class);
        getActivity().stopService(intent);
    }

}
