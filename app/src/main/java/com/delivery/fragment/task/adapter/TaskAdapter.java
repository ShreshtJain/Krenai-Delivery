package com.delivery.fragment.task.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.delivery.R;
import com.delivery.activity.media.MediaActivity;
import com.delivery.fragment.task.entity.RequestEntity;
import com.delivery.fragment.task.entity.TaskEntity;
import com.delivery.site.SiteDetailActivity;
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
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.PendingRequestViewHolder> {

    private Context context;
    private List<TaskEntity> data;
    private LayoutInflater inflater;

    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;

    public TaskAdapter(Context context, List<TaskEntity> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        sharedPreferenceStatus = context.getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_request_accepted, parent, false);
        PendingRequestViewHolder holder = new PendingRequestViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PendingRequestViewHolder holder, int position) {
        TaskEntity current = data.get(position);
        holder.textViewSiteShotLocation.setText(current.getLocation());
        holder.textViewSupplierName.setText(current.getCustomer());
        holder.textViewMode.setText(current.getMode() + " - " +"₹ "+ current.getAmount());
        holder.textViewTime.setText(current.getRequestAcceptedTime().subSequence(0,10));
        holder.textViewDate.setText(current.getRequestAcceptedTime().subSequence(11,19));
        String status = current.getDescription();
        holder.textViewStatus.setText(status);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class PendingRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewSupplierName,
                textViewMode, textViewTime,textViewDate, textViewStatus;
        private TextView textViewSiteShotLocation;


        private PendingRequestViewHolder(View itemView) {
            super(itemView);
            textViewSupplierName = itemView.findViewById(R.id.site_shot_id);
            textViewMode = itemView.findViewById(R.id.payment_mode);
            textViewTime = itemView.findViewById(R.id.remaining_to_fulfill_request);
            textViewDate = itemView.findViewById(R.id.remaining_to_fulfill_request_date);
            textViewSiteShotLocation = itemView.findViewById(R.id.site_shot_location);
            textViewStatus = itemView.findViewById(R.id.status);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskEntity taskEntity = data.get(getPosition());

                        Intent intent =  new Intent(context, SiteDetailActivity.class);

                        intent.putExtra("site_name",taskEntity.getCustomer());
                        intent.putExtra("cart_id", taskEntity.getRequestId());
                        intent.putExtra("description", taskEntity.getLocation());
                        intent.putExtra("image", taskEntity.getImageUrl());
                        intent.putExtra("basePrice", taskEntity.getUnpaidAmount());
                        Log.i("basePrice",taskEntity.getUnpaidAmount());
                        intent.putExtra("customer", taskEntity.getMode());
                        intent.putExtra("customerPhone", taskEntity.getMode());
                        intent.putExtra("modeOfPayment", taskEntity.getMode());;
                        intent.putExtra("customer_lat", taskEntity.getCustomerLatitude());
                        intent.putExtra("customer_lang", taskEntity.getCustomerLongitude());
                        intent.putExtra("state", taskEntity.getStatus());
                        intent.putExtra("status", taskEntity.getDescription());
                        intent.putExtra("cartProductRequests",taskEntity.getCartProductRequests());
                        intent.putExtra("refundedProducts",taskEntity.getRefundedProducts());
                        intent.putExtra("refundedProductsAmount",taskEntity.getRefundedProductsAmount());
                        context.startActivity(intent);
                    }
            });
        }

        @Override
        public void onClick(View view) {
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