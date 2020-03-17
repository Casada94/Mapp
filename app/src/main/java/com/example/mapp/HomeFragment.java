package com.example.mapp;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.example.mapp.entityObjects.point;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private ImageView map;
    private Polygon[] bOutlines;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oldDist = 1.0f;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    CardView buildingDetails;
    TextView buildingName;
    TextView hours;
    ImageButton report;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        final PanoViewModel panoViewModel = new ViewModelProvider(requireActivity()).get(PanoViewModel.class);

        /* Sets up the map */
        map = root.findViewById(R.id.map);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inMutable = true;
        final Bitmap mapMap;
        mapMap = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);

        map.setImageBitmap(mapMap);

        buildingDetails = root.findViewById(R.id.buildingDetails);
        buildingName = root.findViewById(R.id.bName);
        hours = root.findViewById(R.id.hours);
        report = root.findViewById(R.id.report);

        /* Logic for deciding how to initialize the bounds of buildings */
        if(upToDate()){
            setUpOutlines();
        }
        else{
            upDateOutlines();
        }


        ArrayList<point> unfiltered = readData();
        System.out.println(unfiltered.size());
        final ArrayList<point> filtered = new ArrayList<>();
        for(int i = 0; i < unfiltered.size(); i++){
            if(unfiltered.get(i).getName().startsWith("p")){
                filtered.add(unfiltered.get(i));
            }
        }




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

                //float[] points = {4200,1380,4075,1550,   4075,1550,4075,1690,     4075,1690,4055,1700,      4055,1700, 4055,2250,   4055,2250,3650,2250,     3650,2250,3650,2500};
                float[] points = {(float)(bOutlines[0].points[0].x/.87637), (float)(bOutlines[0].points[0].y/.87344), (float) (bOutlines[0].points[2].x/.87637), (float) (bOutlines[0].points[2].y/.87344)};

                routeMap = drawRoute(mapMap, points);
                map.setImageBitmap(routeMap);
            }
        });

        final long[] startTime = {0};
        final long[] duration = {0};
        final int[] count = {0};
        final PointF firstXY = new PointF();
        final PointF secondXY = new PointF();

        final Handler handler = new Handler();
        final Runnable[] longPressed = new Runnable[1];

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

                        /* Sets up and initializes long press function  */
                        matrix.getValues(f);
                        longPressed[0] = new Runn(new SimplePoint((int)(-f[2]/f[0] + event.getX()/f[0]), (int)(-f[5]/f[0] + event.getY()/f[0])), bOutlines);
                        handler.postDelayed(longPressed[0], ViewConfiguration.getLongPressTimeout());

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

                        /* Cancels long press function if finger is lifted */
                        handler.removeCallbacks(longPressed[0]);

                        Long time = Long.valueOf(1000);
                            if(count[0] == 2) {
                                 time = System.currentTimeMillis() - startTime[0];
                                duration[0] += time;
                                secondXY.set(event.getX(), event.getY());
                            }

                        float distance = (float) Math.sqrt(Math.pow((secondXY.x-firstXY.x), 2) + Math.pow((secondXY.y - firstXY.y), 2));
                        matrix.getValues(f);

                        if(count[0] == 2) {
                            if (f[Matrix.MSCALE_X] == 3) {
                                if (distance < 30) {
                                    if (time <= 500) {
                                        openStreetView(filtered, new SimplePoint((int)event.getX(), (int)event.getY()), panoViewModel);

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

                            /* Cancels long press if its done while dragging */
                            float longPressDistance = (float) Math.sqrt(Math.pow(moveXby,2) + Math.pow(moveYby,2));
                            if(longPressDistance > 15)
                                handler.removeCallbacks(longPressed[0]);


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


                        } else if (mode == ZOOM) {
                            handler.removeCallbacks(longPressed[0]);

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
                System.out.println((-f[2]/f[0] + event.getX()/f[0]) + ", " + (-f[5]/f[0] + event.getY()/f[0]));

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

    /* Checks to see when the last time building detail and bounds was updated */
    public boolean upToDate(){
        Context context = getActivity();
        SharedPreferences mPrefs = context.getSharedPreferences("com.example.mapp.upToDate", Context.MODE_PRIVATE);

        String last = mPrefs.getString("lastChecked", "00000000");
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        return today.equals(last);
    }

    /* Sets up the clickable areas for building details
    * pulls from firestore and saves into shared preferences */
    public void upDateOutlines(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("polygons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()){
                        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                        bOutlines = new Polygon[documentSnapshots.size()];

                        int x[] = new int[4];
                        int y[] = new int[4];
                        String name;
                        DocumentSnapshot temp;
                        for(int i = 0; i< documentSnapshots.size(); i++){
                            temp = documentSnapshots.get(i);
                            name = temp.get("name").toString();
                            x[0] = Integer.parseInt(temp.get("coord1x").toString());
                            y[0] = Integer.parseInt(temp.get("coord1y").toString());

                            x[1] = Integer.parseInt(temp.get("coord2x").toString());
                            y[1] = Integer.parseInt(temp.get("coord2y").toString());

                            x[2] = Integer.parseInt(temp.get("coord3x").toString());
                            y[2] = Integer.parseInt(temp.get("coord3y").toString());

                            x[3] = Integer.parseInt(temp.get("coord4x").toString());
                            y[3] = Integer.parseInt(temp.get("coord4y").toString());
                            bOutlines[i] = new Polygon(x,y,4, name);

                        }
                        Context context = getActivity().getApplicationContext();
                        SharedPreferences mPrefs = context.getSharedPreferences("com.example.mapp.outlines", Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = mPrefs.edit();
                        prefEditor.clear();
                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        for(int i = 0; i< bOutlines.length; i++){
                            prefEditor.putString(String.valueOf(i), gson.toJson(bOutlines[i]));

                        }
                        prefEditor.apply();

                        mPrefs = context.getSharedPreferences("com.example.mapp.upToDate", Context.MODE_PRIVATE);
                        prefEditor = mPrefs.edit();
                        prefEditor.clear();
                        prefEditor.putString("lastChecked", new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()));
                        prefEditor.apply();
                    }
                }
            }
        });
    }

    /* Sets up the clickable areas for buildings
    * pulls from shared preferences to save reads on firebase */
    public void setUpOutlines(){
        Context context = getActivity().getApplicationContext();
        context = getActivity().getApplicationContext();
        SharedPreferences mPrefs = context.getSharedPreferences("com.example.mapp.outlines", Context.MODE_PRIVATE);
        //SharedPreferences mPrefs = PreferenceManager.getSharedPreferences("com.example.mapp.outlines", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Map<String, ?> keys = mPrefs.getAll();

        bOutlines = new Polygon[keys.size()];
        for(int i = 0; i< keys.size(); i++){
            String json = keys.get(String.valueOf(i)).toString();
            bOutlines[i] = gson.fromJson(json, new TypeToken<Polygon>(){}.getType());
        }


    }

    /* Sets all the textViews and info for building details card view */
    public void buildingInfo(Polygon building){
        buildingDetails.setContentPadding(40,20,40,20);
        buildingDetails.setVisibility(View.VISIBLE);
        buildingDetails.setCardElevation(30);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facilities").document(building.name).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    buildingName.setText(documentSnapshot.get("name").toString());
                    List<Double> temp = (List<Double>) documentSnapshot.get("Hours");
                    String avail = temp.get(0).intValue() + "am - " + (temp.get(1).intValue())%12 + "pm";
                    hours.setText(avail);
                }
            }
        });

    }

    /* Runnable class so that a variable can be passed to the runnable and open the building details card view */
    class Runn implements Runnable{
        SimplePoint user;
        Polygon[] bOutlines;
        public Runn(SimplePoint user, Polygon[] bOutlines){
            this.user = user;
            this.bOutlines = bOutlines;
        }

        @Override
        public void run(){
            System.out.println("User selected (" + user.x + ", " + user.y + ")");
            for(int i = 0; i < bOutlines.length; i++){
                if(bOutlines[i].contains(user.x, user.y)){
                    buildingInfo(bOutlines[i]);
                    Toast.makeText(getContext(),"hurray", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /*  */
    public void openStreetView(ArrayList<point> streetViews, SimplePoint userTouch, PanoViewModel pano){
        point closest = streetViews.get(0);
        float bestDistance = 1000;
        float testDistance;
        float streetX, streetY;
        for(int i = 0; i < streetViews.size(); i++){
            streetX = (float)streetViews.get(i).getX();
            streetY = (float)streetViews.get(i).getY();
            testDistance = (float) Math.sqrt(Math.pow((userTouch.x - streetX),2) + Math.pow(userTouch.y - streetY, 2));
            if(testDistance < bestDistance){
                bestDistance = testDistance;
                closest = streetViews.get(i);
            }
        }
        pano.setStreetViewPoint(closest);
        NavController navController = Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment));
        navController.navigate(R.id.panoramaview);
    }

    public ArrayList<point> readData()
    {
        ArrayList<point> points = new ArrayList<>();
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        Gson gson = new Gson();
        Map<String, ?> keys = mPrefs.getAll();
        for(String name : keys.keySet())
        {
            String json = keys.get(name).toString();
            point p = gson.fromJson(json, new TypeToken<point>(){}.getType());
            points.add(p);
        }
        return points;
    }

}

