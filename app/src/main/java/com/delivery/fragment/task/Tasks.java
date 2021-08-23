package com.delivery.fragment.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.delivery.utils.jkeys.Keys;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.model.LatLng;
import com.delivery.R;
import com.delivery.fragment.task.adapter.TaskAdapter;
import com.delivery.fragment.task.entity.TaskEntity;
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
import com.android.volley.Request;
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
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.BRAND_LOGO_IMAGE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.BRAND_NAME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.CART;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DATA_PATH_ON_DEMAND_SUPPLY_ACCEPTED;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_AMOUNT;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_CUSTOMER_NAME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_CUSTOMER_PHONE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_PAYMENT_MODE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_REQUEST_ID;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_REQUEST_TIME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_SUPPLIER_LANG;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_SUPPLIER_LAT;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DELIVERY_SUPPLIER_NAME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DESCRIPTION;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_DUE_TIME;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_LATITUDE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.SITE_SHOT_LONGITUDE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.STATUS;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.STATUS_ID;

public class Tasks extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private int mParam1;
    private String mParam2;
    private RecyclerView recyclerViewTask;
    private List<TaskEntity> taskEntityList;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    private ShimmerFrameLayout spinner;
    private LinearLayout linearLayoutError;
    private TextView errorHeading, errorMessage;
    private ImageView errorImage;
    private View view;
    private OnFragmentInteractionListener mListener;

    public Tasks() {

    }

    public static Tasks newInstance(int param1) {
        Tasks fragment = new Tasks();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_task_manager, container, false);
        sharedPreferenceStatus = getActivity().getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        spinner = view.findViewById(R.id.shimmer_loading);
        linearLayoutError = view.findViewById(R.id.network_error_message);
        errorImage = view.findViewById(R.id.error_image);
        errorHeading = view.findViewById(R.id.error_heading);
        errorMessage = view.findViewById(R.id.details_message);
        getHistoryList(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getHistoryList(view);
    }

    private void getHistoryList(View view) {
        recyclerViewTask = view.findViewById(R.id.task_recycler_view);
        taskEntityList = new ArrayList<>();
        recyclerViewTask.setLayoutManager(new LinearLayoutManager(getActivity()));
        getData();
    }

    private void getData() {
        spinner.startShimmer();
        String userId = sharedPreferenceStatus.getString(USER_ID, "");
        int uId = (int) Float.parseFloat(userId);
        String url = CONNECTION_URL_DEMAND + DATA_PATH_ON_DEMAND_SUPPLY_ACCEPTED + uId ;
        Log.v("TASK URL", "" + url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        linearLayoutError.setVisibility(View.GONE);
                        spinner.stopShimmer();
                        spinner.setVisibility(View.GONE);
                        recyclerViewTask.setVisibility(View.VISIBLE);
                        if (taskEntityList != null) {
                            taskEntityList.clear();
                        }
                        parseJsonObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        linearLayoutError.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        recyclerViewTask.setVisibility(View.GONE);
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

    private void parseJsonObject(JSONObject response) {
        if (response == null || response.length() == 0) {
            return;
        } else {
            try {
                JSONArray jsonArray = response.getJSONArray("object");
                for (int i = 0; i < jsonArray.length(); i++) {
                    TaskEntity taskEntity = new TaskEntity();
                    JSONObject currentObj = jsonArray.getJSONObject(i);
                    if(currentObj.getJSONObject(CART).getJSONObject(STATUS)
                            .getString(DESCRIPTION).toUpperCase().equals("REFUND PICKUP")||currentObj.getJSONObject(CART).getJSONObject(STATUS)
                            .getString(DESCRIPTION).toUpperCase().equals("SHIPPED")) {
                        taskEntity.setRequestId(currentObj.getJSONObject(CART).getString(DELIVERY_REQUEST_ID));
                        taskEntity.setRequestAcceptedTime(currentObj.getString(DELIVERY_REQUEST_TIME));
                        taskEntity.setCustomer(String.valueOf(currentObj.getJSONObject(CART)
                                .getString(DELIVERY_CUSTOMER_NAME)));
                        taskEntity.setCustomerContact(String.valueOf(currentObj.getJSONObject(CART)
                                .getString(DELIVERY_CUSTOMER_PHONE)));
                        taskEntity.setAmount(String.valueOf(currentObj.getJSONObject(CART)
                                .getInt(DELIVERY_AMOUNT)));
                        taskEntity.setRefundedProductsAmount(String.valueOf(currentObj.getJSONObject(CART)
                                .getInt("refundedProductsAmount")));
                        taskEntity.setUnpaidAmount(String.valueOf(currentObj.getJSONObject(CART)
                                .getInt("unpaidAmount")));
                        taskEntity.setMode(String.valueOf(currentObj.getJSONObject(CART)
                                .getString(DELIVERY_PAYMENT_MODE)));
                        taskEntity.setStatus(String.valueOf(currentObj.getJSONObject(CART).getJSONObject(STATUS)
                                .getString(STATUS_ID)));
                        taskEntity.setDescription(String.valueOf(currentObj.getJSONObject(CART).getJSONObject(STATUS)
                                .getString(DESCRIPTION)));
                        taskEntity.setCustomerLatitude(String.valueOf(currentObj.getJSONObject(CART).getJSONArray("addresses").getJSONObject(0)
                                .getString(DELIVERY_SUPPLIER_LAT)));
                        taskEntity.setCustomerLongitude(String.valueOf(currentObj.getJSONObject(CART).getJSONArray("addresses").getJSONObject(0)
                                .getString(DELIVERY_SUPPLIER_LANG)));
                        taskEntity.setCartProductRequests(String.valueOf(currentObj.getJSONObject(CART).getJSONArray("cartProductRequests")));
                        taskEntity.setRefundedProducts(String.valueOf(currentObj.getJSONObject(CART).getJSONArray("refundedProducts")));
                        taskEntity.setLocation(String.valueOf(currentObj.getJSONObject(CART).getJSONArray("addresses").getJSONObject(0)
                                .getString("addressLine1")));

                        taskEntityList.add(taskEntity);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (taskEntityList.size() == 0) {
            linearLayoutError.setVisibility(View.VISIBLE);
            errorImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_empty_search));
            errorHeading.setText(getActivity().getText(R.string.stringEmptyLiveList));
            errorMessage.setText(getActivity().getText(R.string.stringEmptyLiveListMessage));
            recyclerViewTask.setVisibility(View.GONE);

        } else {
            if (getActivity()!=null){
                TaskAdapter adapter = new TaskAdapter(getActivity(), taskEntityList);
                recyclerViewTask.setAdapter(adapter);
            }

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
