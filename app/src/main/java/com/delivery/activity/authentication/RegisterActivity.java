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
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import com.delivery.R;
import com.delivery.utils.network.VolleySingleton;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.CommonResources.STATIC_SUPPLIER_ID;
import static com.delivery.utils.jkeys.Keys.LoginKeys.DATA_PATH;
import static com.delivery.utils.jkeys.Keys.LoginKeys.STATUS;
import static com.delivery.utils.jkeys.Keys.LoginKeys.SUPPLIER_ID;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_CITY;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_LINE_TWO;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_PIN_CODE;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_STATE;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_STREET;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_FIRST_NAME;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_LAST_NAME;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_PHONE_NUMBER;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_SAFE_CODE;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button buttonLogin;
    private LinearLayout linearLayoutProgressBar;
    private ImageView arrow;
    private LinearLayout buttonRegister;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private static int SPLASH_TIME_OUT = 500;
    private int returnStatus = 0;
    private AwesomeValidation validation;

    private EditText editTextUserName, editTextMobile, editTextSafeCode,
            editTextAddressLineOne, editTextAddressLineTwo, editTextCity,
            editTextState, editTextPinCode;
    private String sFullName, sPhoneNumber, sSafeCode, sAddressLineOne, sAddressLineTwo,
            sCity, sState, sPinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        registerFrom();
    }

    private void registerFrom() {
        validation = new AwesomeValidation(ValidationStyle.COLORATION);
        buttonLogin = findViewById(R.id.sign_in_button);
        linearLayoutProgressBar = findViewById(R.id.login_progress);
        arrow = findViewById(R.id.arrow);
        buttonRegister = findViewById(R.id.login_register_button);
        buttonRegister.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        editTextUserName = findViewById(R.id.reg_name);
        editTextMobile = findViewById(R.id.reg_contact_number);
        editTextSafeCode = findViewById(R.id.reg_safe_code);
        editTextAddressLineOne = findViewById(R.id.reg_address_line_first);
        editTextAddressLineTwo = findViewById(R.id.reg_address_line_second);
        editTextCity = findViewById(R.id.reg_city);
        editTextState = findViewById(R.id.reg_state);
        editTextPinCode = findViewById(R.id.reg_address_pin_code);

        validation.addValidation(this, R.id.reg_name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.errorMessageName);
        validation.addValidation(this, R.id.reg_contact_number, "^[0-9]{2}[0-9]{8}$", R.string.errorMessagePhone);
        validation.addValidation(this, R.id.reg_safe_code, "^[0-9]{0,}$", R.string.errorMessageSafeCode);

        validation.addValidation(this, R.id.reg_city, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.errorMessageCity);
        validation.addValidation(this, R.id.reg_state, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.errorMessageState);
        validation.addValidation(this, R.id.reg_address_pin_code, "^[0-9]{6}$", R.string.errorMessagePinCode);
    }

    @Override
    public void onClick(View view) {
        final Intent[] intent = new Intent[1];
        switch (view.getId()) {
            case R.id.sign_in_button:
                arrow.setVisibility(View.GONE);
                linearLayoutProgressBar.setVisibility(View.VISIBLE);
                if (validation.validate()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (returnStatus == 0) {
                                createUser();
                            }
                        }
                    }, SPLASH_TIME_OUT);
                }
                break;
            case R.id.login_register_button:
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }

    private void createUser() {

        sFullName = editTextUserName.getText().toString().trim();
        sPhoneNumber = editTextMobile.getText().toString().trim();
        sSafeCode = editTextSafeCode.getText().toString().trim();
        sAddressLineOne = editTextAddressLineOne.getText().toString().trim();
        sAddressLineTwo = editTextAddressLineTwo.getText().toString().trim();
        sCity = editTextCity.getText().toString().trim();
        sState = editTextState.getText().toString().trim();
        sPinCode = editTextPinCode.getText().toString().trim();

        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put(USER_FIRST_NAME, sFullName);
            requestObject.put(USER_LAST_NAME, sFullName);
            requestObject.put(USER_PHONE_NUMBER, sPhoneNumber);
            requestObject.put(USER_SAFE_CODE, sSafeCode);

            requestObject.put(USER_ADDRESS_STREET, sAddressLineOne);
            requestObject.put(USER_ADDRESS_LINE_TWO, sAddressLineTwo);
            requestObject.put(USER_ADDRESS_CITY, sCity);
            requestObject.put(USER_ADDRESS_STATE, sState);
            requestObject.put(USER_ADDRESS_PIN_CODE, sPinCode);

            requestObject.put(SUPPLIER_ID, STATIC_SUPPLIER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = CONNECTION_URL + DATA_PATH;
        Log.v(TAG, url);
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        arrow.setVisibility(View.VISIBLE);
                        linearLayoutProgressBar.setVisibility(View.GONE);
                        try {
                            if ((jsonObject.getString("message")).equals("The phone number already exists")) {
                                Toast.makeText(RegisterActivity.this, "Phone number is already register.", Toast.LENGTH_SHORT).show();
                            } else {
                                parseJSONObject(jsonObject);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        arrow.setVisibility(View.VISIBLE);
                        linearLayoutProgressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Network Error ! " + volleyError, Toast.LENGTH_SHORT).show();
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

    private void parseJSONObject(JSONObject jsonObject) {
        Log.d(TAG, jsonObject + toString());
        SharedPreferences.Editor editor = sharedPreferencesStatus.edit();
        editor.putString(USER_FIRST_NAME, sFullName);
        editor.putString(USER_PHONE_NUMBER, sPhoneNumber);
        try {
            JSONObject jsonObject1 = jsonObject.getJSONArray("object").getJSONObject(0);
            editor.putString(USER_ID, jsonObject1.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putInt(STATUS, 0);
        editor.apply();
        startActivity(new Intent(this, OneTimePasswordActivity.class));
        finish();
    }
}
