package com.razorpay.sampleapp;

import android.app.Activity;

import android.view.View;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
import com.razorpay.Razorpay;

public class PaymentActivity extends Activity {
  private Razorpay razorpay;
  private ViewGroup parent;
  private Activity activity;
  private RelativeLayout container;
  private WebView webview;
  private JSONObject payload;

  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    activity = this;

    parent = (ViewGroup) this.findViewById(android.R.id.content);

    initRZP();
    createWebView();

    /**
     * Through user action, initiate the payment
     */
    initiatePayment();
  }

  private void initRZP(){
    razorpay = new Razorpay(this, "rzp_live_ILgsfZCZoFIKMb"){
      /**
       * This method gets called once the payment methods have been fetched
       * For structure of result, refer:
       * Ask merchant to cache
       */
      public void paymentMethodsCallback(String result){
        /**
         * This returns JSON data
         * The structure of this data can be seen at the following link:
         * https://api.razorpay.com/v1/methods?key_id=rzp_test_1DP5mmOlF5G5ag
         *
         * Since network calls are async in android, payment methods are fetched async too
         * This method gets called after you have called getPaymentMethods()
         */
        Log.d("com.example", "paymentMethodsCallback " + result);
      }

      /**
       * Is called if the payment was successful
       * You can now destroy the webview
       */
      public void onSuccess(String razorpay_payment_id){
        Toast.makeText(activity, "Payment Successful: " + razorpay_payment_id, Toast.LENGTH_SHORT).show();
      }

      /**
       * Is called if the payment failed
       * possible values for code in this sdk are:
       * 2: in case of network error
       * 4: response parsing error
       * 5: This will contain meaningful message and can be shown to user
       *    Format: {"error": {"description": "Expiry year should be greater than current year"}}
       */
      public void onError(int code, String response){
        Toast.makeText(activity, "Error " + Integer.toString(code) + ": " + response, Toast.LENGTH_SHORT).show();
        Log.d("com.example", "onError: " + Integer.toString(code) + ": " + response);
      }
    };

    razorpay.getPaymentMethods();
  }

  /**
   * How you create webview, programmatically or otherwise,
   * Razorpay only needs the reference
   *
   * The settings shown below need to be set for payments to work
   */
  private void createWebView(){

    /**
     * Here we have created view programmatically
     */
    container = new RelativeLayout(this);
    container.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    container.setBackgroundColor(0xffffffff);
    parent.addView(container);

    webview = new WebView(this);

    WebSettings settings = webview.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setSaveFormData(false);
    webview.clearFormData();

    /**
     * Inserting WebView into view
     */
    webview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    container.addView(webview);

    /**
     * Even if you don't use any method from WebChromeClient
     * set this to enable JS Alerts on bank pages
     */
    webview.setWebChromeClient(new WebChromeClient(){});

    webview.setWebViewClient(new WebViewClient(){
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
      }

      /**
       * This is called usually in case of network errors
       */
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        razorpay.onError(2, description);
      }

      public void onPageFinished(WebView view, String url){
        razorpay.onPageFinished(view, url);
      }
    });

    /**
     * You need to pass the webview to Razorpay
     */
    razorpay.setWebView(webview);
  }

  private void initiatePayment(){
    try{
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
      payload.put("method", "netbanking");

      /**
       * REQUIRED if method = netbanking
       * The value of "bank" has to match the value provided in
       * paymentMethodsCallback()
       */
      payload.put("bank", "HDFC");

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
       * payload.put("card[name]", "Shashank Mehta");
       * payload.put("card[number]", "4111111111111111");
       * payload.put("card[expiry_month]", "12");
       * payload.put("card[expiry_year]", "20");
       * payload.put("card[cvv]", "100");
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


      /**
       * notes: 15 in total
       * description: 255 characters
       * order_id: alphanumeric, 14 characters after order_
       */

      Map<String, String> validation = razorpay.validateFields(payload);
      if(validation != null){
        Log.d("com.example", "Validation failed: " + validation.get("field") + " " + validation.get("description"));
        Toast.makeText(activity, "Validation: " + validation.get("field") + " " + validation.get("description"), Toast.LENGTH_SHORT).show();
      }
      else {
        razorpay.submit(payload);
      }
    }
    catch(Exception e){
      Log.d("com.example", "Exception: " + e.getMessage());
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    if(razorpay != null){
      razorpay.onBackPressed();
    }
  }

}