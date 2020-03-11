package com.example.mapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.ByteArrayLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class PanoFragment extends Fragment {

    private VrPanoramaView streetView;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference ref = storage.getReference("images_360").child("a360.jpg");

//    StorageReference storageRef = storage.getReference();
//    StorageReference imageRef = storageRef.child("a360.jpg");
//    StorageReference storageRef = storage.getReferenceFromUrl("gs://mapp-d533c/a360.jpg");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_pano, container, false);

        /* Connects the view in XML with the java code */
        streetView = root.findViewById(R.id.panoView);

        final long ONE_MEGABYTE = 1024 * 1024;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
        return root;
    }

}