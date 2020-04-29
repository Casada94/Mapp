package com.example.mapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapp.entityObjects.point;

/* Panorama view model used for inter-fragment communication */
public class PanoViewModel extends ViewModel {
    private final MutableLiveData<point> streetViewPoint = new MutableLiveData<>();

    public LiveData<point> getPoint(){
        return streetViewPoint;
    }

    public PanoViewModel(){
        super();
    }

    public void setStreetViewPoint(point streetView){
        streetViewPoint.setValue(streetView);
    }
}
