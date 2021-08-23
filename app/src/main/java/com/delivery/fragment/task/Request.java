package com.delivery.fragment.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.model.LatLng;
import com.delivery.R;
import com.delivery.fragment.task.adapter.RequestAdapter;
import com.delivery.fragment.task.entity.RequestEntity;
import com.delivery.utils.network.VolleySingleton;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_DEMAND;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.BRAND_NAME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.CART;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DATA_PATH_ON_DEMAND_SUPPLY_ALLOTED;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_AMOUNT;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_CART_ID;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_PAYMENT_MODE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_REQUEST_ID;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_REQUEST_TIME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.MEDIA;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.MEDIA_IMAGE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_DUE_TIME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_LATITUDE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_LONGITUDE;

public class Request extends Fragment {
    private static final String TAG = Request.class.getSimpleName();
    private final LatLng mDefaultLocation = new LatLng(28.495982, 77.1779033);
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private int mParam1;
    private String mParam2;
    private View view;

    private RecyclerView recyclerViewRequest;
    private List<RequestEntity> requestList;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    private ShimmerFrameLayout spinner;
    private LinearLayout linearLayoutError;
    private TextView errorHeading, errorMessage;
    private ImageView errorImage;


    public Request() {
    }

    public static Request newInstance(int param1) {
        Request fragment = new Request();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_request_based, container, false);
        sharedPreferenceStatus = getActivity().getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        spinner = view.findViewById(R.id.shimmer_loading);
        linearLayoutError = view.findViewById(R.id.network_error_message);
        errorImage = view.findViewById(R.id.error_image);
        errorHeading = view.findViewById(R.id.error_heading);
        errorMessage = view.findViewById(R.id.details_message);
        getHistoryList(view);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void getHistoryList(View view) {

        recyclerViewRequest = view.findViewById(R.id.request_recycler_view);
        requestList = new ArrayList<>();
        recyclerViewRequest.setLayoutManager(new LinearLayoutManager(getActivity()));
        getData();
    }

    private void getData() {
        spinner.startShimmer();
        String userId = sharedPreferenceStatus.getString(USER_ID, "");
        int uId = (int) Float.parseFloat(userId);
        String url = CONNECTION_URL_DEMAND + DATA_PATH_ON_DEMAND_SUPPLY_ALLOTED + uId + "?status=ALLOTED";
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        linearLayoutError.setVisibility(View.GONE);
                        spinner.stopShimmer();
                        spinner.setVisibility(View.GONE);
                        recyclerViewRequest.setVisibility(View.VISIBLE);
                        parseJsonObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        linearLayoutError.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        recyclerViewRequest.setVisibility(View.GONE);
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
                        Log.d(TAG, "" + response.statusCode);

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
                JSONArray jsonArray = response.getJSONArray("object");
                for (int i = 0; i < jsonArray.length(); i++) {
                    System.out.println("" + jsonArray);
                    RequestEntity requestEntity = new RequestEntity();
                    JSONObject currentObj = jsonArray.getJSONObject(i);

                    requestEntity.setRequestId(currentObj.getString(DELIVERY_REQUEST_ID));
                    requestEntity.setCartId(currentObj.getJSONObject(CART)
                            .getString(DELIVERY_CART_ID));

                    requestEntity.setRequestAcceptedTime(currentObj.getString(DELIVERY_REQUEST_TIME));

                    requestEntity.setBrandName(currentObj.getJSONObject(CART)
                            .getString(DELIVERY_PAYMENT_MODE));

                    requestEntity.setLatitude(String.valueOf(currentObj.getJSONObject(CART)
                            .getString(DELIVERY_AMOUNT)));

                    requestEntity.setImageUrl("https://image.flaticon.com/icons/svg/2332/2332792.svg");

                    requestEntity.setLocation(String.valueOf(currentObj.getJSONObject(CART).getJSONArray("addresses").getJSONObject(0)
                            .getString("addressLine1")));

                    requestList.add(requestEntity);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (requestList.size() == 0 || requestList == null) {
            linearLayoutError.setVisibility(View.VISIBLE);
            errorImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_empty_search));
            errorHeading.setText(getActivity().getText(R.string.stringEmptyLiveList));
            errorMessage.setText(getActivity().getText(R.string.stringEmptyLiveListMessage));
            recyclerViewRequest.setVisibility(View.GONE);
        } else {
            RequestAdapter adapter = new RequestAdapter(getActivity(), requestList);
            recyclerViewRequest.setAdapter(adapter);
        }
    }
}
