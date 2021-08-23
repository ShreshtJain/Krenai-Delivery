package com.delivery.site;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.delivery.R;
import com.delivery.fragment.task.Complete;
import com.delivery.utils.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_RFP_MANAGER;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.SiteManager.COMMENT;
import static com.delivery.utils.jkeys.Keys.SiteManager.DATA_PATH_SITE_STATUS_UPDATE;
import static com.delivery.utils.jkeys.Keys.SiteManager.END_DATE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_ID;
import static com.delivery.utils.jkeys.Keys.SiteManager.START_DATE;
import static com.delivery.utils.jkeys.Keys.SiteManager.STATUS;

public class SiteReservation extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SiteReservation.class.getSimpleName();
    private LinearLayout updateDate;
    private EditText editTextComment;
    private String startDate, endDate, comment, siteId;
    private TextView mmYYStart, mmYYEnd, ddStart, ddEnd, dayStart, dayEnd;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private ImageView arrow, closeButton;
    private LinearLayout progress;
    private Button statusUpdateButton;
    private Date dateStart, dateEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_reservation);
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        updateDate = findViewById(R.id.update_date);
        Bundle extra = getIntent().getExtras();
        siteId = extra.getString("siteId");
        closeButton = findViewById(R.id.close_button);
        mmYYStart = findViewById(R.id.mm_yy_start);
        mmYYEnd = findViewById(R.id.mm_yy_end);
        ddStart = findViewById(R.id.dd_start);
        ddEnd = findViewById(R.id.dd_end);
        dayStart = findViewById(R.id.day_start);
        dayEnd = findViewById(R.id.day_end);
        editTextComment = findViewById(R.id.comment);
        arrow = findViewById(R.id.arrow);
        progress = findViewById(R.id.login_progress);
        statusUpdateButton = findViewById(R.id.status_update_button);
        statusUpdateButton = findViewById(R.id.status_update_button);
        statusUpdateButton.setOnClickListener(this);
        updateDate.setOnClickListener(this);
        closeButton.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && data != null) {
            startDate = data.getStringExtra("startDate");
            endDate = data.getStringExtra("endDate");


            if (startDate != null && endDate != null) {
                showDates();
            } else {
                Snackbar snackbar = Snackbar
                        .make(updateDate, "PLEASE SELECT THE VALID DATE FORMAT ", Snackbar.LENGTH_SHORT)
                        .setAction("CLOSE", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                snackbar.show();
            }
        }
    }

    private void showDates() {

        try {
            dateStart = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(startDate);
            dateEnd = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(endDate);

            Calendar startCalSelected = Calendar.getInstance();
            startCalSelected.setTime(dateStart);

            Calendar endCalSelected = Calendar.getInstance();
            endCalSelected.setTime(dateEnd);

            ddStart.setText(String.valueOf(startCalSelected.get(Calendar.DAY_OF_MONTH)));
            ddEnd.setText(String.valueOf(endCalSelected.get(Calendar.DAY_OF_MONTH)));

            mmYYStart.setText(String.valueOf(getMonthName(startCalSelected.get(Calendar.MONTH) + 1) + " | " + startCalSelected.get(Calendar.YEAR)));
            mmYYEnd.setText(String.valueOf(getMonthName(endCalSelected.get(Calendar.MONTH) + 1) + " | " + endCalSelected.get(Calendar.YEAR)));

            dayStart.setText(String.valueOf(getDayName(startCalSelected.get(Calendar.DAY_OF_WEEK))));
            dayEnd.setText(String.valueOf(getDayName(endCalSelected.get(Calendar.DAY_OF_WEEK))));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public String getDayName(int count) {
        switch (count) {
            case Calendar.SUNDAY:
                return "SUNDAY";
            case Calendar.MONDAY:
                return "MONDAY";
            case Calendar.TUESDAY:
                return "TUESDAY";
            case Calendar.WEDNESDAY:
                return "WEDNESDAY";
            case Calendar.THURSDAY:
                return "THURSDAY";
            case Calendar.FRIDAY:
                return "FRIDAY";
            case Calendar.SATURDAY:
                return "SATURDAY";
        }
        return "ERROR";
    }

    public String getMonthName(int count) {
        switch (count) {
            case Calendar.JANUARY:
                return "JANUARY";
            case Calendar.FEBRUARY:
                return "FEBRUARY";
            case Calendar.MARCH:
                return "MARCH";
            case Calendar.APRIL:
                return "APRIL";
            case Calendar.MAY:
                return "MAY";
            case Calendar.JUNE:
                return "JUNE";
            case Calendar.JULY:
                return "JULY";
            case Calendar.AUGUST:
                return "AUGUST";
            case Calendar.SEPTEMBER:
                return "SEPTEMBER";
            case Calendar.OCTOBER:
                return "OCTOBER";
            case Calendar.NOVEMBER:
                return "NOVEMBER";
            case Calendar.DECEMBER:
                return "DECEMBER";
        }
        return "ERROR";
    }


    private void updateReservation() {
        arrow.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        comment = editTextComment.getText().toString().trim();
        if (startDate == null || endDate == null || comment == null) {
            Snackbar snackbar = Snackbar
                    .make(editTextComment, "ALL FIELDS ARE REQUIRED ", Snackbar.LENGTH_SHORT)
                    .setAction("CLOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
            snackbar.show();
            return;
        }
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put(SITE_ID, Integer.parseInt(siteId));
            requestObject.put(START_DATE, new Timestamp(dateStart.getTime()));
            requestObject.put(END_DATE, new Timestamp(dateEnd.getTime()));
            requestObject.put(COMMENT, comment);
            requestObject.put(STATUS, "hold");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("DATA", "" + requestObject);
        String url = CONNECTION_URL_RFP_MANAGER + DATA_PATH_SITE_STATUS_UPDATE;
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        arrow.setVisibility(View.VISIBLE);
                        progress.setVisibility(View.GONE);
                        parseJSONObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse.statusCode == 409) {

                            arrow.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                            Toast.makeText(SiteReservation.this, "Unknown Error.", Toast.LENGTH_SHORT).show();
                        } else {

                            arrow.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                            Toast.makeText(SiteReservation.this, "Network Error !" + volleyError, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-correlation-id", "true");
                headers.put("Authorization", "Bearer " + sharedPreferencesStatus.getString(TOKEN, ""));
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

    private void parseJSONObject(JSONObject jsonObject) {
        startActivity(new Intent(this, Complete.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.status_update_button:
                updateReservation();
                break;
            case R.id.update_date:
                Intent intent = new Intent(SiteReservation.this, DateRange.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.close_button:
                finish();
                break;
        }
    }
}
