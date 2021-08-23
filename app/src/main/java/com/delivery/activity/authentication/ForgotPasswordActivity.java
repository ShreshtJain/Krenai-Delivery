package com.delivery.activity.authentication;

import androidx.appcompat.app.AppCompatActivity;

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
import com.android.volley.toolbox.StringRequest;
import com.delivery.R;
import com.delivery.utils.network.VolleySingleton;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.ForgotPassword.DATA_PATH_UPDATE_SAFE_CODE;
import static com.delivery.utils.jkeys.Keys.ForgotPassword.DATA_PATH_VERIFY_PHONE;
import static com.delivery.utils.jkeys.Keys.LoginKeys.ONE_TIME_PASSWORD;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_PHONE_NUMBER;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_SAFE_CODE;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ForgotPasswordActivity.class.getSimpleName();
    private EditText editTextPhoneNumber, editTextOneTimePassword, editTextSafeCode;
    private String sPhoneNumber, sOneTimePassword, sSafeCode;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private ImageView imageViewArrow;
    private static int SPLASH_TIME_OUT = 500;
    private AwesomeValidation awesomeValidation;
    private Button buttonVerifyAccount, buttonUpdatePassword;
    private LinearLayout linearLayoutLoadingVerify, linearLayoutLoadingUpdate,
            linearLayoutVerifyHolder, linearLayoutUpdateHolder, buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getReferenceId();
    }

    private void getReferenceId() {
        awesomeValidation = new AwesomeValidation(ValidationStyle.COLORATION);
        buttonVerifyAccount = findViewById(R.id.verify_button);
        buttonUpdatePassword = findViewById(R.id.update_button);
        buttonBack = findViewById(R.id.back_to_button);
        linearLayoutLoadingVerify = findViewById(R.id.login_progress_verify);
        linearLayoutLoadingUpdate = findViewById(R.id.update_progress);
        linearLayoutVerifyHolder = findViewById(R.id.email_login_form);
        linearLayoutUpdateHolder = findViewById(R.id.forgot_password_holder);
        imageViewArrow = findViewById(R.id.arrow);
        buttonVerifyAccount.setOnClickListener(this);
        buttonUpdatePassword.setOnClickListener(this);
        buttonBack.setOnClickListener(this);
        editTextPhoneNumber = findViewById(R.id.reg_phone);
        editTextOneTimePassword = findViewById(R.id.reg_one_time_password);
        editTextSafeCode = findViewById(R.id.reg_safe_code_new);
        awesomeValidation.addValidation(this, R.id.reg_one_time_password, "^[0-9]{6}$", R.string.errorMessageSafeCode);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.verify_button:
                sPhoneNumber = editTextPhoneNumber.getText().toString().trim();
                if (!sPhoneNumber.equals("") && sPhoneNumber != null && sPhoneNumber.matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")){
                    verifyPhoneNumber();
                } else {
                    Toast.makeText(this, "Enter Valid Username", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.update_button:
                if (awesomeValidation.validate()) {
                    updateSafeCode();
                }
                break;
            case R.id.back_to_button:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }


    private void verifyPhoneNumber() {
        linearLayoutLoadingVerify.setVisibility(View.VISIBLE);
        imageViewArrow.setVisibility(View.GONE);
        String url = CONNECTION_URL + DATA_PATH_VERIFY_PHONE + sPhoneNumber+"/DEL";
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("true")) {
                            linearLayoutVerifyHolder.setVisibility(View.GONE);
                            linearLayoutUpdateHolder.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Username is not found in database", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        imageViewArrow.setVisibility(View.VISIBLE);
                        linearLayoutLoadingVerify.setVisibility(View.GONE);
                        Toast.makeText(ForgotPasswordActivity.this, "Network Error !", Toast.LENGTH_SHORT).show();
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

    private void updateSafeCode() {
        sOneTimePassword = editTextOneTimePassword.getText().toString().trim();
        sSafeCode = editTextSafeCode.getText().toString().trim();
        String url = CONNECTION_URL + DATA_PATH_UPDATE_SAFE_CODE + sPhoneNumber + "/" + sSafeCode + "/" + sOneTimePassword+"/DEL";
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String jsonObject) {
                        Log.i("response",jsonObject);
                        if(jsonObject.equals("true")) {
                            Log.i("response1",jsonObject);
                            showNotification();
                        }
                        else{
                            Toast.makeText(ForgotPasswordActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        imageViewArrow.setVisibility(View.VISIBLE);
                        linearLayoutLoadingVerify.setVisibility(View.GONE);
                        Toast.makeText(ForgotPasswordActivity.this, "Network Error !"+volleyError, Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("x-correlation-id", "true");
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
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    public void showNotification() {

        LayoutInflater inflater = this.getLayoutInflater();
        View viewAccountVerification = inflater.inflate(R.layout.custom_popup_safe_code_update, null);
        Button buttonGetStarted = viewAccountVerification.findViewById(R.id.get_started);
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(viewAccountVerification);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
