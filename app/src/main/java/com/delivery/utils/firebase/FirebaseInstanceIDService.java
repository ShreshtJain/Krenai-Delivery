package com.delivery.utils.firebase;

import android.util.Log;

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

import com.delivery.utils.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.FireBaseToken.DATA_PATH_REFRESH_FIREBASE_TOKEN;
import static com.delivery.utils.jkeys.Keys.FireBaseToken.FIREBASE_UPDATE_DEVICE_TOKEN;
import static com.delivery.utils.jkeys.Keys.FireBaseToken.FIREBASE_UPDATE_USER_ID;

public class FirebaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG = FirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "TOKEN : " + token);
        TokenRegistration(token);
    }

    private void TokenRegistration(String token) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(FIREBASE_UPDATE_DEVICE_TOKEN, token);
            obj.put(FIREBASE_UPDATE_USER_ID, "7");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = CONNECTION_URL + DATA_PATH_REFRESH_FIREBASE_TOKEN;
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse.statusCode == 409) {

                        } else {

                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-correlation-id", "true");
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

}
