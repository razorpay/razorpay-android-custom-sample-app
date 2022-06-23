package com.razorpay.sampleapp.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.razorpay.sampleapp.R
import com.razorpay.sampleapp.kotlin.PaymentActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_kotlin).setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        findViewById<Button>(R.id.btn_java).setOnClickListener {
            startActivity(Intent(this, com.razorpay.sampleapp.java.PaymentActivity::class.java))
        }
    }
}