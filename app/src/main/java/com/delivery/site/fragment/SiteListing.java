package com.delivery.site.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.delivery.R;
import com.delivery.fragment.TaskFragment;
import com.delivery.site.SiteCreateActivity;
import com.delivery.site.fragment.adapter.MarketAdapter;
import com.delivery.site.fragment.entity.Market;
import com.delivery.site.fragment.listing.AllSites;
import com.delivery.site.fragment.listing.Draft;
import com.delivery.site.fragment.listing.Favourites;
import com.delivery.site.fragment.listing.Hold;
import com.delivery.site.fragment.listing.adapter.SiteManagerFragmentPagerAdapter;
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
import static com.delivery.utils.jkeys.Keys.SiteManager.DATA_PATH_MARKET;
import static com.delivery.utils.jkeys.Keys.SiteManager.MARKET_ID;
import static com.delivery.utils.jkeys.Keys.SiteManager.MARKET_NAME;

public class SiteListing extends Fragment implements View.OnClickListener, MarketAdapter.AdapterCallback {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;

    private TabLayout tabLayout;
    private TaskFragment.OnFragmentInteractionListener mListener;
    private List<Market> markets;
    ViewPager viewPager;
    private ImageView imageViewCreateSite;
    private LinearLayout linearLayoutSelectMarket;
    private BottomSheetBehavior sheetBehavior;
    private LinearLayout bottom_sheet;
    private FloatingActionButton buttonFilter;
    private AlertDialog alertDialog;
    private SiteManagerFragmentPagerAdapter adapter;
    private String mParam1;
    private String mParam2;
    private TextView address;

    public SiteListing() {
    }

    public static SiteListing newInstance(String param1, String param2) {
        SiteListing fragment = new SiteListing();
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
        View view = inflater.inflate(R.layout.fragment_site_listing, container, false);
        setTopBarNavigation(view);
        sharedPreferenceStatus = getActivity().getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        imageViewCreateSite = view.findViewById(R.id.create_new_site);
        imageViewCreateSite.setOnClickListener(this);
        address = view.findViewById(R.id.address);
        getMarketData();
        bottom_sheet = view.findViewById(R.id.bottom_sheet);
        linearLayoutSelectMarket = view.findViewById(R.id.select_market);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        buttonFilter = view.findViewById(R.id.filter_btn);
        linearLayoutSelectMarket.setOnClickListener(this);
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        buttonFilter.setVisibility(View.GONE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        buttonFilter.setVisibility(View.VISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
        return view;
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    Market market = new Market();
                    JSONObject currentObj = jsonArray.getJSONObject(i);
                    market.setMarketId(currentObj.getString("id"));
                    market.setName(currentObj.getString("marketName"));
                    markets.add(market);
                }
                address.setText(markets.get(0).getName());
                SharedPreferences.Editor editor = sharedPreferenceStatus.edit();
                editor.putString(MARKET_NAME, markets.get(0).getName());
                editor.putString(MARKET_ID, markets.get(0).getMarketId());
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setTopBarNavigation(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        adapter = new SiteManagerFragmentPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new Favourites(), "BOOKMARK");
        adapter.addFragment(new Draft(), "DRAFT");
        adapter.addFragment(new Hold(), "HOLD");
        adapter.addFragment(new AllSites(), "LIVE");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.create_new_site:
                intent = new Intent(getActivity(), SiteCreateActivity.class);
                startActivity(intent);
                break;
            case R.id.select_market:
                showNotificationAccept();
                break;
        }
    }

    public void showNotificationAccept() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View requestAccepted = inflater.inflate(R.layout.custom_popup_accept_reuqest, null);
        ImageView imageViewCloseButton = requestAccepted.findViewById(R.id.close_button);
        //RecyclerView marketList = requestAccepted.findViewById(R.id.market_list);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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
          //  marketList.setLayoutManager(new LinearLayoutManager(getActivity()));
            MarketAdapter adapter = new MarketAdapter(getActivity(), markets, this);
            //marketList.setAdapter(adapter);
        }
    }

    @Override
    public void marketSelect(Market market) {
        alertDialog.dismiss();
        SharedPreferences.Editor editor = sharedPreferenceStatus.edit();
        editor.putString(MARKET_NAME, market.getName());
        editor.putString(MARKET_ID, market.getMarketId());
        editor.apply();
        address.setText(market.getName());
    }
}
