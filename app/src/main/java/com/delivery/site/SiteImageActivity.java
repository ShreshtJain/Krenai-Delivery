package com.delivery.site;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.delivery.R;
import com.delivery.fragment.task.Complete;
import com.delivery.utils.firebase.FirebaseUpload;
import com.delivery.utils.jkeys.Keys;
import com.delivery.utils.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static com.delivery.utils.jkeys.Keys.CommonResources.CONNECTION_URL_SITE_MANAGER;
import static com.delivery.utils.jkeys.Keys.CommonResources.FIREBASE_BUCKET;
import static com.delivery.utils.jkeys.Keys.CommonResources.FIREBASE_URL;
import static com.delivery.utils.jkeys.Keys.LoginKeys.TOKEN;
import static com.delivery.utils.jkeys.Keys.SiteManager.DATA_PATH_SITE_UPDATE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.SiteManager.SITE_LONGITUDE;

public class SiteImageActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean cameraStatus;
    private ImageView imageViewCaptureSiteShot, imageViewCloseButton;
    private Button upload_siteshot, imageViewSiteShotReload;
    private LinearLayout linearLayoutSiteShotUpload, linearLayoutUploadSuccess;
    private TextureView textureView;
    private ImageReader reader;
    private ProgressBar loading;
    private int statusUpload = 0;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final String loginStatus = "loginStatus";
    private SharedPreferences sharedPreferencesStatus;
    private String imageUrl;
    FirebaseUser user;
    private FirebaseAuth mAuth;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
    }

    private String cameraId, currentAddress;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private String siteId;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Uri siteShotUriFull, siteShotUriFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_image);
        sharedPreferencesStatus = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        updateScreenResolution();
        getBundle();
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        //Take Picture Button
        imageViewCaptureSiteShot = findViewById(R.id.btn_take_picture);
        assert imageViewCaptureSiteShot != null;
        imageViewCaptureSiteShot.setOnClickListener(this);
        //Refresh Camera Object Button
        imageViewSiteShotReload = findViewById(R.id.site_shot_reload_data);
        assert imageViewSiteShotReload != null;
        imageViewSiteShotReload.setOnClickListener(this);

        //Upload Final Image Button
        linearLayoutSiteShotUpload = findViewById(R.id.upload_image);
        linearLayoutUploadSuccess = findViewById(R.id.upload_success);
        imageViewCloseButton = findViewById(R.id.close_button);
        imageViewCloseButton.setOnClickListener(this);
        loading = findViewById(R.id.progress_bar);
        upload_siteshot = findViewById(R.id.upload_siteshot);
        upload_siteshot.setOnClickListener(this);
    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            siteId = bundle.getString("site_id");
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 0;
            int height = 0;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(SiteImageActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
            if (captureListener != null) {
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;

                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        //save(bytes);
                    }

                    private void save(byte[] bytes) throws IOException {
                        OutputStream output = null;
                        try {
                            output = new FileOutputStream(file);
                            output.write(bytes);
                        } finally {
                            if (null != output) {
                                output.close();
                            }
                        }
                    }
                };
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            }


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(SiteImageActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(Keys.Log.LOG_TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(Keys.Log.LOG_TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(Keys.Log.LOG_TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onClick(View view) {
        FileOutputStream outputPhoto = null;
        switch (view.getId()) {
            case R.id.btn_take_picture:
                statusUpload = 0;
                Date date = new Date();
                //  String direction = new CompassDirection().onSensorChanged();
                Timestamp ts = new Timestamp(date.getTime());
                linearLayoutSiteShotUpload.setVisibility(View.VISIBLE);
                linearLayoutSiteShotUpload.animate().translationY(0);
                takePicture();
                Bitmap bitmap = textureView.getBitmap();
                if (isReadStoragePermissionGranted() && isWriteStoragePermissionGranted())
                    siteShotUriFull = getImageUri(this, bitmap);
                siteShotUriFocus = getImageUri(this, resizeImage(bitmap));
                break;
            case R.id.site_shot_reload_data:
                statusUpload = 0;
                linearLayoutSiteShotUpload.setVisibility(View.GONE);
                startBackgroundThread();
                if (textureView.isAvailable()) {
                    openCamera();
                } else {
                    textureView.setSurfaceTextureListener(textureListener);
                }
                break;
            case R.id.upload_siteshot:
                siteShotUpload();
                break;
            case R.id.close_button:
                finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2:
                Log.d("STORAGE", "External storage2");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                break;

            case 3:
                Log.d("STORAGE", "External storage1");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // close the app
                    Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void siteShotUpload() {
        if (statusUpload == 0) {
            statusUpload = 1;
            loading.setVisibility(View.VISIBLE);
            FirebaseUpload firebaseUpload = new FirebaseUpload(this);
            Long timeMillis = System.currentTimeMillis();
            boolean responseFullImageUpload = firebaseUpload.uploadImage(siteShotUriFull, timeMillis, "SiteMedia");
            imageUrl = FIREBASE_URL + FIREBASE_BUCKET + "site_manager%2F" + user.getUid() + "%2F" + timeMillis;
            if (responseFullImageUpload) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //linearLayoutUploadSuccess.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                        if (siteId != null) {
                            updateSite();
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("Image", String.valueOf(siteShotUriFull));
                            intent.putExtra("imageUrl", String.valueOf(imageUrl));
                            setResult(2, intent);
                            finish();
                        }
                        startBackgroundThread();
                        if (textureView.isAvailable()) {
                            openCamera();
                        } else {
                            textureView.setSurfaceTextureListener(textureListener);
                        }
                    }
                }, 1500);
            } else {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
                upload_siteshot.setVisibility(View.VISIBLE);
            }
            linearLayoutUploadSuccess.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Image Already Upaded Take New One", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private void updateScreenResolution() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, Environment.getExternalStorageDirectory() + "siteshot/pic.jpg", null);
        return Uri.parse(path);
    }

    public Bitmap resizeImage(Bitmap bitmap) {
        int x0 = bitmap.getWidth() / 2;
        int y0 = bitmap.getHeight() / 2;
        int dx = bitmap.getHeight() / 3;
        int dy = bitmap.getHeight() / 3;
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x0 - 300, y0 - dy, x0 + 280, y0 + 280, matrix, true);
        return croppedBitmap;
    }


    private void updateSite() {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put(SITE_LATITUDE, "");
            requestObject.put(SITE_LONGITUDE, "");
            requestObject.put(SITE_IMAGE_URL, imageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = CONNECTION_URL_SITE_MANAGER + DATA_PATH_SITE_UPDATE + siteId;
        final RequestQueue requestQueue = VolleySingleton.getsInstance().getMyRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH, url, requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        parseJSONObject(jsonObject);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse.statusCode == 409) {
                            Toast.makeText(SiteImageActivity.this, "Unknown Error.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SiteImageActivity.this, "Network Error !", Toast.LENGTH_SHORT).show();
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
}