package com.delivery.fragment.task.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.delivery.fragment.task.Complete;
import com.delivery.fragment.task.entity.RequestEntity;
import com.delivery.utils.network.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_DEMAND;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LONGITUDE;
import static com.delivery.utils.jkeys.Keys.SiteShotRequest.DATA_PATH_ON_DEMAND_SUPPLY_STATUS_UPDATE;

/**
 * Created by Pushpendra on 16-09-2019.
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.PendingRequestViewHolder> {

    private Context context;
    private List<RequestEntity> data;
    private LayoutInflater inflater;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    private int uId;

    public RequestAdapter(Context context, List<RequestEntity> data) {
        this.context = context;
        this.data = data;
        sharedPreferenceStatus = context.getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        String userId = sharedPreferenceStatus.getString(USER_ID, "");
        uId = (int) Float.parseFloat(userId);
    }

    @NonNull
    @Override
    public PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_request_pending, parent, false);
        PendingRequestViewHolder holder = new PendingRequestViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PendingRequestViewHolder holder, int position) {
        RequestEntity current = data.get(position);
        holder.textViewSiteShotLocation.setText(current.getLocation());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(current.getRequestAcceptedTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.textViewRemainingFulfillRequest.setText(date.toString());
        holder.textViewDistance.setText(current.getBrandName() + " : " + current.getLatitude());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void delete(int feedPosition) {
        data.remove(feedPosition);
        notifyItemRemoved(feedPosition);
    }

    class PendingRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewDistance,
                textViewRemainingFulfillRequest, textViewTimer;
        private TextView textViewSiteShotLocation;
        private ImageView imageViewBrandImage;
        private LinearLayout linearLayoutAccept,
                linearLayoutReject;

        public PendingRequestViewHolder(View itemView) {
            super(itemView);
            //textViewSiteShotRequestId = itemView.findViewById(R.id.site_shot_id);
            textViewSiteShotLocation = itemView.findViewById(R.id.site_shot_location);
            textViewDistance = itemView.findViewById(R.id.distance);
            textViewRemainingFulfillRequest = itemView.findViewById(R.id.remaining_to_fulfill_request);
            textViewTimer = itemView.findViewById(R.id.timer);
            linearLayoutAccept = itemView.findViewById(R.id.accept_button);
            linearLayoutReject = itemView.findViewById(R.id.reject_button);
            imageViewBrandImage = itemView.findViewById(R.id.brand_image);
            linearLayoutAccept.setOnClickListener(this);
            linearLayoutReject.setOnClickListener(this);
            textViewSiteShotLocation.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.accept_button:
                    showNotificationAccept(getPosition(), data.get(getPosition()));
                    break;
                case R.id.reject_button:
                    showNotificationReject(getPosition(), data.get(getPosition()));
                    break;
                case R.id.site_shot_location:
                    RequestEntity requestEntity = data.get(getPosition());
                    Uri gmmIntentUri = Uri.parse("google.streetview:cbll=" + requestEntity.getLatitude() + "," + requestEntity.getLongitude());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);
                    break;
            }
        }
    }

    public void showNotificationAccept(final int position, final RequestEntity entity) {
        Activity activity = (Activity) context;
        LayoutInflater inflater = activity.getLayoutInflater();
        View requestAccepted = inflater.inflate(R.layout.custom_popup_accept_reuqest, null);
        ImageView imageViewSiteShot = requestAccepted.findViewById(R.id.header_image);
        TextView heading = requestAccepted.findViewById(R.id.site_address);
        ImageView imageViewClose = requestAccepted.findViewById(R.id.close_button);
        String imageUrl = entity.getImageUrl();
        if (!imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(imageViewSiteShot);
        }
//        heading.setText(entity.getLocation());
        Button buttonGetStarted = requestAccepted.findViewById(R.id.accept);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(requestAccepted);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(position);
                alertDialog.hide();
                updateRequest("ACCEPTED", entity.getCartId());
            }
        });
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
            }
        });
        alertDialog.show();
    }

    public void showNotificationReject(final int position, final RequestEntity entity) {
        Activity activity = (Activity) context;
        LayoutInflater inflater = activity.getLayoutInflater();
        View requestRejected = inflater.inflate(R.layout.custom_popup_reject_reuqest, null);
        ImageView imageViewSiteShot = requestRejected.findViewById(R.id.header_image);
        TextView heading = requestRejected.findViewById(R.id.site_address);
        ImageView imageViewClose = requestRejected.findViewById(R.id.logout);
        String imageUrl = entity.getImageUrl();
        if (!imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(imageViewSiteShot);
        }
        heading.setText(entity.getLocation());
        Button buttonGetStarted = requestRejected.findViewById(R.id.get_started);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(requestRejected);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(position);
                alertDialog.hide();
                updateRequest("REJECTED", entity.getCartId());
            }
        });
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
            }
        });
        alertDialog.show();
    }

    private void updateRequest(String status, String id) {
        Toast.makeText(context, "UPDATE STATUS", Toast.LENGTH_SHORT).show();
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("status", status);
            requestObject.put("cartId", id);
            requestObject.put("deliveryboyId", uId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = CONNECTION_URL_DEMAND + DATA_PATH_ON_DEMAND_SUPPLY_STATUS_UPDATE;
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Intent intent = new Intent(context, Complete.class);
                        context.startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse != null)
                            Toast.makeText(context, "ERROR! TRY AGAIN", Toast.LENGTH_SHORT).show();
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

    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}