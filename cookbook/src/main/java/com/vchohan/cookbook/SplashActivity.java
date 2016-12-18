package com.vchohan.cookbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        mProgressView = (ProgressBar) findViewById(R.id.splash_progress_bar);

        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                setProgressBar();
                startApp();
                finish();
            }
        }).start();
    }

    private void setProgressBar() {
        for (int progress = 0; progress < 100; progress += 10) {
            try {
                Thread.sleep(500);
                mProgressView.setProgress(progress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startApp() {
        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
