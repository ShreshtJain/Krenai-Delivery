package com.delivery.site.fragment.listing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.delivery.R;
import com.delivery.site.fragment.adapter.DraftAdapter;
import com.delivery.site.fragment.entity.SiteEntity;
import com.delivery.utils.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;
import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_SITE_MANAGER;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.SiteManager.DATA_PATH_SITE_MANAGER;
import static com.delivery.utils.jkeys.Keys.SiteManager.MARKET_ID;

public class Draft extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private View view;

    // private FloatingActionButton buttonFilter;

    private RecyclerView recyclerViewDraftSites;
    private List<SiteEntity> siteEntityList;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    private ShimmerFrameLayout spinner;
    private LinearLayout linearLayoutError;
    private TextView errorHeading, errorMessage;
    private ImageView errorImage;

    public Draft() {

    }

    public static Draft newInstance(String param1, String param2) {
        Draft fragment = new Draft();
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
        view = inflater.inflate(R.layout.fragment_draft, container, false);

        sharedPreferenceStatus = getActivity().getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        spinner = view.findViewById(R.id.shimmer_loading);
        linearLayoutError = view.findViewById(R.id.network_error_message);
        errorImage = view.findViewById(R.id.error_image);
        errorHeading = view.findViewById(R.id.error_heading);
        errorMessage = view.findViewById(R.id.details_message);
        getDraftSiteList(view);

        return view;
    }

    private void getDraftSiteList(View view) {
        recyclerViewDraftSites = view.findViewById(R.id.site_manager_draft_recycler_view);
        recyclerViewDraftSites.setNestedScrollingEnabled(false);
        siteEntityList = new ArrayList<>();
        recyclerViewDraftSites.setLayoutManager(new LinearLayoutManager(getActivity()));
        getData();
    }

    private void getData() {
        if (siteEntityList != null) {
            siteEntityList.clear();
        }
        spinner.startShimmer();
        String url = CONNECTION_URL_SITE_MANAGER + DATA_PATH_SITE_MANAGER + sharedPreferenceStatus.getString(MARKET_ID, "0") + "/draft";
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        Log.v(TAG, url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        linearLayoutError.setVisibility(View.GONE);
                        spinner.stopShimmer();
                        spinner.setVisibility(View.GONE);
                        recyclerViewDraftSites.setVisibility(View.VISIBLE);
                        parseJsonObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        linearLayoutError.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        recyclerViewDraftSites.setVisibility(View.GONE);
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
                for (int i = 0; i <= jsonArray.length(); i++) {
                    SiteEntity siteEntity = new SiteEntity();
                    JSONObject currentObj = jsonArray.getJSONObject(i).getJSONObject("site");
                    siteEntity.setSiteId(currentObj.getString("id"));
                    siteEntity.setName(currentObj.getString("name"));
                    siteEntity.setDescription(currentObj.getString("description"));
                    siteEntity.setPhotosUrl(currentObj.getString("photosUrl"));
                    siteEntity.setSiteDisplayType(currentObj.getString("siteDisplayType"));
                    siteEntity.setSiteLocationType(currentObj.getString("siteLocationType"));
                    siteEntity.setSiteIlluminationType(currentObj.getString("siteIlluminationType"));
                    siteEntity.setSiteMediaType(currentObj.getJSONObject("siteMediaType").getString("siteMediaType"));
                    siteEntity.setSiteType(currentObj.getJSONObject("siteType").getString("siteType"));
                    siteEntity.setBasePrice(currentObj.getString("basePrice"));
                    siteEntity.setStatusType("draft");
                    siteEntityList.add(siteEntity);
                    Log.d("DRAFT DATA ", currentObj.toString());

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (siteEntityList.size() == 0 || siteEntityList == null) {
            linearLayoutError.setVisibility(View.VISIBLE);
            errorImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_empty_search));
            errorHeading.setText(getActivity().getText(R.string.stringDraftLiveList));
            errorMessage.setText(getActivity().getText(R.string.stringEmptyDraftListMessage));
            recyclerViewDraftSites.setVisibility(View.GONE);
        } else {
            DraftAdapter adapter = new DraftAdapter(getActivity(), siteEntityList);
            recyclerViewDraftSites.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.address_holder:
                showNotificationAccept();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (siteEntityList.size() > 0) {
            siteEntityList.clear();
            getDraftSiteList(view);
        }
    }

    public void showNotificationAccept() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View requestAccepted = inflater.inflate(R.layout.custom_popup_accept_reuqest, null);
//        ImageView imageViewClose = requestAccepted.findViewById(R.id.logout);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(requestAccepted);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
