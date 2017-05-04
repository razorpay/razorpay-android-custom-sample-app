package com.razorpay.sampleapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PaymentMethods {

    JSONObject methodsJson;
    ArrayList<String> methods = new ArrayList<>();
    ArrayList<String> bankNameList = new ArrayList<>();
    ArrayList<String> walletList = new ArrayList<>();
    HashMap<String, String> bankCodeMap = new HashMap<>();

    public PaymentMethods(String methodsString){
        try {
            this.methodsJson = new JSONObject(methodsString);
            if(methodsJson.has("card") && methodsJson.getBoolean("card")){
                methods.add("Card");
            }
            if(methodsJson.has("netbanking")) {
                methods.add("Netbanking");
                processBanks(methodsJson.getJSONObject("netbanking"));
            }
            if(methodsJson.has("wallet")) {
                methods.add("Wallet");
                processWallets(methodsJson.getJSONObject("wallet"));
            }
        } catch (Exception e) {
        }
    }

    private void processWallets(JSONObject wallets) throws JSONException{
        Iterator<String> keys = wallets.keys();
        while (keys.hasNext()) {
            String wallet = keys.next();
            /**
             * Check if wallet is enabled
             */
            if(wallets.getBoolean(wallet)){
                walletList.add(wallet);
            }
        }
    }

    private void processBanks(JSONObject banks) throws JSONException{
        Iterator<String> keys = banks.keys();
        while (keys.hasNext()) {
            String bankCode = keys.next();
            String bankName = banks.getString(bankCode);
            bankNameList.add(bankName);
            bankCodeMap.put(bankName, bankCode);
        }
    }

    public ArrayList<String> getMethods() {
        return methods;
    }

    public ArrayList<String> getWalletList() {
        return walletList;
    }

    public ArrayList<String> getBankList() {
        return bankNameList;
    }

    public String getBankCode(String bankName) {
        return bankCodeMap.get(bankName);
    }
}
