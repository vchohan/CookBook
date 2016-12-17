package com.vchohan.cookbook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        showProgressDialog();

// METHOD 1
        /****** Create Thread that will sleep for 5 seconds *************/
        Thread background = new Thread() {
            public void run() {
                try {
                    // Thread will sleep for 5 seconds
                    sleep(3 * 1000);

                    // After 5 seconds redirect to another intent
                    Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                    //Remove activity
                    hideProgressDialog();
                    finish();

                } catch (Exception e) {

                }
            }
        };

        // start thread
        background.start();

//METHOD 2
        /*
        new Handler().postDelayed(new Runnable() {
            // Using handler with postDelayed called runnable run method
            @Override
            public void run() {
                    Intent loginIntent = new Intent(getBaseContext(), LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                // close this activity
                hideProgressDialog();
                finish();
            }
        }, 5*1000); // wait for 5 seconds
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
