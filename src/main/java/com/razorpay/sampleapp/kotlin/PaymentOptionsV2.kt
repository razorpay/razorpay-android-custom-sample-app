package com.razorpay.sampleapp.kotlin

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.razorpay.*
import com.razorpay.sampleapp.R
import org.json.JSONArray
import org.json.JSONObject

class PaymentOptionsV2 : AppCompatActivity() {

    private lateinit var btnCard: Button
    private lateinit var btnUpiIntent : Button
    private lateinit var btnUpiCollect : Button
    private lateinit var btnNetbanking: Button
    private lateinit var btnWallet: Button
    private lateinit var btnPay: Button
    private lateinit var etApiKey: EditText
    private lateinit var etOptionsPayload : EditText
    private lateinit var etVpa: EditText
    private lateinit var ivVpaValidate: ImageView
    private lateinit var webview: WebView
    private lateinit var clOuterBox : ConstraintLayout
    private lateinit var razorpay: Razorpay
    private lateinit var banksListAdapter : ArrayAdapter<String>
    private lateinit var walletListAdapter: ArrayAdapter<String>
    private lateinit var bankDialog: AlertDialog
    private lateinit var walletDialog: AlertDialog
    private var payload = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_options_v2)
        btnCard = findViewById(R.id.btn_card)
        btnUpiIntent = findViewById(R.id.btn_upi_intent)
        btnUpiCollect = findViewById(R.id.btn_upi_collect)
        btnNetbanking = findViewById(R.id.btn_netbanking)
        btnWallet = findViewById(R.id.btn_wallet)
        btnPay = findViewById(R.id.btn_pay)
        webview = findViewById(R.id.webview)
        clOuterBox = findViewById(R.id.cl_outer)

        etApiKey = findViewById(R.id.et_key)
        etOptionsPayload = findViewById(R.id.et_options_payload)

        etOptionsPayload.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    payload = JSONObject(s.toString())
                }
            }

        })

        etVpa = findViewById(R.id.et_vpa)
        ivVpaValidate = findViewById(R.id.iv_vpa)

        ivVpaValidate.setOnClickListener {
            if (etVpa.text.toString().isNotEmpty() && this::razorpay.isInitialized){
                razorpay.isValidVpa(etVpa.text.toString(), object :ValidateVpaCallback{
                    override fun onResponse(p0: JSONObject?) {
                        val alertDialog = AlertDialog.Builder(this@PaymentOptionsV2)
                        alertDialog.setMessage(p0.toString())
                        alertDialog.setTitle("VPA Validation Result")
                        alertDialog.show()
                    }

                    override fun onFailure() {

                    }

                })
            }
        }

        btnCard.setOnClickListener {
            basePayload()
            payload.put("method", "card")
            payload.put("card[number]", "4111111111111111")
            payload.put("card[expiry_month]", "12")
            payload.put("card[expiry_year]", "24")
            payload.put("card[cvv]", "123")
            payload.put("card[name]", "Razorpay")
            etOptionsPayload.setText(payload.toString(4))
        }
        btnUpiIntent.setOnClickListener {
            basePayload()

            val jArray = JSONArray()
            jArray.put("in.org.npci.upiapp")
            jArray.put("com.snapwork.hdfc")
            payload.put("method", "upi")
            payload.put("_[flow]", "intent")
            payload.put("preferred_apps_order", jArray)
            payload.put("other_apps_order", jArray)
            etOptionsPayload.setText(payload.toString(4))
        }
        btnUpiCollect.setOnClickListener {
            basePayload()

            payload.put("method","upi")
            payload.put("vpa","success@razorpay")
            etOptionsPayload.setText(payload.toString(4))

        }
        btnNetbanking.setOnClickListener {
            if (this::bankDialog.isInitialized) bankDialog.show()
        }
        btnWallet.setOnClickListener {
            if (this::walletDialog.isInitialized) walletDialog.show()
        }
        btnPay.setOnClickListener {
            sendRequest(true)
        }

        findViewById<Button>(R.id.btn_pay_data).setOnClickListener{
            sendRequest(false)
        }

        initRazorpay()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this::razorpay.isInitialized){
            razorpay.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun basePayload(){
        payload = JSONObject()
        payload.put("amount", 100)
        payload.put("currency", "INR")
        payload.put("contact", "9999999999")
        payload.put("email", "a@a.com")
    }

    private fun sendRequest(usePaymentResultListener: Boolean){


        if (this::razorpay.isInitialized){
            if (etApiKey.text.toString().isNotEmpty()){
                razorpay.changeApiKey(etApiKey.text.toString())
            }
            val dialog = AlertDialog.Builder(this@PaymentOptionsV2)
            dialog.setPositiveButton("Ok",object:DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {}
            })
            razorpay.validateFields(payload, object:ValidationListener{
                override fun onValidationSuccess() {
                    toggleWebViewVisibility(View.VISIBLE)
                    if (usePaymentResultListener){
                        razorpay.submit(payload, object: PaymentResultListener{
                            override fun onPaymentSuccess(p0: String?) {
                                p0?.let {
                                    toggleWebViewVisibility(View.GONE)
                                    dialog.setTitle("Payment Successful")
                                    dialog.setMessage(it)
                                    dialog.show()
                                }
                            }

                            override fun onPaymentError(p0: Int, p1: String?) {
                                toggleWebViewVisibility(View.GONE)
                                dialog.setTitle("Payment Failed")
                                dialog.setMessage(p1)
                                dialog.show()
                            }
                        })
                    }else{
                        razorpay.submit(payload, object :PaymentResultWithDataListener{
                            override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
                                p1?.let {
                                    toggleWebViewVisibility(View.GONE)
                                    dialog.setTitle("Payment Successful")
                                    dialog.setMessage(it.data.toString())
                                    dialog.show()
                                }
                            }

                            override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
                                p2?.let {
                                    toggleWebViewVisibility(View.GONE)
                                    dialog.setTitle("Payment Failed")
                                    dialog.setMessage(it.data.toString())
                                    dialog.show()
                                }
                            }

                        })
                    }

                }

                override fun onValidationError(p0: MutableMap<String, String>?) {
                    dialog.setMessage(p0.toString())
                    dialog.show()
                }

            })
        }
    }

    private fun initRazorpay(){
        razorpay = Razorpay(this)
        razorpay.getPaymentMethods(object:PaymentMethodsCallback{
            override fun onPaymentMethodsReceived(p0: String?) {
                val bankDialogBuilder = AlertDialog.Builder(this@PaymentOptionsV2)
                val walletDialogBuilder = AlertDialog.Builder(this@PaymentOptionsV2)
                val bankListLayout = LayoutInflater.from(this@PaymentOptionsV2).inflate(R.layout.banks_list,null, false)
                val bankListView = bankListLayout.findViewById<ListView>(R.id.list_view_bank)
                val walletListLayout = LayoutInflater.from(this@PaymentOptionsV2).inflate(R.layout.banks_list,null, false)
                val walletListView = walletListLayout.findViewById<ListView>(R.id.list_view_bank)
                p0?.let {
                    val bankKeys = ArrayList<String>()
                    val bankNames = ArrayList<String>()
                    val bankListJson = JSONObject(it).getJSONObject("netbanking")
                    val itr: Iterator<String>  = bankListJson.keys()
                    while (itr.hasNext()){
                        val key = itr.next()
                        bankKeys.add(key)
                        bankNames.add(bankListJson.getString(key))
                    }
                    banksListAdapter = ArrayAdapter(this@PaymentOptionsV2,android.R.layout.simple_list_item_1, bankNames)
                    bankListView.adapter = banksListAdapter
                    bankDialogBuilder.setView(bankListLayout)
                    bankDialogBuilder.setTitle("Select a bank")
                    bankDialogBuilder.setPositiveButton("Ok"
                    ) { dialog, which ->  }
                    bankDialog = bankDialogBuilder.create()
                    bankListView.setOnItemClickListener { parent, view, position, id ->
                        basePayload()
                        payload.put("method", "netbanking")
                        payload.put("bank", bankKeys[position])
                        etOptionsPayload.setText(payload.toString(4))
                        bankDialog.dismiss()

                    }
                    val walletNames = ArrayList<String>()
                    val walletsListJson = JSONObject(it).getJSONObject("wallet")
                    val walletItr = walletsListJson.keys()
                    while (walletItr.hasNext()){
                        val key = walletItr.next()
                        if (walletsListJson.getBoolean(key)) {
                            walletNames.add(key)
                        }
                    }
                    walletListAdapter = ArrayAdapter(this@PaymentOptionsV2, android.R.layout.simple_list_item_1, walletNames)
                    walletListView.adapter = walletListAdapter
                    walletDialogBuilder.setView(walletListLayout)
                    walletDialogBuilder.setTitle("Select a Wallet")
                    walletDialogBuilder.setPositiveButton("Ok"
                    ) { dialog, which ->  }
                    walletDialog = walletDialogBuilder.create()
                    walletListView.setOnItemClickListener { parent, view, position, id ->
                        basePayload()
                        payload.put("method", "wallet")
                        payload.put("wallet", walletNames[position])
                        etOptionsPayload.setText(payload.toString(4))
                        walletDialog.dismiss()
                    }
                }


            }

            override fun onError(p0: String?) {
                Toast.makeText(this@PaymentOptionsV2, p0,Toast.LENGTH_LONG).show()
            }
        })
        razorpay.setWebView(webview)
    }

    private fun toggleWebViewVisibility(webviewVisibility: Int){
        webview.visibility = webviewVisibility
        clOuterBox.visibility = if (webviewVisibility == View.VISIBLE){
            View.GONE
        }else{
            View.VISIBLE
        }
    }
}