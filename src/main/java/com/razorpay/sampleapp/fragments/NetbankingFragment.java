package com.razorpay.sampleapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.razorpay.sampleapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class NetbankingFragment extends PaymentMethodFragment {

    Spinner bankListSpinner;
    @Override
    public JSONObject getMethodPayload() {
        try {
            JSONObject payload = new JSONObject();
            String bank = bankListSpinner.getSelectedItem().toString();
            payload.put("bank", getPaymentMethods().getBankCode(bank));
            return payload;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public String getMethod() {
        return "netbanking";
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_netbanking, null);
        bankListSpinner = (Spinner) view.findViewById(R.id.spinner_bank_list);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, getPaymentMethods().getBankList());
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        bankListSpinner.setAdapter(spinnerAdapter);
        return view;
    }
}
