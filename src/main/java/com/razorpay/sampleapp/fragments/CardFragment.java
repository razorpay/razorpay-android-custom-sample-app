package com.razorpay.sampleapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.razorpay.sampleapp.PaymentActivity;
import com.razorpay.sampleapp.R;

import org.json.JSONObject;

public class CardFragment extends PaymentMethodFragment {

    EditText cardNumberEt;
    EditText cardNameEt;
    EditText cardCvvEt;
    EditText cardExpiryMonthEt;
    EditText cardExpiryYearEt;

    @Override
    public JSONObject getMethodPayload() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("card[name]", cardNameEt.getText().toString());
            payload.put("card[number]", cardNumberEt.getText().toString());
            payload.put("card[cvv]", cardCvvEt.getText().toString());
            payload.put("card[expiry_month]", cardExpiryMonthEt.getText().toString());
            payload.put("card[expiry_year]", cardExpiryYearEt.getText().toString());
            return payload;
        } catch (Exception e) {
            Log.e(PaymentActivity.TAG, "Error creating payload", e);
            // return empty payload
            return  new JSONObject();
        }
    }

    @Override
    public String getMethod() {
        return "card";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, null);
        cardNameEt = (EditText) view.findViewById(R.id.et_card_name);
        cardNumberEt = (EditText) view.findViewById(R.id.et_card_number);
        cardCvvEt = (EditText) view.findViewById(R.id.et_card_cvv);
        cardExpiryYearEt = (EditText) view.findViewById(R.id.et_card_expiry_year);
        cardExpiryMonthEt = (EditText) view.findViewById(R.id.et_card_expiry_month);
        return  view;
    }
}
