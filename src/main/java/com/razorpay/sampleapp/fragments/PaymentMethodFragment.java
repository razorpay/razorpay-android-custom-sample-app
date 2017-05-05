package com.razorpay.sampleapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.razorpay.sampleapp.PaymentActivity;
import com.razorpay.sampleapp.PaymentMethods;

import org.json.JSONObject;

public abstract class PaymentMethodFragment extends Fragment {

    private PaymentMethods paymentMethods;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paymentMethods = ((PaymentActivity) getActivity()).getPaymentMethods();
    }

    protected PaymentMethods getPaymentMethods() {
        return paymentMethods;
    }

    /**
     * @return method specific data
     * To be implemented by each fragment
     */
    public abstract JSONObject getMethodPayload();

    /**
     * @return payment method
     */
    public abstract String getMethod();

}
