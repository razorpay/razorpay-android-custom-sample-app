package com.razorpay.sampleapp;

import android.app.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import android.util.Log;

import com.razorpay.Razorpay;
import com.razorpay.RazorpayWebViewClient;
import com.razorpay.sampleapp.fragments.PaymentMethodFragment;

public class PaymentActivity extends Activity {
    private Razorpay razorpay;
    private WebView webview;
    private JSONObject payload;
    private Activity activity;
    PaymentMethods paymentMethods;
    RelativeLayout methodsLayout;
    ViewPager viewPager;
    Button payButton;
    boolean inPayment = true;
    PaymentMethodPagerAdapter paymentMethodPagerAdapter;


    public static final String TAG = PaymentActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_payment);
        viewPager = (ViewPager) findViewById(R.id.vpPager);
        payButton = (Button) findViewById(R.id.btn_pay);
        methodsLayout = (RelativeLayout) findViewById(R.id.rl_methods);
        initRazorpay();
        createWebView();
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePayment();
            }
        });
    }

    private void initRazorpay() {
        razorpay = new Razorpay(this) {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(activity, "Payment Successful: " + s, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(activity, "Error " + Integer.toString(i) + ": " + s, Toast.LENGTH_SHORT).show();
                Log.d("com.example", "onError: " + Integer.toString(i) + ": " + s);
                resetPayment();
            }

            @Override
            public void paymentMethodsCallback(String s) {
                paymentMethods = new PaymentMethods(s);
                paymentMethodPagerAdapter = new PaymentMethodPagerAdapter(getFragmentManager(), paymentMethods);
                viewPager.setAdapter(paymentMethodPagerAdapter);
            }
        };
        razorpay.getPaymentMethods();
    }

    private void createWebView() {

        webview = (WebView) activity.findViewById(R.id.payment_webview);

        /**
         * Set a custom webview client if required
         */
        razorpay.setWebviewClient(new RazorpayWebViewClient(razorpay) {
            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                Log.d(TAG, "Custom webview client onPageStarted");
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                Log.d(TAG, "Custom webview client onPageFinished");
            }
        });

        /**
         * You need to pass the webview to Razorpay
         */
        razorpay.setWebView(webview);
    }

    private void initiatePayment() {
        try {
            /**
             * Default INR
             * 3 a-zA-Z
             */
            payload = new JSONObject("{currency: 'INR'}");

            /**
             * REQUIRED
             * Amount is in paise
             * For eg, to charge Rs 100, you need to pass 10000
             * min Re 1, should be in paise, max 5L
             */
            payload.put("amount", "100");

            /**
             * REQUIRED
             * Customer contact number
             * ^+?[0-9]{10,15}$
             * Allow curvy brackets and space and -
             */
            payload.put("contact", "9999999999");

            /**
             * REQUIRED
             * Customer email id
             * max 255 characters
             * Should be a valid email address with "@" and at least one "."
             */
            payload.put("email", "customer@name.com");

            /**
             * REQUIRED
             * The payment method selected by the user
             * Possible values
             *  - card
             *  - netbanking
             *  - wallet
             */
            /**
             * REQUIRED if method = netbanking
             * The value of "bank" has to match the value provided in
             * paymentMethodsCallback()
             */
            //payload.put("bank", "HDFC");

            /**
             * If method = card
             *
             * payload.put("method", "card");
             *
             * payload.put("card[name]", "Customer Name");
             * a-zA-Z space dot
             *
             * payload.put("card[number]", "4111111111111111");
             * 0-9, luhn check, 13-19 digits
             *
             * payload.put("card[expiry_month]", "11"); in MM format
             * two digits, 01-12
             *
             * payload.put("card[expiry_year]", "22"); in YY format
             * future year
             *
             * payload.put("card[cvv]", "342");
             * 0-9, 3 digits for all except amex
             * 4 digits for amex, card starts with ^34 or ^37
             */

            /**
             * If method = wallet
             *
             * payload.put("method", "wallet");
             * payload.put("wallet", "wallet_name");
             *
             * wallet_name has to match the the value provided in
             * paymentMethodsCallback()
             */

            PaymentMethodFragment currentFragment = paymentMethodPagerAdapter.getCurrentFragment();
            payload.put("method", currentFragment.getMethod());
            payload = merge(payload, currentFragment.getMethodPayload());
            Log.d(TAG, payload.toString());
            /**
             * notes: 15 in total
             * description: 255 characters
             * order_id: alphanumeric, 14 characters after order_
             */

            razorpay.validateFields(payload, new Razorpay.ValidationListener() {
                @Override
                public void onValidationSuccess() {
                    try {
                        methodsLayout.setVisibility(View.GONE);
                        webview.setVisibility(View.VISIBLE);
                        razorpay.submit(payload);
                        inPayment = true;
                    } catch (Exception e) {
                        Log.e("com.example", "Exception", e);
                    }
                }

                @Override
                public void onValidationError(Map<String, String> validation) {
                    Log.d("com.example", "Validation failed: " + validation.get("field") + " " + validation.get("description"));
                    Toast.makeText(activity, "Validation: " + validation.get("field") + " " + validation.get("description"), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("com.example", "Exception", e);
        }
    }

    /**
     * Merge two JSONObjects
     * @param obj1
     * @param obj2
     */
    private JSONObject merge(JSONObject obj1, JSONObject obj2) throws JSONException {
        JSONObject mergedObj = new JSONObject();
        Iterator<String> it1 = obj1.keys();
        Iterator<String> it2 = obj2.keys();
        String key;
        while(it1.hasNext()) {
            key = it1.next();
            mergedObj.put(key, obj1.get(key));
        }
        while(it2.hasNext()) {
            key = it2.next();
            mergedObj.put(key, obj2.get(key));
        }
        return mergedObj;
    }

    @Override
    public void onBackPressed() {
        if(inPayment) {
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            razorpay.onBackPressed();
            resetPayment();
        }
        else {
            super.onBackPressed();
        }
    }

    public PaymentMethods getPaymentMethods() {
        return paymentMethods;
    }

    /**
     * Reset the payment
     * Hide the webview
     * and show methods layout
     */
    public void resetPayment(){
        inPayment = false;
        webview.setVisibility(View.GONE);
        methodsLayout.setVisibility(View.VISIBLE);
    }
}