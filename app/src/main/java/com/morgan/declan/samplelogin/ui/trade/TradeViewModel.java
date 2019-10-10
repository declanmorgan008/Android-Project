package com.morgan.declan.samplelogin.ui.trade;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TradeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TradeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Trade fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}