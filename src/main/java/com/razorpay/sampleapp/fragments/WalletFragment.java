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

public class WalletFragment extends PaymentMethodFragment {

    Spinner walletListSpinner;
    @Override
    public JSONObject getMethodPayload() {
        try {
            JSONObject payload = new JSONObject();
            String wallet = walletListSpinner.getSelectedItem().toString();
            payload.put("wallet", wallet);
            return payload;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public String getMethod() {
        return "wallet";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, null);
        walletListSpinner = (Spinner) view.findViewById(R.id.spinner_wallet_list);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, paymentMethods.getWalletList());
        walletListSpinner.setAdapter(spinnerAdapter);
        return view;
    }
}
