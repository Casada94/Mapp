package com.example.mapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapp.entityObjects.point;

import java.util.ArrayList;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Integer> count = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Classes>> classes = new MutableLiveData<>();
    private final MutableLiveData<String> userInput = new MutableLiveData<>();
    private final MutableLiveData<Boolean> readDbDone = new MutableLiveData<>();

    public HomeViewModel(){
        super();
        count.setValue(0);
        classes.setValue(new ArrayList<Classes>());
        userInput.setValue("");
        readDbDone.setValue(false);
    }

    /* Getters */
    public LiveData<Integer> getCount() { return count; }
    public LiveData<ArrayList<Classes>> getClasses() { return classes; }
    public LiveData<String> getUserInput() { return userInput; }
    public LiveData<Boolean> getDone(){ return readDbDone; }

    /* Setters */
    public void setClasses(ArrayList<Classes> classes){ this.classes.setValue(classes); }
    public void setUserInput(String input) { userInput.setValue(input); }
    public void incrementCount(){
        count.setValue(count.getValue()+1);
    }
    public void setDone(){ readDbDone.setValue(true); }



}