package com.delivery.activity.authentication;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.StringRequest;
import com.delivery.R;
import com.delivery.fragment.task.Complete;
import com.delivery.utils.network.VolleySingleton;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_DEMAND;
import static com.delivery.utils.jkeys.Keys.LoginKeys.DATA_PATH_OTP_RESEND;
import static com.delivery.utils.jkeys.Keys.LoginKeys.DATA_PATH_OTP_VERIFY;
import static com.delivery.utils.jkeys.Keys.LoginKeys.STATUS;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;

public class OneTimePasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = OneTimePasswordActivity.class.getSimpleName();
    private SharedPreferences loginDetails;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private Button buttonAccountVerification;
    private String sMobileNumber, sEmailAddress,status;
    private TextView textViewResendCodeText;
    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;
    private EditText et5;
    private EditText et6;
    private int resendCount=0;
    private ProgressBar pbResend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_time_password);
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        getActivityReference();
    }

    private void getActivityReference() {
        textViewResendCodeText =findViewById(R.id.resend_code);
        //textViewResendCodeText.setOnClickListener(this);
        et1 = (EditText) findViewById(R.id.editText1);
        et2 = (EditText) findViewById(R.id.editText2);
        et3 = (EditText) findViewById(R.id.editText3);
        et4 = (EditText) findViewById(R.id.editText4);
        et5 = (EditText) findViewById(R.id.editText5);
        et6 = (EditText) findViewById(R.id.editText6);
        et1.addTextChangedListener(new GenericTextWatcher(et1));
        et2.addTextChangedListener(new GenericTextWatcher(et2));
        et3.addTextChangedListener(new GenericTextWatcher(et3));
        et4.addTextChangedListener(new GenericTextWatcher(et4));
        et5.addTextChangedListener(new GenericTextWatcher(et5));
        et6.addTextChangedListener(new GenericTextWatcher(et6));
        buttonAccountVerification = findViewById(R.id.finish_register);
        buttonAccountVerification.setOnClickListener(this);
        textViewResendCodeText.setOnClickListener(this);
        status=getIntent().getStringExtra("status");
        pbResend=findViewById(R.id.pb_resend);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.finish_register:
                if (et1.getText().length() == 1 && et2.getText().length() == 1 && et3.getText().length() == 1 && et4.getText().length() == 1 && et5.getText().length() == 1 && et6.getText().length() == 1) {
                    String otpValue = et1.getText().toString() + et2.getText().toString() + et3.getText().toString() + et4.getText().toString()+ et5.getText().toString()+ et6.getText().toString();
                    if (otpValue.length() == 6) {
                        if(status.equals("Refund Pickup")) {
                            OneTimePasswordVerificationRefund(otpValue);
                        }
                        else if(status.equals("Shipped")){
                            OneTimePasswordVerificationDelivery(otpValue);
                        }
                    }
                } else {
                    Toast.makeText(this, "Invalid One Time Password", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.resend_code:
                if (resendCount < 3) {
                    resendCount+=1;
                    pbResend.setVisibility(View.VISIBLE);
                    textViewResendCodeText.setVisibility(View.GONE);
                    ResendOTP();
                }
                else{
                    Toast.makeText(this,"Maximum Limit Reached",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class GenericTextWatcher implements TextWatcher {
        private View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Intent intent;
            String text = editable.toString();
            switch (view.getId()) {
                case R.id.editText1:
                    if (text.length() == 1)
                        et2.requestFocus();
                    break;
                case R.id.editText2:
                    if (text.length() == 1)
                        et3.requestFocus();
                    break;
                case R.id.editText3:
                    if (text.length() == 1)
                        et4.requestFocus();
                    break;
                case R.id.editText4:
                    if (text.length() == 1)
                        et5.requestFocus();
                    break;
                case R.id.editText5:
                    if (text.length() == 1)
                        et6.requestFocus();
                    break;
                case R.id.editText6:
                    String otpValue = et1.getText().toString() + et2.getText().toString() + et3.getText().toString() + et4.getText().toString()+ et5.getText().toString()+ et6.getText().toString();
                    if (otpValue.length() == 6) {
                        if(status.equals("Refund Pickup")) {
                            OneTimePasswordVerificationRefund(otpValue);
                        }
                        else if(status.equals("Shipped")){
                            OneTimePasswordVerificationDelivery(otpValue);
                        }
                    } else {
                        Toast.makeText(OneTimePasswordActivity.this, "Invalid One Time Password", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

    }

    private void OneTimePasswordVerificationDelivery(final String otpValue) {
        String url = CONNECTION_URL_DEMAND + "api/v3/store/cart/delivery-otp-verify" + "/" +getIntent().getStringExtra("cartId")+"/"+ otpValue;
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("Success")) {
                            parseJSONObject();
                        } else {
                            Toast.makeText(OneTimePasswordActivity.this, "Entered OTP is Wrong.", Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(OneTimePasswordActivity.this, "Network Error !" + volleyError, Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }
    private void OneTimePasswordVerificationRefund(final String otpValue) {
        String url = CONNECTION_URL_DEMAND + "api/v3/store/cart/refund-otp-verify" + "/" +getIntent().getStringExtra("cartId")+"/"+ otpValue;
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("Success")) {
                            parseJSONObject();
                        } else {
                            Toast.makeText(OneTimePasswordActivity.this, "Entered OTP is Wrong.", Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(OneTimePasswordActivity.this, "Network Error !" + volleyError, Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }
    private void parseJSONObject() {
        startActivity(new Intent(this, Complete.class));
        //showNotification();
    }


    //OTP RESEND

    private void OneTimePasswordResend() {
        String url = CONNECTION_URL + DATA_PATH_OTP_RESEND;
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put(USER_ID, sharedPreferencesStatus.getString(USER_ID, "0"));
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


    public void showNotification() {

        LayoutInflater inflater = this.getLayoutInflater();
        View viewAccountVerification = inflater.inflate(R.layout.custom_popup_account_verify, null);
        Button buttonGetStarted = viewAccountVerification.findViewById(R.id.get_started);
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OneTimePasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(viewAccountVerification);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void ResendOTP()
    {
        String url = CONNECTION_URL_DEMAND + "api/v3/store/cart/regenerate-otp" + "/" +getIntent().getStringExtra("cartId");
        Log.i("RESENDOTP", url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pbResend.setVisibility(View.GONE);
                        textViewResendCodeText.setVisibility(View.VISIBLE);
                        if (response.equals("true")) {
                            Toast.makeText(OneTimePasswordActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                            textViewResendCodeText.setEnabled(false);
                            textViewResendCodeText.setVisibility(View.GONE);
                            new Handler().postDelayed(new Runnable()
                                                      {
                                                          public void run()
                                                          {
                                                             textViewResendCodeText.setEnabled(true);
                                                              textViewResendCodeText.setVisibility(View.VISIBLE);
                                                          }
                                                      }, 120000
                            );
                        } else {
                           Toast.makeText(OneTimePasswordActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        pbResend.setVisibility(View.GONE);
                        textViewResendCodeText.setVisibility(View.VISIBLE);
                        Toast.makeText(OneTimePasswordActivity.this, "Network Error !" + volleyError, Toast.LENGTH_SHORT).show();
                    }
                }
        );
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }
}
