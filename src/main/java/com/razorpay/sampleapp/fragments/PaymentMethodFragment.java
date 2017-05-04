package com.razorpay.sampleapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.razorpay.sampleapp.PaymentActivity;
import com.razorpay.sampleapp.PaymentMethods;

import org.json.JSONObject;

public abstract class PaymentMethodFragment extends Fragment {

    protected PaymentMethods paymentMethods;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paymentMethods = ((PaymentActivity) getActivity()).getPaymentMethods();
    }

    /**
     * Return method specific data
     * To be implemented by each method
     */
    public abstract JSONObject getMethodPayload();

    public abstract String getMethod();

}
