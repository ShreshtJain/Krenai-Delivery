package com.delivery.fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.delivery.R;
import com.delivery.activity.media.MediaActivity;
import com.delivery.fragment.entity.NearbySiteEntity;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LONGITUDE;


/**
 * Created by Pushpendra on 16-09-2019.
 */
public class NearbySiteAdapter extends RecyclerView.Adapter<NearbySiteAdapter.HistoryViewHolder> {

    private Context context;
    private List<NearbySiteEntity> data;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;

    public NearbySiteAdapter(Context context, List<NearbySiteEntity> data) {
        this.context = context;
        this.data = data;
        sharedPreferenceStatus = context.getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_popup_nearby_reuqest, parent, false);
        HistoryViewHolder holder = new HistoryViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        NearbySiteEntity current = data.get(position);
        //holder.textViewSiteShotRequestId.setText(String.valueOf(current.getBrandName()));
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        holder.textViewSiteShotLocation.setPaintFlags(holder.textViewSiteShotLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        String imageUrl = current.getImageUrl();
        if (!imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(holder.imageViewSiteShotImage);
        }
        holder.textViewSiteShotLocation.setText(current.getLocation());
        Long dis = Math.round(distance(Double.parseDouble(sharedPreferenceStatus.getString(LOCATION_UPDATE_LATITUDE, "")), Double.parseDouble(sharedPreferenceStatus.getString(LOCATION_UPDATE_LONGITUDE, "")),
                Double.parseDouble(current.getLatitude()), Double.parseDouble(current.getLongitude()), 'K'));
        holder.textViewDistance.setText(String.valueOf(dis));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewDistance,
                textViewRemainingFulfillRequest;
        private TextView textViewSiteShotLocation;
        private ImageView imageViewSiteShotImage;
        private LinearLayout linearLayoutTakePictureButton;


        public HistoryViewHolder(View itemView) {
            super(itemView);


            imageViewSiteShotImage = itemView.findViewById(R.id.brand_image);
            textViewSiteShotLocation = itemView.findViewById(R.id.site_shot_location);
            linearLayoutTakePictureButton = itemView.findViewById(R.id.take_picture_button);
            linearLayoutTakePictureButton.setOnClickListener(this);
            textViewSiteShotLocation.setOnClickListener(this);
            textViewDistance = itemView.findViewById(R.id.distance);
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.take_picture_button:
                    intent = new Intent(context, MediaActivity.class);
                    context.startActivity(intent);
                    break;
                case R.id.site_shot_location:
                    NearbySiteEntity taskEntity = data.get(getPosition());
                    Uri gmmIntentUri = Uri.parse("google.streetview:cbll=" + taskEntity.getLatitude() + "," + taskEntity.getLongitude());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);
                    break;
            }
        }
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