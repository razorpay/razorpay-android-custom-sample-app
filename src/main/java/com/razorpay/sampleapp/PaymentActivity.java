package com.razorpay.sampleapp;

import android.app.Activity;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.json.JSONObject;

import com.razorpay.Razorpay;


public class PaymentActivity extends Activity{
    private Razorpay razorpay;
    private Activity activity;
    private WebView webview;
    private ViewGroup outerBox;
    private JSONObject payload;
    private static final String TAG = PaymentActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        setContentView(R.layout.activity_layout_payment);
        webview = (WebView) findViewById(R.id.payment_webview);
        outerBox = (ViewGroup) findViewById(R.id.outerbox);

        View button = findViewById(R.id.pay_btn);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(PaymentActivity.this, PaymentOptions.class);
                startActivity(intent);

            }

        });
    }
}
