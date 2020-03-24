package com.example.mapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.mapp.entityObjects.point;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;



public class PanoFragment extends Fragment {

    private VrPanoramaView streetView;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    //private StorageReference ref = storage.getReference("images_360");
    private PanoViewModel panoViewModel;
//    StorageReference storageRef = storage.getReference();
//    StorageReference imageRef = storageRef.child("a360.jpg");
//    StorageReference storageRef = storage.getReferenceFromUrl("gs://mapp-d533c/a360.jpg");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_pano, container, false);
        final StorageReference ref = storage.getReference("images_360");//.child("tester.jpg");

        /* Connects the view in XML with the java code */
        streetView = root.findViewById(R.id.panoView);

        panoViewModel = new ViewModelProvider(requireActivity()).get(PanoViewModel.class);
        //ref = ref.child(panoViewModel.getPoint().getValue().getName() + "_360");
        final long ONE_MEGABYTE = 1024 * 1024;
        panoViewModel.getPoint().observe(getViewLifecycleOwner(), new Observer<point>() {
            @Override
            public void onChanged(point point) {
                String picture = panoViewModel.getPoint().getValue().getName() + ".jpg";

                ref.child(picture).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        /* Programmatically provides an image to fill VrPanoramaView */
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);//decodeResource(getResources(), ref.);
                        VrPanoramaView.Options options = new VrPanoramaView.Options();
                        options.inputType = VrPanoramaView.Options.TYPE_MONO;
                        streetView.loadImageFromBitmap(bitmap, options);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        });

        return root;
    }

}