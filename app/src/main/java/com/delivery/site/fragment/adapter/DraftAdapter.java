package com.delivery.site.fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.delivery.R;
import com.delivery.site.SiteDetailActivity;
import com.delivery.site.fragment.entity.SiteEntity;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Pushpendra on 16-09-2019.
 */
public class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.DraftSitesViewHolder> {

    private Context context;
    private List<SiteEntity> data;

    public DraftAdapter(Context context, List<SiteEntity> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public DraftSitesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_draft_site_manager, parent, false);
        DraftSitesViewHolder holder = new DraftSitesViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DraftSitesViewHolder holder, int position) {
        SiteEntity current = data.get(position);
        holder.textViewSiteName.setText(current.getName());
        holder.textViewDescription.setText(current.getDescription());
        holder.textViewDisplayType.setText(current.getSiteDisplayType());
        holder.textViewSiteType.setText(current.getSiteType());
        holder.textViewLocationType.setText(current.getSiteLocationType());
        holder.textViewMediaType.setText(current.getSiteMediaType());
        holder.textViewSiteIlluminationType.setText(current.getSiteIlluminationType());
        String imageUrl = current.getPhotosUrl();
        if (!imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(holder.imageViewSiteImage);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class DraftSitesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout linearLayoutSiteListing;
        private TextView textViewSiteName, textViewDisplayType, textViewDescription,
                textViewSiteType, textViewMediaType, textViewLocationType, textViewSiteIlluminationType;
        private ImageView imageViewSiteImage;

        public DraftSitesViewHolder(View itemView) {
            super(itemView);
            linearLayoutSiteListing = itemView.findViewById(R.id.site_listing);
            textViewSiteName = itemView.findViewById(R.id.site_name);
            textViewDisplayType = itemView.findViewById(R.id.display_type);
            imageViewSiteImage = itemView.findViewById(R.id.site_image);
            textViewDescription = itemView.findViewById(R.id.description);
            textViewSiteType = itemView.findViewById(R.id.site_type);
            textViewLocationType = itemView.findViewById(R.id.location_type);
            textViewMediaType = itemView.findViewById(R.id.media_type);
            textViewSiteIlluminationType = itemView.findViewById(R.id.site_illumination_type);
            linearLayoutSiteListing.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.site_listing:
                    SiteEntity siteEntity = data.get(getPosition());
                    intent = new Intent(context, SiteDetailActivity.class);
                    intent.putExtra("site_name", siteEntity.getName());
                    intent.putExtra("site_display_type", siteEntity.getSiteDisplayType());
                    intent.putExtra("site_image", siteEntity.getPhotosUrl());
                    intent.putExtra("site_id", siteEntity.getSiteId());
                    intent.putExtra("description", siteEntity.getDescription());
                    intent.putExtra("siteLocationType", siteEntity.getSiteLocationType());
                    intent.putExtra("siteIlluminationType", siteEntity.getSiteIlluminationType());
                    intent.putExtra("siteMediaType", siteEntity.getSiteMediaType());
                    intent.putExtra("siteType", siteEntity.getSiteType());
                    intent.putExtra("basePrice", siteEntity.getBasePrice());
                    intent.putExtra("status", siteEntity.getStatusType());
                    context.startActivity(intent);
                    break;
            }
        }
    }
}