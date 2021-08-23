package com.delivery.fragment.task;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.delivery.HomeActivity;
import com.delivery.R;

public class Complete extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);
        ImageView mImgCheck = findViewById(R.id.imageView);
        ((Animatable) mImgCheck.getDrawable()).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Complete.this, HomeActivity.class));
                finish();
                finishAffinity();
            }
        }, SPLASH_TIME_OUT);

    }
}
