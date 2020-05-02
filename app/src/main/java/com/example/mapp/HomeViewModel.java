package com.example.mapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Integer> count = new MutableLiveData<>();

    public HomeViewModel(){
        super();
        count.setValue(0);

    }

    public LiveData<Integer> getCount() {
        return count;
    }

    public void incrementCount(){
        count.setValue(count.getValue()+1);
    }
}