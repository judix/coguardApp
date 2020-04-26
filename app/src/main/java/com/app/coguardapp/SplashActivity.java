package com.app.coguardapp;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_SCREEN =1500;
    ImageView imageView,imageView2;
    Animation left, right;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);



        imageView = findViewById(R.id.image1);
        imageView2 = findViewById(R.id.image2);


        left = AnimationUtils.loadAnimation(this,R.anim.left);
        right = AnimationUtils.loadAnimation(this, R.anim.right);
        imageView.setAnimation(right);
        imageView2.setAnimation(left);


        new Handler().postDelayed(() -> {
            if (checkInternetConnection()){
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        },SPLASH_SCREEN);



    }

    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected) {
            return true;
        } else {

            View view = findViewById(R.id.liner);
            Snackbar snackbar = Snackbar.make(view,"Bağlantınızı kontrol edin",Snackbar.LENGTH_LONG);

            snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);


            snackbar.setAction("Tekrar dene", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkInternetConnection()){
                        snackbar.dismiss();
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else
                        snackbar.show();
                }
            });
            //Eklediğimiz action'ın text rengini değiştirebiliriz
            snackbar.setActionTextColor(getResources().getColor(android.R.color.black));

            //Arkaplan rengini değiştirme
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.color));
            snackbar.show();



            return false;
        }

    }
}

