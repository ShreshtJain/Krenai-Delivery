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
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.delivery.R;
import com.delivery.utils.firebase.FirebaseUpload;
import com.delivery.utils.network.VolleySingleton;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL;
import static com.delivery.utils.jkeys.Keys.CommonResources.FIREBASE_BUCKET;
import static com.delivery.utils.jkeys.Keys.CommonResources.FIREBASE_URL;
import static com.delivery.utils.jkeys.Keys.CommonResources.STATIC_SUPPLIER_ID;
import static com.delivery.utils.jkeys.Keys.LoginKeys.DATA_PATH_DOC_UPLOAD;
import static com.delivery.utils.jkeys.Keys.LoginKeys.STATUS;
import static com.delivery.utils.jkeys.Keys.LoginKeys.SUPPLIER_ID;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_CITY;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_LINE_TWO;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_PIN_CODE;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_STATE;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADDRESS_STREET;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ADHAR_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_FIRST_NAME;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_ID;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_LAST_NAME;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_LICENCE_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_PHONE_NUMBER;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_PROFILE_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.LoginKeys.USER_SAFE_CODE;

public class DocumentUploadActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DocumentUploadActivity.class.getSimpleName();
    private int PICK_IMAGE_REQUEST = 1;
    private int ADHAR_IMAGE_REQUEST = 3;
    private int PICK_DRIVING_LICENCE_REQUEST = 2;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private Uri filePath;
    private Bitmap bitmap;
    private String imageUserProfile, imageUserLicence, imageUserAdharCardString;
    private LinearLayout linearLayoutProfileImageUpload, linearLayoutDrivingLicenceUpload, linearLayoutAdharCardUpload;
    private Button buttonImageUpload;
    private ImageView imageViewProfileImage, imageViewDrivingLicence, imageUserAdharCard;
    private static int SPLASH_TIME_OUT = 500;
    private int returnStatus = 0;
    private AwesomeValidation validation;
    private ImageView arrow;
    private EditText editTextUserName,
            editTextAddressLineOne, editTextAddressLineTwo, editTextCity,
            editTextState, editTextPinCode;
    private String sFullName, sAddressLineOne, sAddressLineTwo,
            sCity, sState, sPinCode, mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_upload);
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        linearLayoutDrivingLicenceUpload = findViewById(R.id.driving_licence_image_layout);
        linearLayoutProfileImageUpload = findViewById(R.id.profile_image_upload_layout);
        linearLayoutAdharCardUpload = findViewById(R.id.aadhar_card_image_layout);

        imageViewProfileImage = findViewById(R.id.profile_image_user);
        imageViewDrivingLicence = findViewById(R.id.driving_licence_image);
        imageUserAdharCard = findViewById(R.id.aadhar_licence_image);

        linearLayoutProfileImageUpload.setOnClickListener(this);
        linearLayoutDrivingLicenceUpload.setOnClickListener(this);
        linearLayoutAdharCardUpload.setOnClickListener(this);
        Bundle extra = getIntent().getExtras();
        mobile = extra.getString("phone");
        registerFrom();
    }

    public void showNotification() {

        LayoutInflater inflater = this.getLayoutInflater();
        View viewAccountVerification = inflater.inflate(R.layout.custom_popup_account_verify, null);
        Button buttonGetStarted = viewAccountVerification.findViewById(R.id.get_started);
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DocumentUploadActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(viewAccountVerification);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showFileChooser(int type) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Long timeMillis = System.currentTimeMillis();
                FirebaseUpload firebaseUpload = new FirebaseUpload(this);
                boolean status = firebaseUpload.uploadImage(filePath, timeMillis, "Profile/" + sharedPreferencesStatus.getString(USER_PHONE_NUMBER, "00000000"));
                if (status) {
                    Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    imageUserProfile = FIREBASE_URL + FIREBASE_BUCKET + "Profile%2F" + sharedPreferencesStatus.getString(USER_PHONE_NUMBER, "00000000") + "%2F" + timeMillis;
                    imageViewProfileImage.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == PICK_DRIVING_LICENCE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Long timeMillis = System.currentTimeMillis();
                FirebaseUpload firebaseUpload = new FirebaseUpload(this);
                boolean status = firebaseUpload.uploadImage(filePath, timeMillis, "Licence/" + sharedPreferencesStatus.getString(USER_PHONE_NUMBER, "00000000"));
                if (status) {
                    Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    imageUserLicence = FIREBASE_URL + FIREBASE_BUCKET + "Licence%2F" + sharedPreferencesStatus.getString(USER_PHONE_NUMBER, "00000000") + "%2F" + timeMillis;
                    imageViewDrivingLicence.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == ADHAR_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            System.out.println("REQUEST CODE" + filePath);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Long timeMillis = System.currentTimeMillis();
                FirebaseUpload firebaseUpload = new FirebaseUpload(this);
                boolean status = firebaseUpload.uploadImage(filePath, timeMillis, "Adhar/" + sharedPreferencesStatus.getString(USER_PHONE_NUMBER, "00000000"));
                if (status) {
                    Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    imageUserAdharCardString = FIREBASE_URL + FIREBASE_BUCKET + "Adhar%2F" + sharedPreferencesStatus.getString(USER_PHONE_NUMBER, "00000000") + "%2F" + timeMillis;
                    imageUserAdharCard.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.driving_licence_image_layout:
                showFileChooser(PICK_DRIVING_LICENCE_REQUEST);
                break;
            case R.id.profile_image_upload_layout:
                showFileChooser(PICK_IMAGE_REQUEST);
                break;
            case R.id.aadhar_card_image_layout:
                showFileChooser(ADHAR_IMAGE_REQUEST);
                break;
            case R.id.upload_button:
                if (validation.validate()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (returnStatus == 0) {
                                userDocumentUpload();
                            }
                        }
                    }, SPLASH_TIME_OUT);
                }
                break;
        }
    }

    private void registerFrom() {
        validation = new AwesomeValidation(ValidationStyle.COLORATION);
        buttonImageUpload = findViewById(R.id.upload_button);
        arrow = findViewById(R.id.arrow);
        buttonImageUpload.setOnClickListener(this);
        editTextUserName = findViewById(R.id.reg_name);

        editTextAddressLineOne = findViewById(R.id.reg_address_line_first);
        editTextAddressLineTwo = findViewById(R.id.reg_address_line_second);
        editTextCity = findViewById(R.id.reg_city);
        editTextState = findViewById(R.id.reg_state);
        editTextPinCode = findViewById(R.id.reg_address_pin_code);
        validation.addValidation(this, R.id.reg_name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.errorMessageName);
        validation.addValidation(this, R.id.reg_city, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.errorMessageCity);
        validation.addValidation(this, R.id.reg_state, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.errorMessageState);
        validation.addValidation(this, R.id.reg_address_pin_code, "^[0-9]{6}$", R.string.errorMessagePinCode);
    }

    private void userDocumentUpload() {
        sFullName = editTextUserName.getText().toString().trim();
        sAddressLineOne = editTextAddressLineOne.getText().toString().trim();
        sAddressLineTwo = editTextAddressLineTwo.getText().toString().trim();
        sCity = editTextCity.getText().toString().trim();
        sState = editTextState.getText().toString().trim();
        sPinCode = editTextPinCode.getText().toString().trim();
        String userId = sharedPreferencesStatus.getString(USER_ID, "");
        int uId = (int) Float.parseFloat(userId);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put(USER_FIRST_NAME, sFullName);
            requestObject.put(USER_LAST_NAME, sFullName);
            requestObject.put(USER_PHONE_NUMBER, mobile);
            requestObject.put("id", uId);
            requestObject.put(USER_ADDRESS_STREET, sAddressLineOne);
            requestObject.put(USER_ADDRESS_LINE_TWO, sAddressLineTwo);
            requestObject.put(USER_ADDRESS_CITY, sCity);
            requestObject.put(USER_ADDRESS_STATE, sState);
            requestObject.put(USER_ADDRESS_PIN_CODE, sPinCode);
            requestObject.put(USER_PROFILE_IMAGE_URL, imageUserProfile);
            requestObject.put(USER_LICENCE_IMAGE_URL, imageUserLicence);
            requestObject.put(USER_ADHAR_IMAGE_URL, imageUserLicence);
            requestObject.put(SUPPLIER_ID, STATIC_SUPPLIER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = CONNECTION_URL + DATA_PATH_DOC_UPLOAD;
        Log.v(TAG, url);
        Log.v(TAG, requestObject.toString());
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        parseJSONObject();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(DocumentUploadActivity.this, "Network Error !", Toast.LENGTH_SHORT).show();
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

    private void parseJSONObject() {
        SharedPreferences.Editor editor = sharedPreferencesStatus.edit();
        editor.apply();
        Toast.makeText(this, "PROFILE UPDATED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
        finish();
    }
}
