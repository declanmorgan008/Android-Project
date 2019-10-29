package com.morgan.declan.samplelogin.ui.additem;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddItemViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AddItemViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Trade fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}