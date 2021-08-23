package com.delivery.site;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.delivery.R;
import com.delivery.activity.authentication.OneTimePasswordActivity;
import com.delivery.fragment.task.Complete;
import com.delivery.utils.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_DEMAND;

public class otpUpdateActivity extends AppCompatActivity  implements View.OnClickListener {
    private SharedPreferences loginDetails;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private Button buttonAccountVerification;
    private String sMobileNumber, sEmailAddress,status,jsonObject;
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
        setContentView(R.layout.activity_otp_update);
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
        status=getIntent().getStringExtra("status");
        jsonObject=getIntent().getStringExtra("json");
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
                      Toast.makeText(otpUpdateActivity.this, "Invalid One Time Password", Toast.LENGTH_SHORT).show();
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
    private void parseJSONObject() {
        startActivity(new Intent(this, Complete.class));
        //showNotification();
    }
    public void OneTimePasswordVerificationRefund(String otpValue)
    {
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        String url=CONNECTION_URL_DEMAND+"api/v3/store/cart/pickup/refund?otp="+otpValue;
        try
        {
            final JSONObject j=new JSONObject(jsonObject);
            StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response.equals("true"))
                    {
                        Log.i("response",response);
                        parseJSONObject();
                    }
                    else
                    {
                        Toast.makeText(otpUpdateActivity.this,"Invalid OTP",Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(otpUpdateActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {

                    return  j.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            requestQueue.add(req);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    public void OneTimePasswordVerificationDelivery(String otpValue)
    {

        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        String url="http://awseb-AWSEB-1JA54ZOKWDPMR-1110084430.ap-south-1.elb.amazonaws.com/api/v3/store/cart/door-step/refund?otp="+otpValue;
        try
        {
           final JSONObject j=new JSONObject(jsonObject);
            StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                  if(response.equals("true"))
                  {
                      Log.i("response",response);
                      parseJSONObject();
                  }
                  else
                  {
                      Toast.makeText(otpUpdateActivity.this,"Invalid OTP",Toast.LENGTH_SHORT).show();
                  }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(otpUpdateActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {

                    return  j.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            requestQueue.add(req);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
