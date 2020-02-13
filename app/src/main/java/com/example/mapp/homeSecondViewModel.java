package com.example.mapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class homeSecondViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public homeSecondViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the second home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}