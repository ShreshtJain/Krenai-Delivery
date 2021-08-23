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
public class HistoryTaskAdapter extends RecyclerView.Adapter<HistoryTaskAdapter.PendingRequestViewHolder> {

    private Context context;
    private List<TaskEntity> data;
    private LayoutInflater inflater;

    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;

    public HistoryTaskAdapter(Context context, List<TaskEntity> data) {
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
        }
        @Override
        public void onClick(View view) {
        }
    }


}