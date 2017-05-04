package com.razorpay.sampleapp;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.razorpay.sampleapp.fragments.CardFragment;
import com.razorpay.sampleapp.fragments.NetbankingFragment;
import com.razorpay.sampleapp.fragments.PaymentMethodFragment;
import com.razorpay.sampleapp.fragments.WalletFragment;


public class PaymentMethodPagerAdapter extends FragmentPagerAdapter {

    PaymentMethods methods;
    PaymentMethodFragment currentFragment;

    public PaymentMethodPagerAdapter(FragmentManager fm, PaymentMethods methods) {
        super(fm);
        this.methods = methods;
    }

    @Override
    public Fragment getItem(int position) {
        return getFragment(methods.getMethods().get(position));
    }

    private Fragment getFragment(String method) {
        if(method.equals("Card")){
            return new CardFragment();
        }
        if(method.equals("Netbanking")){
            return new NetbankingFragment();
        }
        if(method.equals("Wallet")){
            return new WalletFragment();
        }
        return new CardFragment();
    }

    @Override
    public int getCount() {
        return methods.getMethods().size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return methods.getMethods().get(position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((PaymentMethodFragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public PaymentMethodFragment getCurrentFragment() {
        return currentFragment;
    }
}
