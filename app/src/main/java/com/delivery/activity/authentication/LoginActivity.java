package com.delivery.activity.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.delivery.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.delivery.R;
import com.delivery.utils.network.VolleySingleton;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.FireBaseToken.DATA_PATH_REFRESH_FIREBASE_TOKEN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.DATA_PATH_LOGIN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.STATUS;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_NAME;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_PASSWORD;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button buttonLogin;
    private LinearLayout linearLayoutRegister;
    private LinearLayout linearLayoutLoading;
    private ImageView imageViewArrow;
    private static int SPLASH_TIME_OUT = 500;
    private int returnStatus = 0;
    private String sMobile, sPassword;
    private TextView textViewForgotPassword;
    private EditText editTextPhone, editTextPassword;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private FirebaseAuth mAuth;
    private AwesomeValidation awesomeValidation;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        getReferenceId();
    }

    private void getReferenceId() {
        awesomeValidation = new AwesomeValidation(ValidationStyle.COLORATION);
        buttonLogin = findViewById(R.id.sign_in_button);
        linearLayoutLoading = findViewById(R.id.login_progress);
        textViewForgotPassword = findViewById(R.id.forgot_password_text);
        textViewForgotPassword.setOnClickListener(this);
        imageViewArrow = findViewById(R.id.arrow);
        linearLayoutRegister = findViewById(R.id.login_register_button);
        linearLayoutRegister.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        editTextPhone = findViewById(R.id.reg_phone);
        editTextPassword = findViewById(R.id.reg_safe_code);
        awesomeValidation.addValidation(this, R.id.reg_phone, "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$", R.string.errorMessageUsername);
        awesomeValidation.addValidation(this, R.id.reg_safe_code, "^[A-Za-z0-9\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}[0-9]{0,}$", R.string.errorMessageSafeCode);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {

            case R.id.forgot_password_text:
                Intent e = new Intent(this, ForgotPasswordActivity.class);
                startActivity(e);
                finish();
                break;
            case R.id.sign_in_button:
                if (awesomeValidation.validate()) {
                    imageViewArrow.setVisibility(View.GONE);
                    linearLayoutLoading.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (returnStatus == 0) {
                                login();
                            }
                        }
                    }, SPLASH_TIME_OUT);
                }
                break;
            case R.id.login_register_button:
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }

    private void login() {
        sMobile = editTextPhone.getText().toString().trim();
        sPassword = editTextPassword.getText().toString().trim();
        String url = CONNECTION_URL + DATA_PATH_LOGIN;

        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String jsonObject) {
                        parseLoginJSONObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse.statusCode == 400) {
                            imageViewArrow.setVisibility(View.VISIBLE);
                            linearLayoutLoading.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Account is not verified.", Toast.LENGTH_SHORT).show();
                        } else {
                            imageViewArrow.setVisibility(View.VISIBLE);
                            linearLayoutLoading.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Invalid ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Authorization", "Basic VVNFUl9ERUxJVkVSWV9BUFA6cGFzc3dvcmQ=");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(USER_NAME, sMobile);
                params.put(USER_PASSWORD, sPassword);
                params.put("grant_type", "password");
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
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


    private void parseLoginJSONObject(String jsonObject) {
        Map map = new Gson().fromJson(jsonObject, HashMap.class);
        SharedPreferences.Editor editor = sharedPreferencesStatus.edit();
        Log.d(TAG, "" + jsonObject);
        editor.putString(TOKEN, map.get(TOKEN).toString());
        editor.putString(USER_ID, map.get(USER_ID).toString());
        editor.putInt(STATUS, 4);
        editor.apply();
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
        finishAffinity();
        //TokenRegistration(map.get(USER_ID).toString());
    }

    private void TokenRegistration(final String userId) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String mToken = instanceIdResult.getToken();
                Log.e("Token",mToken);
                int num = (int) Double.parseDouble(userId);
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, sharedPreferencesStatus.getString(TOKEN, ""));
                String url = CONNECTION_URL + DATA_PATH_REFRESH_FIREBASE_TOKEN + "/" + num + "/" + token;
                Log.d("KRENAI TEST", "" + url);
                final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
                StringRequest jsonObjectRequest = new StringRequest(
                        Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String jsonObject) {
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.d("KRENAI TEST", "" + volleyError);
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Bearer " + sharedPreferencesStatus.getString(TOKEN, ""));
                        return headers;
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
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
                RetryPolicy policy = new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                jsonObjectRequest.setRetryPolicy(policy);
                jsonObjectRequest.setShouldCache(false);
                requestQueue.add(jsonObjectRequest);
            }
        });
    }

}
