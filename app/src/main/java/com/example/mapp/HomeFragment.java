package com.example.mapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mapp.entityObjects.Building;
import com.example.mapp.entityObjects.point;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private TextView brokenReports;
    private TextView otherReports;

    private CardView reportCard;
    private Spinner reasons;
    private EditText other;

    private Polygon currentBuilding;
    private View previousView;
    private HomeViewModel homeViewModel;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private LocationManager locationManager;
    private static final double left_long = -118.123707;
    private static final double right_long = -118.107734;
    private static final double top_lat = 33.788961;
    private static final double bot_lat = 33.774842;
    private static final int mapSize_x = 4700;
    private static final int mapSize_y = 5008;
    private static float scalingFactorX = 0.87637f;
    private static float scalingFactorY = 0.8734f;

    private FloatingActionButton water;
    private FloatingActionButton bathroom;
    private HashMap<String, point> allPoints = new HashMap<>();

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (previousView != null) {
            inflater.inflate(R.layout.fragment_home, (ViewGroup) previousView, false);
        } else {

            /* Initializations of UI elements */
            final View root = inflater.inflate(R.layout.fragment_home, container, false);
            final PanoViewModel panoViewModel = new ViewModelProvider(requireActivity()).get(PanoViewModel.class);
            homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
            homeViewModel.incrementCount();


            /* Hides search button in action bar */
            if (homeViewModel.getCount().getValue() > 1) {
                Toolbar toolbar = ((MainActivity) Objects.requireNonNull(getActivity())).findViewById(R.id.toolBar);
                MenuItem menuItem = toolbar.getMenu().getItem(0);
                menuItem.setVisible(true);
            }

            /* Connecting XML of Building Details to Java Code */
            buildingDetails = root.findViewById(R.id.buildingDetails);
            buildingDetails.setContentPadding(40, 20, 40, 20);
            buildingName = root.findViewById(R.id.bName);
            hours = root.findViewById(R.id.hours);
            brokenReports = root.findViewById(R.id.broken_changeable);
            otherReports = root.findViewById(R.id.other_changeable);
            ImageButton report = root.findViewById(R.id.report);

            /* Connecting XML of Report card to Java Code */
            reportCard = root.findViewById(R.id.reportCard);
            reportCard.setContentPadding(40, 20, 40, 20);
            reasons = root.findViewById(R.id.reportReasons);
            other = root.findViewById(R.id.otherReason);
            Button submit = root.findViewById(R.id.submitReport);

            /* Set the values of report reason spinner */
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.reportReasons, android.R.layout.simple_spinner_dropdown_item);
            reasons.setAdapter(adapter);

            water = root.findViewById(R.id.waterBtn);
            bathroom = root.findViewById(R.id.bathroomBtn);

            Button go = root.findViewById(R.id.directionsToBuildingButton);

            /* Sets up the map */
            map = root.findViewById(R.id.map);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inMutable = true;
            final Bitmap[] mapMap = new Bitmap[1];
            mapMap[0] = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);
            map.setImageBitmap(mapMap[0]);

            homeViewModel.getDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if(aBoolean.booleanValue())
                        allPoints = getAllPoints();

                }
            });



            /* Functionality for quick search find water floating action button */
            water.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Location loc = getLocation();

                    /* Only for demonstration purposes because we are not on campus */
                    loc.setLongitude(-118.113265);
                    loc.setLatitude(33.778486);

                    point me = findNearestPoint(loc);

                    String wanted = "Water Fountain";
                    point destination = findDestination(allPoints, me, wanted);
                    float [] path = findPath(allPoints, me, destination);
                    map.setImageBitmap(drawRoute(mapMap[0], path));
                    moveTo(destination);
                    mapMap[0] = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);
                }
            });

            bathroom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Location loc = getLocation();

                    /* Only for demonstration purposes because we are not on campus */
                    loc.setLongitude(-118.113265);
                    loc.setLatitude(33.778486);

                    point me = findNearestPoint(loc);

                    String wanted = "Restroom";
                    point destination = findDestination(allPoints, me, wanted);
                    float [] path = findPath(allPoints, me, destination);
                    map.setImageBitmap(drawRoute(mapMap[0], path));
                    map.setImageBitmap(drawRoute(mapMap[0], path));
                    moveTo(destination);
                    mapMap[0] = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);
                }
            });

            homeViewModel.getClasses().observe(getViewLifecycleOwner(), new Observer<ArrayList<Classes>>() {
                @Override
                public void onChanged(ArrayList<Classes> classes) {
                    ArrayList<point> schedulePoints = new ArrayList<>();

                    for(int i = 0; i < classes.size(); i++){
                        schedulePoints.add(getPoint(allPoints, classes.get(i)));
                    }

                    drawSchedulePath(allPoints, schedulePoints, mapMap[0]);
                    mapMap[0] = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);
                }
            });

            homeViewModel.getUserInput().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    if(allPoints.containsKey(s)) {
                        point searchedPoint = new point();
                        searchedPoint = allPoints.get(s);
                        moveTo(searchedPoint);
                        buildingInfo(searchedPoint);
                    }
                    else{
                        if(!s.isEmpty()) {
                            String toastMessage = "Sorry! We couldnt find \"" + s + "\"";
                            Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            //temporarily here for seeing the paths
            // Uncomment readPointsDB in main activity to update the sharedPreferences
            //then tap draw to draw map

            Button drawMap = root.findViewById(R.id.Draw);
            drawMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, point> points = new HashMap<>();
                    SharedPreferences mPrefs = getActivity().getSharedPreferences("points", 0);
                    Gson gson = new Gson();
                    Map<String, ?> keys = mPrefs.getAll();
                    for (String name : keys.keySet()) {
                        String json = keys.get(name).toString();
                        point p = null;
                        switch (name.split("-")[0]) {
                            case "b":
                                p = gson.fromJson(json, new TypeToken<Building>() {
                                }.getType());
                                break;
//                            case "u":
//                                p = gson.fromJson(json, new TypeToken<Utility>() {
//                                }.getType());
//                                break;
                            default:
                                p = gson.fromJson(json, new TypeToken<point>() {
                                }.getType());
                        }
                        points.put(p.getAbbr(), p);
                    }


                    for (String p : points.keySet()) {
                        ArrayList<Float> pointsf = new ArrayList<>();
                        for (String n : points.get(p).getNeighbors()) {
                            point p1 = points.get(p);

                            point p2 = points.get(n);
//                            if(p2 != null) {
                                float x1 = (float) p1.getX();
                                float y1 = (float) p1.getY();
                                float x2 = (float) p2.getX();
                                float y2 = (float) p2.getY();

                                if (p1.getClass() == Building.class) {
                                    x1 /= scalingFactorX;
                                    y1 /= scalingFactorY;
                                } else {
                                x1 += 7;
                                y1 -= 33;
                                }
                                if (p2.getClass() == Building.class) {
                                    x2 /= scalingFactorX;
                                    y2 /= scalingFactorY;
                                } else {
                                x2 += 7;
                                y2 -= 33;
                                }
                                pointsf.add(x1);
                                pointsf.add(y1);
                                pointsf.add(x2);
                                pointsf.add(y2);
//                            }
//                            else
//                                Log.d("ken", "p2 is null" + (p2 == null) + "for p = " + p + " and n = " + n);
                        }
                        float[] pts = new float[pointsf.size()];
                        for (int i = 0; i < pts.length; i++)
                            pts[i] = pointsf.get(i);
                        drawRoute(mapMap[0], pts);
                    }
                    locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }else
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListenerGPS);;
//                    findNearestPoint(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    // Set in locations below manually if you want to test GPS location tracking
                    loc.setLongitude(-118.113265);
                    loc.setLatitude(33.778486);
                    point p = findNearestPoint(loc);
                    Log.d("ken_GPS",  "closest point is " + p.getAbbr());
                }
            });


            /* Slides the card up when the keyboard is opened so that its not hidden behind it */
            final boolean cardAtBottom[] = new boolean[1];
            cardAtBottom[0] = true;
            root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    root.getWindowVisibleDisplayFrame(r);

                    int heightDiff = root.getRootView().getHeight() - (r.bottom - r.top);
                    ViewPropertyAnimator animate = reportCard.animate();
                    if(heightDiff > 270 && cardAtBottom[0]){
                        animate.translationYBy(-(heightDiff - (float)(r.top*1.5)));
                        animate.setDuration(200);
                        animate.start();
                        cardAtBottom[0] = false;
                    }else if(heightDiff < 270 && !cardAtBottom[0]){
                        animate.translationY(heightDiff);
                        animate.setDuration(200);
                        animate.start();
                        cardAtBottom[0] = true;
                    }
                }
            });


            /* Logic for deciding how to initialize the bounds of buildings */
            if (upToDate("lastUpdateBounds")) {
                setUpOutlines();
            } else {
                upDateOutlines();
            }

            final ArrayList<point> filtered = readData();

            final float y[] = new float[2];

            /*Set OnTouchListener for building details so that it may be swiped down to hide*/
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

            /* OnTouchListener for report card so that it may be swiped down to hide */
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

            /* ClickListener to raise the report card into view
            * animated move */
            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewPropertyAnimator animator = reportCard.animate();
                    animator.translationY(-reportCard.getHeight());
                    animator.setDuration(500);
                    animator.start();
                }
            });

            /* Captures user input data and writes it to database */
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String reason = reasons.getSelectedItem().toString();
                    String typedReason = other.getText().toString();
                    Date date = Calendar.getInstance().getTime();
                    //so you can add the report to the right building
                    String currUser;
                    if(mAuth.getCurrentUser() != null){
                         currUser = mAuth.getCurrentUser().getEmail();
                    }
                    else
                        currUser = "N/A";


                    System.out.println(currentBuilding.name );
                    HashMap<String, Object> report = new HashMap<>();
                    report.put("facility", currentBuilding.name);
                    report.put("reason", reason);
                    report.put("description", typedReason);
                    report.put("reportedOn", date.toString());
                    report.put("reportedBy", currUser);
                    report.put("status", "active");
                    /* Ken please add the functionality to write to the database*/
                    db.collection("activeReports").document().set(report);

                    reasons.setSelection(0);
                    other.clearComposingText();

                    ViewPropertyAnimator animator = reportCard.animate();
                    animator.setDuration(500);
                    animator.translationY(reportCard.getHeight());
                    animator.start();
                }
            });

            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Location loc = getLocation();

                    /* Only for demonstration purposes because we are not on campus */
                    loc.setLongitude(-118.113265);
                    loc.setLatitude(33.778486);

                    point me = findNearestPoint(loc);

                    String[] temp = buildingName.getText().toString().split("\\(");
                    temp[0] = temp[1].substring(0, temp[1].length()-1);
                    System.out.println("____________________" + temp[0]);

                    point dest = allPoints.get(temp[0]);
//                    for(String str : allPoints.keySet())
//                    {
//                        if (allPoints.get(str).getClass() == Building.class)
//                        {
//                    if(dest == null)
//                        Log.d("ken", "dest is null" );
//                        }
//                    }
                    map.setImageBitmap(drawRoute(mapMap[0], findPath(allPoints, me, dest)));
                    mapMap[0] = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.map, options);
                }
            } );


            /* Various variables needed for map movement touch functionality */
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
                            longPressed[0] = new Runn((int) (-f[2] / f[0] + event.getX() / f[0]), (int) (-f[5] / f[0] + event.getY() / f[0]), bOutlines);
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
                                            openStreetView(filtered, (int) (-f[2] / f[0] + event.getX() / f[0]), (int) (-f[5] / f[0] + event.getY() / f[0]), panoViewModel);
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

                /* Calculates where exactly on the screen the user wants to zoom in */
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

    /* gets the current/Last known location of the user */
    private Location getLocation(){
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListenerGPS);;

        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return loc;
    }

    /* finds the closest point on the compass map according to the users location data
    * used to find the starting point for direction/path finding */
    private point findNearestPoint(Location loc) {
        point closest = getAllPoints().get("261");
        if(loc != null)
        {
            Double curr_lat = loc.getLatitude();
            Double curr_long = loc.getLongitude();
            if(!(curr_lat > top_lat || curr_lat < bot_lat || curr_long < left_long || curr_long > right_long)) {
                int mapCord_x = (int) ((left_long - curr_long) * mapSize_x / (left_long - right_long));
                int mapCord_y = (int) ((top_lat - curr_lat) * mapSize_y / (top_lat - bot_lat));
                point currGPS = new point("curr", mapCord_x, mapCord_y);
                HashMap<String, point> points = new HashMap<>();
                SharedPreferences mPrefs = getActivity().getSharedPreferences("points", 0);
                Gson gson = new Gson();
                Map<String, ?> keys = mPrefs.getAll();
                for (String name : keys.keySet()) {
                    String json = keys.get(name).toString();
                    String type = "p";
                    try {
                        Integer.parseInt(name);
                    }catch (NumberFormatException nfe)
                    {
                        type = "b";
                    }
                    point p = null;
                    switch (type) {
                        case "b":
                            p = gson.fromJson(json, new TypeToken<Building>() {
                            }.getType());
                            break;
//                        case "u":
//                            p = gson.fromJson(json, new TypeToken<Utility>() {
//                            }.getType());
//                            break;
                        default:
                            p = gson.fromJson(json, new TypeToken<point>() {
                            }.getType());
                    }
                    points.put(p.getAbbr(), p);
                }
                closest = new point("default", Double.MAX_VALUE, Double.MAX_VALUE);
//                Log.d("ken_GPS" , );
//                Log.d("ken_GPS", Double.toString(currGPS.distance(points.get("596"))));
                double minDistance = currGPS.distance(closest);
                for (String p : points.keySet()) {
//                    Log.d("ken_GPS", "minDistance: " + minDistance + " evaluating " + points.get(p).getAbbr());
                    double distance = currGPS.distance(points.get(p));
                    if(points.get(p).getClass() == Building.class)
                        distance = Math.sqrt(Math.pow(currGPS.getX() - (points.get(p).getX() / scalingFactorX) , 2)
                                + Math.pow(currGPS.getX() - (points.get(p).getY() / scalingFactorY) , 2));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closest = points.get(p);
                        Log.d("ken_GPS", "closest is now " + closest.getAbbr() +" " +  minDistance);
                    }
                }
            } else
            {
                String msg="GPS Location off campus";
                Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
            }
        }
        return closest;
    }

    /* moves the map to a destination point; typically a building
    * first: scales image back to 1
    * second: matrix translation to desired destination
    * last: zoom into the location
    * may add pin drop functionality */
    public void moveTo(point dest){
        float[] f = new float[9];
        matrix.getValues(f);
        matrix.postScale(1/f[Matrix.MSCALE_X],1/f[Matrix.MSCALE_Y]);

        matrix.getValues(f);
        float transX = f[Matrix.MTRANS_X];
        float transY = f[Matrix.MTRANS_Y];

        float moveXBy = (float)(540 - (dest.getX() + transX));
        float moveYBy = (float)(604 - (dest.getY() + transY));

        matrix.postTranslate(moveXBy,moveYBy);

        matrix.postScale(2/f[Matrix.MSCALE_X],2/f[Matrix.MSCALE_Y], 540, 604);
        map.setImageMatrix(matrix);
        savedMatrix.set(matrix);
    }

    public point getPoint(HashMap<String, point> allPoints, Classes classes){

        for(String p: allPoints.keySet()){
            if(allPoints.get(p).getAbbr().equals(classes.getLocation()))
                return allPoints.get(p);
        }
        return null;
    }


    /* Needed location listener for finding users current location */
    LocationListener locationListenerGPS = new LocationListener(){
        public void onLocationChanged(Location location)
        {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            String msg="New Latitude: "+latitude + "New Longitude: "+longitude;
            Toast.makeText(getActivity().getApplicationContext(),msg,Toast.LENGTH_LONG).show();
         }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /* draws lines on the map for direction purposes */
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

    /* Draws lines on the map for direction purposes
    * better than above because it handles parsing of float array and setting up the proper order
    * and repitiion of points */
    public Bitmap drawRoute(Bitmap routeMap, ArrayList<point> points){
        Canvas canvas = new Canvas((routeMap));
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);

        ArrayList<Float> path = new ArrayList<>();

        for(int i = 0; i< points.size(); i++){
            path.add((float)points.get(i).getX());
            path.add((float)points.get(i).getY());

            if(i != 0 || i!= points.size()-1){
                path.add((float)points.get(i).getX());
                path.add((float)points.get(i).getY());
            }
        }
        float[] finishedPath = new float[path.size()];
        for(int i = 0; i < path.size(); i++){
            finishedPath[i] = path.get(i);
        }

        canvas.drawLines(finishedPath,p);
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
        String name = building.name;
        db.collection("points2").document(name).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();

                    String temp = documentSnapshot.get("name").toString() + "(" + documentSnapshot.get("abbr").toString() + ")";
                    buildingName.setText(temp);

                    db.collection("activeReports").whereEqualTo("facility", documentSnapshot.get("abbr").toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                QuerySnapshot querySnapshot = task.getResult();
                                if(!querySnapshot.isEmpty()){
                                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                                    if(docs.isEmpty()){
                                        brokenReports.setText("0");
                                        otherReports.setText("0");
                                    }else{
                                        int brokenCount = 0;
                                        int otherCount = 0;
                                        for(int i = 0; i < docs.size(); i++){
                                            if(docs.get(i).get("reason").toString().equals("Broken")){
                                                brokenCount++;
                                            }else
                                                otherCount++;
                                        }
                                        brokenReports.setText(String.valueOf(brokenCount));
                                        otherReports.setText(String.valueOf(otherCount));
                                    }
                                }else{
                                    brokenReports.setText("0");
                                    otherReports.setText("0");
                                }
                            }else{
                                brokenReports.setText("0");
                                otherReports.setText("0");
                            }
                        }
                    });
                }
            }
        });

        /* Animation of building details card sliding up */
        ViewPropertyAnimator animation = buildingDetails.animate();
        animation.translationY(-buildingDetails.getHeight());
        animation.setDuration(750);
        animation.start();
    }

    /* Sets all the textViews and info for building details card view */
    private void buildingInfo(point building){

        Building current = (Building) building;
        String temp = current.getName() + " (" + current.getAbbr() + ")";

        buildingName.setText(temp);

        db.collection("activeReports").whereEqualTo("facility", current.getAbbr()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()){
                        List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                        if(docs.isEmpty()){
                            brokenReports.setText("0");
                            otherReports.setText("0");
                        }else{
                            int brokenCount = 0;
                            int otherCount = 0;
                            for(int i = 0; i < docs.size(); i++){
                                if(docs.get(i).get("reason").toString().equals("Broken")){
                                    brokenCount++;
                                }else
                                    otherCount++;
                            }
                            brokenReports.setText(String.valueOf(brokenCount));
                            otherReports.setText(String.valueOf(otherCount));
                        }
                    }else{
                        brokenReports.setText("0");
                        otherReports.setText("0");
                    }
                }else{
                    brokenReports.setText("0");
                    otherReports.setText("0");
                }
            }
        });

        /* Animation of building details card sliding up */
        ViewPropertyAnimator animation = buildingDetails.animate();
        animation.translationY(-buildingDetails.getHeight());
        animation.setDuration(750);
        animation.start();
    }

    /* Runnable class so that a variable can be passed to the runnable and open the building details card view */
    class Runn implements Runnable{
        int x;
        int y;
        Polygon[] bOutlines;
        Runn(int x, int y, Polygon[] bOutlines){
            this.x = x;
            this.y = y;
            this.bOutlines = bOutlines;
        }

        @Override
        public void run(){
            for (Polygon bOutline : bOutlines) {
                if (bOutline.contains(x, y)) {
                    buildingInfo(bOutline);
                }
            }
        }
    }

    /* Determines where the user has double clicked and which streetview is closest to that
    * updates the viewmodel of the panorama class and navigates to that fragment */
    private void openStreetView(ArrayList<point> streetViews, int userX, int userY, PanoViewModel pano){
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
            testDistance = (float) Math.sqrt(Math.pow((userX - streetX),2) + Math.pow(userY - streetY, 2));
            if(testDistance < bestDistance){
                bestDistance = testDistance;
                closest = streetViews.get(i);
            }
        }
        pano.setStreetViewPoint(closest);
        NavController navController = Navigation.findNavController(Objects.requireNonNull(getActivity()).findViewById(R.id.nav_host_fragment));
        navController.navigate(R.id.panoramaview);
    }

    /* Reads streetview points data into memory from either shared preferences of the DB depending
    * on when the last update was */
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

    /* reads all of the map points into memory from shared Preferences
    * the returned data is used for direction and search functionality */
    public HashMap<String, point> getAllPoints(){
        HashMap<String, point> points = new HashMap<>();
        SharedPreferences mPrefs = getActivity().getSharedPreferences("points", 0);
        Gson gson = new Gson();
        Map<String, ?> keys = mPrefs.getAll();
        for (String name : keys.keySet()) {
            String json = keys.get(name).toString();
            point p = null;
            String type = "p";
            try{
                Integer.parseInt(name);
            }catch (NumberFormatException nfe)
            {
                type = "b";
            }
            switch (type) {
                case "b":
                    p = gson.fromJson(json, new TypeToken<Building>() {
                    }.getType());
                    break;
//                case "u":
//                    p = gson.fromJson(json, new TypeToken<Utility>() {
//                    }.getType());
//                    break;
                default:
                    p = gson.fromJson(json, new TypeToken<point>() {
                    }.getType());
            }
            points.put(p.getAbbr(), p);
        }
        Log.d("ken4", "points size: " + points);

        return  points;
    }

    /* Used to find the closest point of user interest */
    public point findDestination(HashMap<String, point> allPoints, point me, String wanted){
        point closest = new point();
        float distance = 10000;

        for(String p : allPoints.keySet()){
            if(allPoints.get(p).hasUtility(wanted)){
                float currDist = (float)Math.sqrt(Math.pow((me.getX()-allPoints.get(p).getX()),2) - Math.pow((me.getY() - allPoints.get(p).getY()),2));
                if(currDist < distance){
                    distance = currDist;
                    closest = allPoints.get(p);
                }
            }
        }
        Log.d("ken", wanted + " is at " + closest.getAbbr());
        return closest;
    }

    /*finds path based on points of the schedule as an input
    returns points as an float[] for the draw function*/
    public void drawSchedulePath(HashMap<String, point> points, ArrayList<point> schedule, Bitmap mapMap){
        ArrayList<point> path = new ArrayList<>();
        for(int i = 0; i < schedule.size() - 1; i++)
        {
            drawRoute(mapMap, findPath(points, schedule.get(i), schedule.get(i+1)));
        }
    }
    /* finds path from point start to point dest based on the graph defined by points */
    public static float[] findPath(HashMap<String, point> points, point start, point dest)
    {
        ArrayList<point> neighbors = new ArrayList<point>();
        neighbors.add(start);
        HashMap<String, point> parent = new HashMap<String, point>();
        HashMap<String, Double> distance = new HashMap<String, Double>();
        Set<String> keys = points.keySet();
        for(String key : keys)
        {
            distance.put(key, Double.MAX_VALUE);
            parent.put(key, null);
        }
        distance.put(start.getAbbr(), 0.0);
        while(neighbors.size() > 0)
        {
            point curr = neighbors.get(0);
            neighbors.remove(curr);
            for(String p : curr.getNeighbors())
            {
                if(parent.get(p) == null && points.get(p) != start)
                {
                    neighbors.add(points.get(p));
                }
                double newDistance = Double.MAX_VALUE;
                if(points.get(p) != null && distance.get(curr.getAbbr()) != null)
                    newDistance = curr.distance(points.get(p)) + distance.get(curr.getAbbr());
                Log.d("kenP" , "p is " + p);
                if(newDistance < distance.get(p))
                {
                    distance.put(p, newDistance);
                    parent.put(p, curr);
                }
            }
        }

        ArrayList<point> path = new ArrayList<point>();
        while(dest != start)
        {
            path.add(0, dest);
            dest = parent.get(dest.getAbbr());
        }
        path.add(0, start);

        ArrayList<point> p = new ArrayList<>();
        for(int i = 0; i < path.size()-1; i++) {
            p.add(path.get(i));
            p.add(path.get(i+1));
        }
        float[] pathf = new float[p.size() * 2];
        for(int i = 0 ; i < pathf.length; i++)
        {
            float f = 0f;
            float scale = 1;
            if(i % 2 == 0) {
                f = (float) p.get(i / 2).getX();
                if(p.get(i/2).getClass() == Building.class)
                    scale = scalingFactorX;
                else f += 7; // shifts point to right to offset scaling
            }
            else {
                f = (float) p.get(i / 2).getY();
                if(p.get(i/2).getClass() == Building.class)
                    scale = scalingFactorY;
                else f -= 30; //shifts point down offset awkward scaling
            }
            pathf[i] = f / scale;
        }
        return pathf;
    }

}

