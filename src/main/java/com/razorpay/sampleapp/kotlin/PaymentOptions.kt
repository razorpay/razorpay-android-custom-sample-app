package com.razorpay.sampleapp.kotlin

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.razorpay.*
import com.razorpay.sampleapp.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PaymentOptions : Activity(), PaymentResultListener {

    var webView: WebView? = null
    private var frameLayout: FrameLayout? = null
    var outerBox: ViewGroup? = null
    var listView: ListView? = null
    var bankCodesList = ArrayList<String>()
    var banksList = ArrayList<String>()
    var walletsList = ArrayList<String>()
    var banksListAdapter: ArrayAdapter<String>? = null
    var walletsListAdapter: ArrayAdapter<String>? = null
    var razorpay: Razorpay? = null
    var payload = JSONObject("{currency:'INR'}")
    val context = this


    val TAG: String = PaymentOptions::class.toString()


    private class MethodSelected : View.OnClickListener {
        override fun onClick(p0: View?) {
            Log.d("ID","CLICK")
            when (p0?.id) {
                R.id.card -> {
                    PaymentOptions().frameLayout?.removeAllViews()
                    LayoutInflater.from(PaymentOptions().context).inflate(R.layout.fragment_payment_method_card, PaymentOptions().frameLayout, true)
                    PaymentOptions().context.findViewById<Button>(R.id.submit_card_details).setOnClickListener { SubmitCardDetails() }
                }
                R.id.upi -> {
                    PaymentOptions().frameLayout?.removeAllViews()
                    LayoutInflater.from(PaymentOptions().context).inflate(R.layout.fragment_payment_method_upi,PaymentOptions().frameLayout,true)
                    PaymentOptions().context.findViewById<Button>(R.id.btn_upi_collect_req).setOnClickListener { SubmitUPICollectRequest() }
                    PaymentOptions().context.findViewById<Button>(R.id.btn_upi_intent_flow).setOnClickListener { SubmitUPIIntentDetails() }

                }
                R.id.netbanking -> {
                    PaymentOptions().frameLayout?.removeAllViews()
                    LayoutInflater.from(PaymentOptions().context).inflate(R.layout.fragment_method_netbanking_wallet_list, PaymentOptions().frameLayout, true)
                    PaymentOptions().listView = PaymentOptions().context.findViewById<View>(R.id.method_available_options_list) as ListView
                    PaymentOptions().listView?.adapter = PaymentOptions().banksListAdapter
                    PaymentOptions().razorpay?.changeApiKey("rzp_live_ILgsfZCZoFIKMb")
                    PaymentOptions().listView?.setOnItemClickListener { parent, view, position, id ->
                        run {
                            PaymentOptions().submitNetbankingDetails(PaymentOptions().bankCodesList[position])
                        }
                    }
                }
                R.id.wallet -> {
                    PaymentOptions().frameLayout?.removeAllViews()
                    LayoutInflater.from(PaymentOptions().context).inflate(R.layout.fragment_method_netbanking_wallet_list,PaymentOptions().frameLayout,true)
                    PaymentOptions().listView = PaymentOptions().context.findViewById(R.id.method_available_options_list)
                    PaymentOptions().listView?.adapter = PaymentOptions().walletsListAdapter
                    PaymentOptions().listView?.setOnItemClickListener { parent, view, position, id ->
                        run{
                            PaymentOptions().submitWalletDetails(PaymentOptions().walletsList[position])
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_options)

        findViewById<TextView>(R.id.card).setOnClickListener { MethodSelected() }
        findViewById<TextView>(R.id.upi).setOnClickListener { MethodSelected() }
        findViewById<TextView>(R.id.netbanking).setOnClickListener {
            frameLayout?.removeAllViews()
            LayoutInflater.from(this).inflate(R.layout.fragment_method_netbanking_wallet_list, PaymentOptions().frameLayout, true)

            listView = findViewById(R.id.method_available_options_list)
            listView?.adapter = banksListAdapter

            razorpay?.changeApiKey("rzp_live_ILgsfZCZoFIKMb")
            listView?.setOnItemClickListener { parent, view, position, id ->
                run {
                    PaymentOptions().submitNetbankingDetails(PaymentOptions().bankCodesList[position])
                }
            }
        }
        findViewById<TextView>(R.id.wallet).setOnClickListener { MethodSelected() }

        webView = findViewById(R.id.payment_webview)
        frameLayout = findViewById(R.id.frame)
        outerBox = findViewById(R.id.outerbox)

        LayoutInflater.from(this).inflate(R.layout.fragment_payment_method_card, frameLayout, true)
        banksListAdapter = ArrayAdapter(this, R.layout.text_view_list_banks_wallet, banksList)
        walletsListAdapter = ArrayAdapter(this, R.layout.text_view_list_banks_wallet, walletsList)

        initRazorpay()
        createWebView()

    }


    private fun createWebView() {
        razorpay?.setWebView(webView)
    }

    private fun initRazorpay() {
        razorpay = Razorpay(this)

        razorpay?.getPaymentMethods(object : PaymentMethodsCallback {
            override fun onPaymentMethodsReceived(result: String?) {
                /**
                 * This returns JSON data
                 * The structure of this data can be seen at the following link:
                 * https://api.razorpay.com/v1/methods?key_id=rzp_test_1DP5mmOlF5G5ag
                 *
                 */
                Log.d("Result", "" + result)
                inflateLists(result)
            }

            override fun onError(error: String?) {
                if (error != null) {
                    Log.e("Get Payment error", error)
                }
            }
        })

        razorpay?.isValidVpa("stambatgr5@okhdfcbank", object : ValidateVpaCallback {
            override fun onResponse(p0: JSONObject?) {
            }

            override fun onFailure() {
                Toast.makeText(this@PaymentOptions, "Error validating VPA", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun inflateLists(result: String?) {
        try {
            val paymentMethods = JSONObject(result)
            val banksListJSON = paymentMethods.getJSONObject("netbanking")
            val walletListJSON = paymentMethods.getJSONObject("wallet")

            val itr1: Iterator<String> = banksListJSON.keys()
            while (itr1.hasNext()) {
                val key = itr1.next()
                bankCodesList.add(key)
                try {
                    banksList.add(banksListJSON.getString(key))
                } catch (jsonException: JSONException) {
                    Log.e("Reading Banks List", jsonException.localizedMessage)
                }
            }

            val itr2: Iterator<String> = walletListJSON.keys()
            while (itr2.hasNext()) {
                val key = itr2.next()
                try {
                    if (walletListJSON.getBoolean(key)) {
                        walletsList.add(key)
                    }
                } catch (jsonException: JSONException) {
                    Log.e("Reading Wallets List", jsonException.localizedMessage)
                }
            }

            banksListAdapter?.notifyDataSetChanged()
            walletsListAdapter?.notifyDataSetChanged()

        } catch (e: Exception) {
            Log.e("Parsing Result", e.localizedMessage)
        }
    }

    public class SubmitCardDetails : View.OnClickListener {

        override fun onClick(view: View?) {
            val context = PaymentOptions().context

            val etName = context.findViewById<EditText>(R.id.name)
            val name = etName.text.toString()

            val etCardNumber = context.findViewById<EditText>(R.id.cardNumber)
            val cardNumber = etCardNumber.text.toString()

            val etDate = context.findViewById<EditText>(R.id.expiry)
            val date = etDate.text.toString()

            val index = date.indexOf('/')
            val month = date.substring(0, index)
            val year = date.substring(index + 1)

            val etCvv = context.findViewById<EditText>(R.id.cvv)
            val cvv = etCvv.text.toString()
            val payload = PaymentOptions().payload
            try {
                payload.put("amount", "100")
                payload.put("contact", "9999999999")
                payload.put("email", "customer@name.com")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                payload.put("method", "card")
                payload.put("card[name]", name)
                payload.put("card[number]", cardNumber)
                payload.put("card[expiry_month]", month)
                payload.put("card[expiry_year]", year)
                payload.put("card[cvv]", cvv)
                PaymentOptions().sendRequest()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }

    public class SubmitUPICollectRequest : View.OnClickListener{
        override fun onClick(view: View?) {
            val context = PaymentOptions().context
            val etVpa = context.findViewById<EditText>(R.id.vpa)
            val vpa = etVpa.text.toString()
            var payload = PaymentOptions().payload
            try{
                payload = JSONObject("{currency: 'INR'}")
                payload.put("amount", "100")
                payload.put("contact", "9999999999")
                payload.put("email", "customer@name.com")
            }catch (e: Exception){
                e.printStackTrace()
            }

            try{
                payload.put("method","upi")
                payload.put("vpa",vpa)
                PaymentOptions().sendRequest()
            }catch (e: Exception){
                e.printStackTrace()
            }

        }

    }

    class SubmitUPIIntentDetails : View.OnClickListener {
        override fun onClick(view: View) {
            val context = PaymentOptions().context
            val vpaET = context.findViewById<View>(R.id.vpa) as EditText
            val vpa = vpaET.text.toString()
            val payload = PaymentOptions().payload
            try {
                payload.put("amount", "111")
                payload.put("contact", "9999999999")
                payload.put("email", "customer@name.com")
                //payload.put("upi_app_package_name", "com.google.android.apps.nbu.paisa.user");
                payload.put("display_logo", true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val jArray = JSONArray()
                jArray.put("in.org.npci.upiapp")
                jArray.put("com.snapwork.hdfc")
                //payload.put("key_id","rzp_test_kEVtCVFWAjUQPG");
                payload.put("method", "upi")
                payload.put("_[flow]", "intent")
                payload.put("preferred_apps_order", jArray)
                payload.put("other_apps_order", jArray)
                PaymentOptions().sendRequest()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitNetbankingDetails(bankName: String?) {
        try {
            payload = JSONObject("{currency: 'INR'}")
            payload.put("amount", "100")
            payload.put("contact", "9999999999")
            payload.put("email", "customer@name.com")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            payload.put("method", "netbanking")
            payload.put("bank", bankName)
            sendRequest()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun submitWalletDetails(walletName: String?) {
        try {
            payload = JSONObject("{currency: 'INR'}")
            payload.put("amount", "100")
            payload.put("contact", "9999999999")
            payload.put("email", "customer@name.com")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            payload.put("method", "wallet")
            payload.put("wallet", walletName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sendRequest()
    }

    private fun sendRequest() {
        razorpay?.validateFields(payload, object : ValidationListener {
            override fun onValidationError(error: MutableMap<String, String>?) {
                Log.d(TAG, "Validation failed: " + error?.get("field"))
                Toast.makeText(this@PaymentOptions, "Validation: " + error?.get("field"), Toast.LENGTH_LONG).show()
            }

            override fun onValidationSuccess() {
                try {
                    webView?.visibility = View.VISIBLE
                    outerBox?.visibility = View.GONE
                    razorpay?.submit(payload, this@PaymentOptions)
                } catch (e: Exception) {
                    Log.e(TAG, "Excpetion: ", e)
                    e.printStackTrace()
                }
            }

        })
    }

    override fun onBackPressed() {
        razorpay?.onBackPressed()
        super.onBackPressed()
        webView?.visibility = View.GONE
        outerBox?.visibility = View.VISIBLE
    }

    /**
     * Is called if the payment failed
     * possible values for code in this sdk are:
     * 2: in case of network error
     * 4: response parsing error
     * 5: This will contain meaningful message and can be shown to user
     * Format: {"error": {"description": "Expiry year should be greater than current year"}}
     */
    override fun onPaymentError(errorCode: Int, errorDescription: String?) {
        webView?.visibility = View.GONE
        outerBox?.visibility = View.VISIBLE
        Toast.makeText(this@PaymentOptions, "Error $errorCode : $errorDescription",Toast.LENGTH_LONG).show()
        Log.e(TAG,"onError: $errorCode : $errorDescription")
    }

    /**
     * Is called if the payment was successful
     * You can now destroy the webview
     */
    override fun onPaymentSuccess(rzpPaymentId: String?) {
        webView?.visibility = View.GONE
        outerBox?.visibility = View.VISIBLE
        Toast.makeText(this@PaymentOptions, "Payment Successful: $rzpPaymentId",Toast.LENGTH_LONG).show()
    }
}
































































