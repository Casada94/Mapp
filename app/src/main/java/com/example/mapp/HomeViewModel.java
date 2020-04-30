package com.example.mapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapp.entityObjects.point;

import java.util.ArrayList;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Integer> count = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Classes>> classes = new MutableLiveData<>();


    public HomeViewModel(){
        super();
        count.setValue(0);
        classes.setValue(new ArrayList<Classes>());
    }

    public LiveData<Integer> getCount() {
        return count;
    }
    public LiveData<ArrayList<Classes>> getClasses() { return classes; }

    public void setClasses(ArrayList<Classes> classes){
        this.classes.setValue(classes);
    }

    public void incrementCount(){
        count.setValue(count.getValue()+1);
    }
}