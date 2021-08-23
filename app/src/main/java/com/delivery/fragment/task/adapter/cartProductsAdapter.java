package com.delivery.fragment.task.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.delivery.R;
import com.delivery.activity.authentication.OneTimePasswordActivity;
import com.delivery.activity.media.MediaActivity;
import com.delivery.fragment.task.entity.ProductsEntity;
import com.delivery.fragment.task.entity.RequestEntity;
import com.delivery.fragment.task.entity.TaskEntity;
import com.delivery.site.SiteDetailActivity;
import com.delivery.utils.network.VolleySingleton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.StringValue;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.PendingIntent.getActivity;
import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_DEMAND;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.RefreshLocation.LOCATION_UPDATE_LONGITUDE;


/**
 * Created by Pushpendra on 16-09-2019.
 */
public class cartProductsAdapter extends RecyclerView.Adapter<cartProductsAdapter.PendingRequestViewHolder>{


    private Context context;
    private List<ProductsEntity> data;
    private LayoutInflater inflater;
    private int cartId;
    private String status;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferenceStatus;
    public JSONObject jsonObject;



    public cartProductsAdapter(Context context, List<ProductsEntity> data,int cartId,String status) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        sharedPreferenceStatus = context.getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        this.cartId=cartId;
        this.status=status;
        this.jsonObject=createJson(data);
    }

    @NonNull
    @Override
    public PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_products, parent, false);
        PendingRequestViewHolder holder = new PendingRequestViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PendingRequestViewHolder holder, int position) {
        ProductsEntity current = data.get(position);
        holder.textViewProductName.setText(current.getProductName());
        holder.textViewBrandName.setText(current.getBrandName());
        holder.textViewAmount.setText("₹ "+ current.getAmount());
        holder.textViewQuantity.setText(current.getProductQuantity());
        if(current.getImageUrl()!=null )
        Picasso.get().load(current.getImageUrl()).into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class PendingRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewProductName,
                textViewBrandName, textViewAmount,textViewQuantity,add,minus,cardViewquantity;
        private ImageView productImage;


        private PendingRequestViewHolder(View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.product_name);
            textViewBrandName = itemView.findViewById(R.id.product_brand);
            textViewAmount = itemView.findViewById(R.id.product_amount);
            productImage = itemView.findViewById(R.id.product_image);
            textViewQuantity=itemView.findViewById(R.id.product_quantity);
            add=itemView.findViewById(R.id.plus);
            minus=itemView.findViewById(R.id.minus);
            cardViewquantity=itemView.findViewById(R.id.quantity);

            minus.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Integer.parseInt(cardViewquantity.getText().toString())>0)
                    {
                        cardViewquantity.setText(String.valueOf(Integer.parseInt(cardViewquantity.getText().toString())-1));
                        try{
                            jsonObject.getJSONArray("refundedProducts").getJSONObject(getPosition()).put("approvedRefundedQuantity",cardViewquantity.getText().toString());
                            jsonObject.getJSONArray("refundedProducts").getJSONObject(getPosition()).put("quantity",cardViewquantity.getText().toString());
                        }
                        catch (JSONException e)
                        {

                        }


                    }


                }
            }));
            add.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     ProductsEntity productEntity=data.get(getPosition());
                    if(Integer.parseInt(cardViewquantity.getText().toString())<Integer.parseInt(productEntity.getProductQuantity()))
                    {
                        cardViewquantity.setText(String.valueOf(Integer.parseInt(cardViewquantity.getText().toString())+1));
                        try{
                            jsonObject.getJSONArray("refundedProducts").getJSONObject(getPosition()).put("approvedRefundedQuantity",cardViewquantity.getText().toString());
                            jsonObject.getJSONArray("refundedProducts").getJSONObject(getPosition()).put("quantity",cardViewquantity.getText().toString());
                        }
                        catch (JSONException e)
                        {

                        }
                    }


                }
            }));

        }

        @Override
        public void onClick(View view) {
        }
    }

    private JSONObject createJson(List<ProductsEntity> data)
    {
        JSONObject j=new JSONObject();
        JSONArray refundedProducts=new JSONArray();
        for(int i=0;i<data.size();i++)
        {
            try {
                JSONObject j2=new JSONObject();
                j2.put("approvedRefundedQuantity",0);
                j2.put("cartId",cartId);
                j2.put("productId",data.get(i).getProductId());
                j2.put("productListingId",data.get(i).getProductListingId());
                j2.put("quantity",0);
                j2.put("skuCode",data.get(i).getSkuCode());
                refundedProducts.put(i, j2);
            }
            catch (JSONException e)
            {

            }
        }
        try {
            j.put("refundedProducts", refundedProducts);
        }catch(JSONException e)
        {

        }
        return j;
    }
}