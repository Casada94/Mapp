package com.example.mapp;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private ImageView map;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oldDist = 1.0f;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

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

        /*testing */
        map = root.findViewById(R.id.map);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inMutable = true;
        final Bitmap mapMap;
        mapMap = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);



        map.setImageBitmap(mapMap);


        /* TEMP BUTTON FOR 360 PANORAMA VIEW */
        Button temp = root.findViewById(R.id.temp);
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment));
                navController.navigate(R.id.panoramaview);
            }
        });

        Button route = root.findViewById(R.id.route);
        route.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final Bitmap routeMap;
                //routeMap = Bitmap.createBitmap(mapMap);

                float[] points = {4200,1380,4075,1550,   4075,1550,4075,1690,     4075,1690,4055,1700,      4055,1700, 4055,2250,   4055,2250,3650,2250,     3650,2250,3650,2500};

                routeMap = drawRoute(mapMap, points);
                map.setImageBitmap(routeMap);
            }
        });

        final long[] startTime = {0};
        final long[] duration = {0};
        final int[] count = {0};
        final PointF firstXY = new PointF();
        final PointF secondXY = new PointF();

        /* Touch motion controls for map */
        map.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){

                ImageView view = (ImageView) v;

                float[] f = new float[9];

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if(count[0] == 0) {
                            startTime[0] = System.currentTimeMillis();
                            firstXY.set(event.getX(), event.getY());
                        }
                        count[0]++;
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
                        Long time = Long.valueOf(1000);
                            if(count[0] == 2) {
                                 time = System.currentTimeMillis() - startTime[0];
                                duration[0] += time;
                                secondXY.set(event.getX(), event.getY());
                            }
                        float distance = (float) Math.sqrt(Math.pow((secondXY.x-firstXY.x), 2) + Math.pow((secondXY.y - firstXY.y), 2));
                        System.out.println(distance);

                        matrix.getValues(f);

                        if(count[0] == 2) {
                            if (f[Matrix.MSCALE_X] == 3) {
                                if (distance < 30) {
                                    if (time <= 500) {
                                        Toast.makeText(getActivity(), "double tapped", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                                count[0] = 0;
                                duration[0] = 0;
                        }

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.getValues(f);

                            float transX = f[Matrix.MTRANS_X];
                            float transY = f[Matrix.MTRANS_Y];
                            float newPosX = transX + (event.getX() - startPoint.x);
                            float newPosY = transY + (event.getY() - startPoint.y);
                            float moveXby = event.getX() - startPoint.x;
                            float moveYby = event.getY() - startPoint.y;


                            /* Prevents the map from moving too far off of screen */
                            if(newPosX > 10){
                                moveXby = -f[Matrix.MTRANS_X] + 10;
                            }
                            if(newPosX < (-4115*f[0] + 1074)) {
                                moveXby = (-4115*f[0] + 1074) - f[Matrix.MTRANS_X];
                            }
                            if(newPosY > 10){
                                moveYby = -f[Matrix.MTRANS_Y] + 40;
                            }
                            if(newPosY < (-4189*f[0] + 1296)){
                                moveYby = (-4189*f[0] + 1296) - f[Matrix.MTRANS_Y];
                            }

                            matrix.postTranslate(moveXby, moveYby);


                            System.out.println(matrix.toString());

                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                            }

                            matrix.getValues(f);
                            float scaleX = f[Matrix.MSCALE_X];
                            float scaleY = f[Matrix.MSCALE_Y];

                            /* Limits Zoom in and Zoom out */
                            if(scaleX <= 0.3f){
                                matrix.postScale((0.3f)/scaleX, (0.3f)/scaleY, midPoint.x, midPoint.y);
                            }else if(scaleX >= 3.0f){
                                matrix.postScale((3.0f)/scaleX, (3.0f)/scaleY, midPoint.x, midPoint.y);
                            }

                            float transX = f[Matrix.MTRANS_X];
                            float transY = f[Matrix.MTRANS_Y];
                            float newPosX = transX + (event.getX() - startPoint.x);
                            float newPosY = transY + (event.getY() - startPoint.y);
                            float moveXby = event.getX() - startPoint.x;
                            float moveYby = event.getY() - startPoint.y;

                            /* Limits panning during zooming */
                            if(newPosX > 10){
                                moveXby = -f[Matrix.MTRANS_X] + 10;
                            }
                            if(newPosX < (-4115*f[0] + 1074)) {
                                moveXby = (-4115*f[0] + 1074) - f[Matrix.MTRANS_X];
                            }
                            if(newPosY > 10){
                                moveYby = -f[Matrix.MTRANS_Y] + 40;
                            }
                            if(newPosY < (-4189*f[0] + 1296)){
                                moveYby = (-4189*f[0] + 1296) - f[Matrix.MTRANS_Y];
                            }

                            matrix.postTranslate(moveXby, moveYby);

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

    public Bitmap drawRoute(Bitmap routeMap, float[] points){
        Canvas canvas = new Canvas(routeMap);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        canvas.drawLines(points, p);
        return routeMap;
    }

}

