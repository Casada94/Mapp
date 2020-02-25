package com.example.mapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;


public class PanoFragment extends Fragment {

    private VrPanoramaView streetView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_pano, container, false);

        /* Connects the view in XML with the java code */
        streetView = root.findViewById(R.id.panoView);

        /* Programmatically provides an image to fill VrPanoramaView */
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a360);
        VrPanoramaView.Options options = new VrPanoramaView.Options();
        options.inputType = VrPanoramaView.Options.TYPE_MONO;
        streetView.loadImageFromBitmap(bitmap, options);


        return root;
    }

}
