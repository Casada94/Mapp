package com.example.mapp;


import android.annotation.SuppressLint;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.example.mapp.entityObjects.Building;
import com.example.mapp.entityObjects.Report;
import com.example.mapp.entityObjects.Utility;
import com.example.mapp.entityObjects.point;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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

    private CardView buildingDetails;
    private TextView buildingName;
    private TextView hours;

    private CardView reportCard;
    private Spinner reasons;
    private EditText other;

    private Polygon currentBuilding;
    private View previousView;
    private HomeViewModel homeViewModel;


    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if(previousView != null){
            inflater.inflate(R.layout.fragment_home, (ViewGroup) previousView, false);
        }else {

            /* Initializations of UI elements */
            final View root = inflater.inflate(R.layout.fragment_home, container, false);
            final PanoViewModel panoViewModel = new ViewModelProvider(requireActivity()).get(PanoViewModel.class);
            homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
            homeViewModel.incrementCount();

            /* Hides search button in action bar */
            if(homeViewModel.getCount().getValue() > 1){
                Toolbar toolbar = ((MainActivity) Objects.requireNonNull(getActivity())).findViewById(R.id.toolBar);
                MenuItem menuItem = toolbar.getMenu().getItem(0);
                menuItem.setVisible(true);
            }

            buildingDetails = root.findViewById(R.id.buildingDetails);
            buildingDetails.setContentPadding(40, 20, 40, 20);
            buildingName = root.findViewById(R.id.bName);
            hours = root.findViewById(R.id.hours);
            ImageButton report = root.findViewById(R.id.report);

            reportCard = root.findViewById(R.id.reportCard);
            reportCard.setContentPadding(40, 20, 40, 20);
            reasons = root.findViewById(R.id.reportReasons);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.reportReasons, android.R.layout.simple_spinner_dropdown_item);
            reasons.setAdapter(adapter);

            other = root.findViewById(R.id.otherReason);
            Button submit = root.findViewById(R.id.submitReport);

            //TEMP JUST FOR DB FILLING
            Button dbFiller = root.findViewById(R.id.dbLoader);
            dbFiller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputStream in = null;
                    BufferedReader bReader = null;
                    try {
                        in = getActivity().getAssets().open("dbFiller.txt");
                        bReader = new BufferedReader(new InputStreamReader(in));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String line = "";
                    final String[][] tokens = new String[1][];
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                        try {
                            while((line = bReader.readLine())  != null) {
                                Map<String, Object> data = new HashMap<>();

                                tokens[0] = line.split(",");

                                if(tokens[0][0].contains(".")){
                                    String[] tempSuper = tokens[0][0].split("[.]");
                                    System.out.println(tempSuper[0]);
                                    data.put("name", tempSuper[0].toUpperCase());
                                } else{
                                    System.out.println(tokens[0][0]);
                                    data.put("name", tokens[0][0].toUpperCase());
                                }

                                data.put("coord1x", tokens[0][1]);
                                data.put("coord1y", tokens[0][2]);
                                data.put("coord2x", tokens[0][3]);
                                data.put("coord2y", tokens[0][4]);
                                data.put("coord3x", tokens[0][5]);
                                data.put("coord3y", tokens[0][6]);
                                data.put("coord4x", tokens[0][7]);
                                data.put("coord4y", tokens[0][8]);

                                db.collection("polygons").document(tokens[0][0].toLowerCase()).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            System.out.println("added to db");
                                        }
                                    }
                                });

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            });

            /* Sets up the map */
            map = root.findViewById(R.id.map);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inMutable = true;
            final Bitmap mapMap;
            mapMap = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);

            map.setImageBitmap(mapMap);

            //temporarily here for seeing the paths
            // Uncomment readPointsDB in main activity to update the sharedPreferences
            //then set the following to true to draw map
            if(false)
            {
                HashMap<String, point> points = new HashMap<>();
                SharedPreferences mPrefs = getActivity().getSharedPreferences("points", 0);
                Gson gson = new Gson();
                Map<String, ?> keys = mPrefs.getAll();
                for(String name : keys.keySet())
                {
                    String json = keys.get(name).toString();
                    point p = null;
                    switch(name.split("-")[0]){
                        case "b": p = gson.fromJson(json, new TypeToken<Building>(){}.getType());
                            break;
                        case "u" : p = gson.fromJson(json, new TypeToken<Utility>(){}.getType());
                            break;
                        default:
                            p = gson.fromJson(json, new TypeToken<point>(){}.getType());
                    }
                    points.put(p.getName(), p);
                }
                float scalingFactorX = 0.87637f;
                float scalingFactorY = 0.87344f;

                for(String p : points.keySet())
                    Log.d("ken", p);
                for(String p : points.keySet())
                {
                    ArrayList<Float> pointsf = new ArrayList<>();
                    for(String n : points.get(p).getNeighbors()) {
                        point p1 = points.get(p);
//                        try{
//                            Double.parseDouble(n);
//                            n = "p-n;
//                        }catch (NumberFormatException nfe)
//                        {
//                            n = "b-" + n;
//                        }
                        point p2 = points.get(n);

                        Log.d("ken", p + " " + n);
                        float x1 = (float) p1.getX();
                        float y1 = (float) p1.getY();
                        float x2 = (float) p2.getX();
                        float y2 = (float) p2.getY();

                        if(p1.getClass() == Building.class)
                        {
                            x1 /= scalingFactorX;
                            y1 /= scalingFactorY;
                        }else
                        {
//                            x1 += 20;
//                            y1 -= 20;
                        }
                        if(p2.getClass() == Building.class)
                        {
                            x2 /= scalingFactorX;
                            y2/= scalingFactorY;
                        }else
                        {
//                            x2 += 20;
//                            y2 -= 20;
                        }
                        pointsf.add(x1);
                        pointsf.add(y1);
                        pointsf.add(x2);
                        pointsf.add(y2);
                    }
                    float[] pts = new float[pointsf.size()];
                    for(int i = 0; i < pts.length; i++)
                        pts[i] = pointsf.get(i);
                    drawRoute(mapMap, pts);
                }
            }

            /* Logic for deciding how to initialize the bounds of buildings */
            if (upToDate("lastUpdateBounds")) {
                setUpOutlines();
            } else {
                upDateOutlines();
            }

            final ArrayList<point> filtered = readData();

            final float y[] = new float[2];

            /*Set onClickListeners for clickable UI elements*/
            buildingDetails.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            y[0] = event.getY();

                            break;

                        case MotionEvent.ACTION_UP:
                            y[1] = event.getY();

                            if ((y[0] - y[1]) < -300) {
                                ViewPropertyAnimator animate = buildingDetails.animate();
                                animate.translationY(buildingDetails.getHeight());
                                animate.setDuration(750);
                                animate.start();
                            }
                            break;

                        default:
                    }
                    return true;
                }
            });

            final float[] y2 = new float[2];
            reportCard.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            y2[0] = event.getY();
                            break;

                        case MotionEvent.ACTION_UP:
                            y2[1] = event.getY();

                            if ((y2[0] - y2[1]) < -300) {
                                ViewPropertyAnimator animate = reportCard.animate();
                                animate.translationY(reportCard.getHeight());
                                animate.setDuration(750);
                                animate.start();
                            }
                            break;

                        default:
                    }
                    return true;
                }
            });


            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewPropertyAnimator animator = reportCard.animate();
                    animator.translationY(-reportCard.getHeight());
                    animator.setDuration(500);
                    animator.start();
                }
            });

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String reason = reasons.getSelectedItem().toString();
                    String typedReason = other.getText().toString();

                    //so you can add the report to the right building
                    String currBuilding = currentBuilding.name;

                    /* Ken please add the functionality to write to the database */
                    Report r = new Report(reason, typedReason, currBuilding);
                    Gson gson = new Gson();
                    FirebaseFirestore.getInstance().collection("activeReports").add(r);
//                    FirebaseFirestore.getInstance().collection("report").add(r);

                    reasons.setSelection(0);
                    other.clearComposingText();

                    ViewPropertyAnimator animator = reportCard.animate();
                    animator.setDuration(500);
                    animator.translationY(reportCard.getHeight());
                    animator.start();
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
            map.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    ImageView view = (ImageView) v;
                    float[] f = new float[9];

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            if (count[0] == 0) {
                                startTime[0] = System.currentTimeMillis();
                                firstXY.set(event.getX(), event.getY());
                            }
                            count[0]++;
                            savedMatrix.set(matrix);
                            startPoint.set(event.getX(), event.getY());
                            mode = DRAG;

                            /* Sets up and initializes long press function  */
                            matrix.getValues(f);
                            longPressed[0] = new Runn(new SimplePoint((int) (-f[2] / f[0] + event.getX() / f[0]), (int) (-f[5] / f[0] + event.getY() / f[0])), bOutlines);
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

                            Long time = 1000L;
                            if (count[0] == 2) {
                                time = System.currentTimeMillis() - startTime[0];
                                duration[0] = time - startTime[0];
                                secondXY.set(event.getX(), event.getY());
                            }

                            float distance = (float) Math.sqrt(Math.pow((secondXY.x - firstXY.x), 2) + Math.pow((secondXY.y - firstXY.y), 2));
                            matrix.getValues(f);

                            if (count[0] == 2) {
                                if (f[Matrix.MSCALE_X] == 3) {
                                    if (distance < 30) {
                                        if (duration[0] <= 750) {
                                            openStreetView(filtered, new SimplePoint((int) (-f[2] / f[0] + event.getX() / f[0]), (int) (-f[5] / f[0] + event.getY() / f[0])), panoViewModel);
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
                                float longPressDistance = (float) Math.sqrt(Math.pow(moveXby, 2) + Math.pow(moveYby, 2));
                                if (longPressDistance > 15)
                                    handler.removeCallbacks(longPressed[0]);


                                /* Prevents the map from moving too far off of screen */
                                if (newPosX > 10) {
                                    moveXby = -f[Matrix.MTRANS_X] + 10;
                                }
                                if (newPosX < (-4115 * f[0] + 1074)) {
                                    moveXby = (-4115 * f[0] + 1074) - f[Matrix.MTRANS_X];
                                }
                                if (newPosY > 10) {
                                    moveYby = -f[Matrix.MTRANS_Y] + 40;
                                }
                                if (newPosY < (-4189 * f[0] + 1296)) {
                                    moveYby = (-4189 * f[0] + 1296) - f[Matrix.MTRANS_Y];
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
                                if (scaleX <= 0.3f) {
                                    matrix.postScale((0.3f) / scaleX, (0.3f) / scaleY, midPoint.x, midPoint.y);
                                } else if (scaleX >= 3.0f) {
                                    matrix.postScale((3.0f) / scaleX, (3.0f) / scaleY, midPoint.x, midPoint.y);
                                }

                                float transX = f[Matrix.MTRANS_X];
                                float transY = f[Matrix.MTRANS_Y];
                                float newPosX = transX + (event.getX() - startPoint.x);
                                float newPosY = transY + (event.getY() - startPoint.y);
                                float moveXby = event.getX() - startPoint.x;
                                float moveYby = event.getY() - startPoint.y;

                                /* Limits panning during zooming */
                                if (newPosX > 10) {
                                    moveXby = -f[Matrix.MTRANS_X] + 10;
                                }
                                if (newPosX < (-4115 * f[0] + 1074)) {
                                    moveXby = (-4115 * f[0] + 1074) - f[Matrix.MTRANS_X];
                                }
                                if (newPosY > 10) {
                                    moveYby = -f[Matrix.MTRANS_Y] + 40;
                                }
                                if (newPosY < (-4189 * f[0] + 1296)) {
                                    moveYby = (-4189 * f[0] + 1296) - f[Matrix.MTRANS_Y];
                                }

                                matrix.postTranslate(moveXby, moveYby);

                            }
                            break;
                        default:
                    }

                    System.out.println((-f[2] / f[0] + event.getX() / f[0]) + ", " + (-f[5] / f[0] + event.getY() / f[0]));
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
            previousView = root;
            return root;
        }

        return previousView;
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
    private boolean upToDate(String key){
        Context context = getActivity();
        SharedPreferences mPrefs = context.getSharedPreferences("com.example.mapp.upToDate", Context.MODE_PRIVATE);

        String last = mPrefs.getString(key, "00000000");
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        return today.equals(last);
    }

    /* Sets up the clickable areas for building details
    * pulls from firestore and saves into shared preferences */
    private void upDateOutlines(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("polygons").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()){
                        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                        bOutlines = new Polygon[documentSnapshots.size()];

                        int[] x = new int[4];
                        int[] y = new int[4];
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
                        prefEditor.commit();

                        mPrefs = context.getSharedPreferences("com.example.mapp.upToDate", Context.MODE_PRIVATE);
                        prefEditor = mPrefs.edit();
                        prefEditor.clear();
                        prefEditor.putString("lastCheckedBounds", new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()));
                        prefEditor.commit();
                    }
                }
            }
        });
    }

    /* Sets up the clickable areas for buildings
    * pulls from shared preferences to save reads on firebase */
    private void setUpOutlines(){
        Context context = getActivity().getApplicationContext();
        SharedPreferences mPrefs = context.getSharedPreferences("com.example.mapp.outlines", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        Map<String, ?> keys = mPrefs.getAll();

        bOutlines = new Polygon[keys.size()];
        for(int i = 0; i< keys.size(); i++){
            String json = keys.get(String.valueOf(i)).toString();
            bOutlines[i] = gson.fromJson(json, new TypeToken<Polygon>(){}.getType());
        }


    }

    /* Sets all the textViews and info for building details card view */
    private void buildingInfo(Polygon building){
        currentBuilding = building;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facilities").document(building.name).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String finalBuildingName = documentSnapshot.get("name").toString() + " (" + documentSnapshot.get("abbr").toString() + ")";
                    buildingName.setText(finalBuildingName);
                    List<Double> temp = (List<Double>) documentSnapshot.get("Hours");
                    String avail = temp.get(0).intValue() + "am - " + (temp.get(1).intValue())%12 + "pm";
                    hours.setText(avail);
                }
            }
        });

        ViewPropertyAnimator animation = buildingDetails.animate();
        animation.translationY(-buildingDetails.getHeight());
        animation.setDuration(750);
        animation.start();
    }

    /* Runnable class so that a variable can be passed to the runnable and open the building details card view */
    class Runn implements Runnable{
        SimplePoint user;
        Polygon[] bOutlines;
        Runn(SimplePoint user, Polygon[] bOutlines){
            this.user = user;
            this.bOutlines = bOutlines;
        }

        @Override
        public void run(){
            System.out.println("User selected (" + user.x + ", " + user.y + ")");
            for (Polygon bOutline : bOutlines) {
                if (bOutline.contains(user.x, user.y)) {
                    buildingInfo(bOutline);
                }
            }
        }
    }

    /*  */
    private void openStreetView(ArrayList<point> streetViews, SimplePoint userTouch, PanoViewModel pano){
        if(streetViews.isEmpty()){
            streetViews = readData();
        }
        point closest = streetViews.get(0);
        float bestDistance = 20000;
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
        NavController navController = Navigation.findNavController(Objects.requireNonNull(getActivity()).findViewById(R.id.nav_host_fragment));
        navController.navigate(R.id.panoramaview);
    }

    /*  */
    private ArrayList<point> readData() {
        if (upToDate("lastUpdateStreetViewPoints")) {
            /* pull from shared preferences */
            SharedPreferences mPrefs = getActivity().getSharedPreferences("com.example.mapp.streetViewPoints", Context.MODE_PRIVATE);
            ArrayList<point> points = new ArrayList<>();
            Gson gson = new Gson();

            Map<String, ?> keys = mPrefs.getAll();
            for (String name : keys.keySet()) {
                String json = keys.get(name).toString();
                point p = gson.fromJson(json, new TypeToken<point>() {
                }.getType());
                points.add(p);
            }
            return points;
        } else {
            /* pull from db */
            final ArrayList<point> points = new ArrayList<>();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("streetViewLocations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();

                            DocumentSnapshot dSnapshot;

                            String name;
                            int x;
                            int y;

                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                dSnapshot = documentSnapshots.get(i);

                                name = dSnapshot.getId();
                                x = Integer.parseInt(dSnapshot.get("x").toString());
                                y = Integer.parseInt(dSnapshot.get("y").toString());

                                points.add(new point(name, x, y));
                            }

                        }
                        else{
                            System.out.println("empty StreetView Snapshot");
                        }
                    }
                    else{
                        System.out.println("Street view task wasnt successful");
                    }
                }
            });
            /* now save into shared preferences */
            SharedPreferences mPrefs = getActivity().getSharedPreferences("com.example.mapp.streetViewPoints", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = mPrefs.edit();
            prefEditor.clear();
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();

            for(int i = 0; i < points.size(); i++){
                prefEditor.putString(String.valueOf(i), gson.toJson(points.get(i)) );
            }
            prefEditor.commit();

            return points;
        }
    }
}

