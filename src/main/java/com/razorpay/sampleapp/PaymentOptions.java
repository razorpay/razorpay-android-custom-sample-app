package com.razorpay.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.razorpay.PaymentResultListener;
import com.razorpay.Razorpay;
import com.razorpay.RazorpayWebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class PaymentOptions extends Activity implements PaymentResultListener {

    private FrameLayout frameLayout;
    private ListView listView;
    ArrayList<String> banksCodesList = new ArrayList<>();
    private ArrayList<String> banksList = new ArrayList<>();
    private ArrayList<String> walletsList = new ArrayList<>();
    private ArrayAdapter banksListAdapter;
    private ArrayAdapter walletsListAdapter;
    private Razorpay razorpay;
    private WebView webview;
    private ViewGroup outerBox;
    private static final String TAG = PaymentOptions.class.getSimpleName();
    private JSONObject payload;

    private class MethodSelected implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.card:
                    frameLayout.removeAllViews();
                    LayoutInflater.from(PaymentOptions.this).inflate(R.layout.fragment_payment_method_card, frameLayout, true);
                    findViewById(R.id.submit_card_details).setOnClickListener(new SubmitCardDetails());
                    razorpay.changeApiKey("rzp_test_1DP5mmOlF5G5ag");
                    break;

                case R.id.upi:
                    frameLayout.removeAllViews();
                    LayoutInflater.from(PaymentOptions.this).inflate(R.layout.fragment_payment_method_upi, frameLayout, true);
                    findViewById(R.id.btn_upi_collect_req).setOnClickListener(new SubmitUPICollectRequest());
                    findViewById(R.id.btn_upi_intent_flow).setOnClickListener(new SubmitUPIIntentDetails());
                    break;

                case R.id.netbanking:
                    frameLayout.removeAllViews();
                    LayoutInflater.from(PaymentOptions.this).inflate(R.layout.fragment_method_netbanking_wallet_list, frameLayout, true);
                    listView = (ListView) findViewById(R.id.method_available_options_list);
                    listView.setAdapter(banksListAdapter);
                    razorpay.changeApiKey("rzp_live_ILgsfZCZoFIKMb");
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            submitNetbankingDetails(banksCodesList.get(position));
                        }
                    });
                    break;

                case R.id.wallet:
                    frameLayout.removeAllViews();
                    LayoutInflater.from(PaymentOptions.this).inflate(R.layout.fragment_method_netbanking_wallet_list, frameLayout, true);
                    listView = (ListView) findViewById(R.id.method_available_options_list);
                    listView.setAdapter(walletsListAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            submitWalletDetails(walletsList.get(position));
                        }
                    });
                    break;

            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_options);

        findViewById(R.id.card).setOnClickListener(new MethodSelected());
        findViewById(R.id.upi).setOnClickListener(new MethodSelected());
        findViewById(R.id.netbanking).setOnClickListener(new MethodSelected());
        findViewById(R.id.wallet).setOnClickListener(new MethodSelected());

        webview = (WebView) findViewById(R.id.payment_webview);
        frameLayout = (FrameLayout) findViewById(R.id.frame);
        outerBox = (ViewGroup) findViewById(R.id.outerbox);

        LayoutInflater.from(PaymentOptions.this).inflate(R.layout.fragment_payment_method_card,
                frameLayout, true);
        banksListAdapter = new ArrayAdapter<String>(this, R.layout.text_view_list_banks_wallet, banksList);
        walletsListAdapter = new ArrayAdapter<String>(this, R.layout.text_view_list_banks_wallet, walletsList);

        initRazorpay();
        createWebView();
    }

    private void initRazorpay() {
        razorpay = new Razorpay(this);

        razorpay.getPaymentMethods(new Razorpay.PaymentMethodsCallback() {
            @Override
            public void onPaymentMethodsReceived(String result) {

                /**
                 * This returns JSON data
                 * The structure of this data can be seen at the following link:
                 * https://api.razorpay.com/v1/methods?key_id=rzp_test_1DP5mmOlF5G5ag
                 *
                 */
                Log.d("Result", "" + result);
                inflateLists(result);
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    private void createWebView() {
        /**
         * You need to pass the webview to Razorpay
         */
        razorpay.setWebView(webview);

        /**
         * Override the RazorpayWebViewClient for your custom hooks
         */
        razorpay.setWebviewClient(new RazorpayWebViewClient(razorpay) {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "Custom client onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Custom client onPageFinished");
            }
        });
    }

    private void inflateLists(String result) {
        try {
            JSONObject paymentMethods = new JSONObject(result);
            JSONObject banksListJSON = paymentMethods.getJSONObject("netbanking");
            JSONObject walletListJSON = paymentMethods.getJSONObject("wallet");

            Iterator<String> itr1 = banksListJSON.keys();
            while (itr1.hasNext()) {
                String key = itr1.next();
                banksCodesList.add(key);
                try {
                    banksList.add(banksListJSON.getString(key));
                } catch (JSONException e) {
                    Log.d("Reading Banks List", "" + e.getMessage());
                }
            }

            Iterator<String> itr2 = walletListJSON.keys();
            while (itr2.hasNext()) {
                String key = itr2.next();
                try {
                    if (walletListJSON.getBoolean(key)) {
                        walletsList.add(key);
                    }
                } catch (JSONException e) {
                    Log.d("Reading Wallets List", "" + e.getMessage());
                }
            }

            if (banksListAdapter != null) {
                banksListAdapter.notifyDataSetChanged();
            }

            if (walletsListAdapter != null) {
                walletsListAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.e("Parsing Result", "" + e.getMessage());
        }
    }

    public class SubmitCardDetails implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            EditText nameET = (EditText) findViewById(R.id.name);
            String name = nameET.getText().toString();

            EditText cardNumberET = (EditText) findViewById(R.id.cardNumber);
            String cardNumber = cardNumberET.getText().toString();

            EditText dateET = (EditText) findViewById(R.id.expiry);
            String date = dateET.getText().toString();
            int index = date.indexOf('/');
            String month = date.substring(0, index);
            String year = date.substring(index + 1);

            EditText cvvET = (EditText) findViewById(R.id.cvv);
            String cvv = cvvET.getText().toString();

            try {
                payload = new JSONObject("{currency: 'INR'}");
                payload.put("amount", "100");
                payload.put("contact", "9999999999");
                payload.put("email", "customer@name.com");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                payload.put("method", "card");
                payload.put("card[name]", name);
                payload.put("card[number]", cardNumber);
                payload.put("card[expiry_month]", month);
                payload.put("card[expiry_year]", year);
                payload.put("card[cvv]", cvv);
                sendRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class SubmitUPICollectRequest  implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            EditText vpaET = (EditText) findViewById(R.id.vpa);
            String vpa = vpaET.getText().toString();

            try {
                payload = new JSONObject("{currency: 'INR'}");
                payload.put("amount", "100");
                payload.put("contact", "9999999999");
                payload.put("email", "customer@name.com");
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                payload.put("method", "upi");
                payload.put("vpa", vpa);
                sendRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class SubmitUPIIntentDetails implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            EditText vpaET = (EditText) findViewById(R.id.vpa);
            String vpa = vpaET.getText().toString();

            try {
                payload = new JSONObject("{currency: 'INR'}");
                payload.put("amount", "100");
                payload.put("contact", "9999999999");
                payload.put("email", "customer@name.com");
                //payload.put("upi_app_package_name", "in.org.npci.upiapp");
                payload.put("display_logo", true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONArray jArray = new JSONArray();
                jArray.put("in.org.npci.upiapp");
                jArray.put("com.snapwork.hdfc");
                payload.put("method", "upi");
                payload.put("_[flow]", "intent");
                payload.put("preferred_apps_order", jArray);
                payload.put("other_apps_order", jArray);
                sendRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void submitNetbankingDetails(String bankName) {

        try {
            payload = new JSONObject("{currency: 'INR'}");
            payload.put("amount", "100");
            payload.put("contact", "9999999999");
            payload.put("email", "customer@name.com");
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            payload.put("method", "netbanking");
            payload.put("bank", bankName);
            sendRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitWalletDetails(String walletName) {


        try {
            payload = new JSONObject("{currency: 'INR'}");
            payload.put("amount", "100");
            payload.put("contact", "9999999999");
            payload.put("email", "customer@name.com");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            payload.put("method", "wallet");
            payload.put("wallet", walletName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendRequest();
    }

    private void sendRequest() {
        razorpay.validateFields(payload, new Razorpay.ValidationListener() {
            @Override
            public void onValidationSuccess() {
                try {
                    webview.setVisibility(View.VISIBLE);
                    outerBox.setVisibility(View.GONE);
                    razorpay.submit(payload, PaymentOptions.this);
                } catch (Exception e) {
                    Log.e("com.example", "Exception: ", e);
                }
            }

            @Override
            public void onValidationError(Map<String, String> error) {
                Log.d("com.example", "Validation failed: " + error.get("field") + " " + error.get("description"));
                Toast.makeText(PaymentOptions.this, "Validation: " + error.get("field") + " " + error.get("description"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        razorpay.onBackPressed();
        super.onBackPressed();
        webview.setVisibility(View.GONE);
        outerBox.setVisibility(View.VISIBLE);
    }

    /* callback for permission requested from android */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (razorpay != null) {
            razorpay.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Is called if the payment was successful
     * You can now destroy the webview
     */
    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        webview.setVisibility(View.GONE);
        outerBox.setVisibility(View.VISIBLE);
        Toast.makeText(PaymentOptions.this, "Payment Successful: " + razorpayPaymentId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Is called if the payment failed
     * possible values for code in this sdk are:
     * 2: in case of network error
     * 4: response parsing error
     * 5: This will contain meaningful message and can be shown to user
     * Format: {"error": {"description": "Expiry year should be greater than current year"}}
     */
    @Override
    public void onPaymentError(int errorCode, String errorDescription) {
        webview.setVisibility(View.GONE);
        outerBox.setVisibility(View.VISIBLE);
        Toast.makeText(PaymentOptions.this, "Error " + Integer.toString(errorCode) + ": " + errorDescription, Toast.LENGTH_SHORT).show();
        Log.d("com.example", "onError: " + Integer.toString(errorCode) + ": " + errorDescription);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        razorpay.onActivityResult(requestCode,resultCode,data);
    }

}
