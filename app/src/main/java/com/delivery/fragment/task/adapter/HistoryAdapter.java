package com.delivery.fragment.task.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import com.delivery.R;
import com.delivery.fragment.task.entity.HistoryEntity;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by Pushpendra on 16-09-2019.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistoryEntity> data;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private String sessionId;
    private String currentLat;
    private String currentLang;
    private static int bStatus;
    private SharedPreferences sharedpreferences;


    public HistoryAdapter(Context context, List<HistoryEntity> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_history, parent, false);
        HistoryViewHolder holder = new HistoryViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntity current = data.get(position);
        holder.siteShotLocation.setText(current.getCustomLocation());
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(current.getTimeStamp()));
        String date = DateFormat.format("dd MMM yyyy | HH.mm", cal).toString();
        holder.siteShotTime.setText(date);
        String imageUrl = current.getFocusImage();
        if (!imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(holder.siteShotFocusImage);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView siteShotTime, siteShotLocation;
        private ImageView siteShotFocusImage;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            siteShotFocusImage = itemView.findViewById(R.id.site_shot_focus_image);
            siteShotLocation = itemView.findViewById(R.id.site_shot_location);
            siteShotTime = itemView.findViewById(R.id.time);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {

            }
        }
    }
}