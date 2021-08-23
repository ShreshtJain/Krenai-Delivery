package com.delivery.site.fragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.delivery.R;
import com.delivery.site.fragment.entity.Market;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.PendingRequestViewHolder> {

    private Context context;
    private List<Market> data;
    private int row_index = -1;
    AdapterCallback callback;

    public interface AdapterCallback {
        void marketSelect(Market market);
    }

    public MarketAdapter(Context context, List<Market> data,AdapterCallback callback) {
        this.context = context;
        this.data = data;
        this.callback = callback;
    }

    @NonNull
    @Override
    public PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_filter, parent, false);
        PendingRequestViewHolder holder = new PendingRequestViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PendingRequestViewHolder holder, int position) {
        Market current = data.get(position);
        holder.textViewName.setText(current.getName());
        if (row_index == position) {
            holder.selectButton.setBackground(context.getResources().getDrawable(R.drawable.button_blue));
            holder.selectButton.setColorFilter(Color.argb(255, 255, 255, 255));
        } else {
            holder.selectButton.setBackground(context.getResources().getDrawable(R.drawable.button_white));
            holder.selectButton.setColorFilter(Color.argb(0, 0, 0, 0));
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class PendingRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout linearLayoutFilterHolder;
        private TextView textViewName;
        private ImageView selectButton;

        public PendingRequestViewHolder(View itemView) {
            super(itemView);
            linearLayoutFilterHolder = itemView.findViewById(R.id.filter_holder);
            textViewName = itemView.findViewById(R.id.site_market);
            selectButton = itemView.findViewById(R.id.select_button);

            linearLayoutFilterHolder.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.filter_holder:
                    Market current = data.get(getPosition());
                    row_index = getAdapterPosition();
                    notifyDataSetChanged();
                    if (callback != null) {
                        callback.marketSelect(current);
                    }
                    break;
            }
        }
    }

}