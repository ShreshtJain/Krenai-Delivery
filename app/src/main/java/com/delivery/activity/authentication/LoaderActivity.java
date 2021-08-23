package com.delivery.activity.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.delivery.HomeActivity;

import androidx.appcompat.app.AppCompatActivity;
import com.delivery.R;


public class LoaderActivity extends AppCompatActivity {
    private String email,name,mobile,password,address;
    private static final String loginStatus = "loginStatus";
    private SharedPreferences status;
    TextView textOne;
    TextView textTwo;
    TextView textThree;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_please_wait_account_creation);
        status = getSharedPreferences(loginStatus, Context.MODE_PRIVATE);
        showAnimation();
    }

    private void showAnimation() {
        textOne = (TextView) findViewById(R.id.text_1);
        textTwo = (TextView) findViewById(R.id.text_2);
        textThree = (TextView) findViewById(R.id.text_3);
        final Intent[] intent = new Intent[1];
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intent[0] = new Intent(LoaderActivity.this, HomeActivity.class);
                startActivity(intent[0]);
            }
        }, 3000);
        textAnimation();
    }


    public void textAnimation() {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.text_animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (count == 0) {
                    textOne.setVisibility(View.GONE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.GONE);
                    textTwo.startAnimation(myanim);
                    count = 1;
                    textAnimation();
                } else if (count == 1) {
                    textOne.setVisibility(View.GONE);
                    textTwo.setVisibility(View.GONE);
                    textThree.setVisibility(View.VISIBLE);
                    textThree.startAnimation(myanim);
                    count = 2;
                    textAnimation();

                } else if (count == 2) {
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.GONE);
                    textThree.setVisibility(View.GONE);
                    textOne.startAnimation(myanim);
                    count = 0;
                    textAnimation();
                }
            }
        }, 3000);
    }
}