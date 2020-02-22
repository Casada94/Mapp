package com.example.mapp;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatCallback;
import androidx.core.graphics.BitmapCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    private float mScaleFactor = 1.0f;
    private ImageView map;

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF startPoint = new PointF();
    PointF midPoint = new PointF();
    float oldDist = 1.0f;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        /* Honestly I dont know the purpose of this section. It isnt needed so idk if it a
          convention that we should follow or what**/
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        map = root.findViewById(R.id.map);

        //mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        map.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                //mScaleGestureDetector.onTouchEvent(event);
                ImageView view = (ImageView) v;
                //System.out.println("matrix=" + savedMatrix.toString());
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:

                        savedMatrix.set(matrix);
                        startPoint.set(event.getX(), event.getY());
                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:

                        oldDist = spacing(event);

                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(midPoint, event);
                            mode = ZOOM;
                        }
                        break;

                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;

                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);



                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            float[] f = new float[9];
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                            }

                            matrix.getValues(f);
                            float scaleX = f[Matrix.MSCALE_X];
                            float scaleY = f[Matrix.MSCALE_Y];

                            if(scaleX <= 0.3f){
                                matrix.postScale((0.3f)/scaleX, (0.3f)/scaleY, midPoint.x, midPoint.y);
                            }else if(scaleX >= 3.0f){
                                matrix.postScale((3.0f)/scaleX, (3.0f)/scaleY, midPoint.x, midPoint.y);
                            }
                        }
                        break;
                        default:


                }

                view.setImageMatrix(matrix);

                return true;
            }

            @SuppressLint("FloatMath")
            private float spacing(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }

            private void midPoint(PointF point, MotionEvent event) {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
                point.set(x / 2, y / 2);
            }
        });



        return root;
    }

}

