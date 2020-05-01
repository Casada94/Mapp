package com.example.mapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;


import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.mapp.entityObjects.Building;
import com.example.mapp.entityObjects.Utility;
import com.example.mapp.entityObjects.point;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AppBarConfiguration mAppBarConfiguration;
    private boolean loggedIn = true;
    private GoogleMap mMap;
    final int x = 0;
    private GoogleApiClient client;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private HomeViewModel homeViewModel;
    /* Set up for getting user location and user permissions */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        Toast.makeText(MainActivity.this, "Getting Current Location", Toast.LENGTH_SHORT).show();;

                    } else {
                        Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else{
            buildGoogleApiClient();
        }

    }


    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

//        KENupdateDB2();
//        Log.d("Ken", " success!");
        //Reads points from the database and stores it in the SharedPreference
//       readPointsDB();
//        writePointsDB();
        final PanoViewModel panoViewModel = new ViewModelProvider(this).get(PanoViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        /* Checks if this activity was launched from a previous activity. for login status purposes**/
        Intent loginStatus = getIntent();
        if(loginStatus.hasExtra("loggedIn"))
            loggedIn = loginStatus.getExtras().getBoolean("loggedIn");


        /* connects the tool bar to the java code and some of its view attributes**/
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolBar);
        myToolBar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        myToolBar.setTitle("Mapp");


        /* connects the drawer layout XML to the code**/
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        final boolean[] showReports = new boolean[1];


        /* sets up the drawer layout**/
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.schedule_frag, R.id.report_frag, R.id.login_frag, R.id.signup_frag)
                .setDrawerLayout(drawer)
                .build();

        /* sets up the navigation controller
          used to change fragments**/
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        final NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView,navController);


        /* connects all the XML elements to the java code**/
        TextView userInNav = navigationView.getHeaderView(0).findViewById(R.id.whoIsIt);
        TextView home = navigationView.getHeaderView(0).findViewById(R.id.nav_home);
        Menu menu = navigationView.getMenu();
        MenuItem login = menu.findItem(R.id.login_frag);
        final MenuItem schedule = menu.findItem(R.id.schedule_frag);
        final MenuItem reports = menu.findItem(R.id.report_frag);
        MenuItem signup = menu.findItem(R.id.signup_frag);
        MenuItem signout = menu.findItem(R.id.signout_frag);

        /* sets the click functionality of the sign out button**/
        signout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mAuth.signOut();
                drawer.closeDrawer(Gravity.LEFT, true);
                navController.navigate(R.id.nav_home);
                Intent reStart = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(reStart);
                MainActivity.this.finish();
                return false;
            }
        });

        if(currentUser != null) {
            db.collection("users").document(currentUser.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot result = task.getResult();
                        if(result.contains("admin")){
                            if(result.getBoolean("admin")){
                                showReports[0] = true;
                                System.out.println("______________SHOW REPORTS__________________");
                                updateNavigation(reports, schedule, showReports[0]);
                            }
                            else{
                                showReports[0] = false;
                                updateNavigation(reports, schedule, showReports[0]);
                            }

                        }else {
                            showReports[0] = false;
                            updateNavigation(reports, schedule, showReports[0]);
                        }
                    }
                }
            });
        }else {
            showReports[0] = false;
            updateNavigation(reports, schedule, showReports[0]);
        }


        /* determines what is shown in the drawer layout (navigation menu; top left)**/
        if(currentUser == null){
            userInNav.setTextSize(18);
            userInNav.setText("Hey, who are you!?");
            schedule.setVisible(false);
            reports.setVisible(false);
            login.setVisible(true);
            signup.setVisible(true);
            signout.setVisible(false);
        }
        else {
            userInNav.setText(currentUser.getEmail().split("\\.")[0]);
            userInNav.setVisibility(View.VISIBLE);
            login.setVisible(false);
            signup.setVisible(false);
            signout.setVisible(true);
            schedule.setVisible(false);
            reports.setVisible(false);
        }

        checkLocationPermission();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListenerGPS);
        Log.d("ken", locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString());

    }

    /* Updates the navigation slider view to show/hide reports and schedule */
    public void updateNavigation(MenuItem reports, MenuItem schedule, boolean showReports){
        if(showReports){
            schedule.setVisible(false);
            reports.setVisible(true);
        }
        else{
            schedule.setVisible(true);
            reports.setVisible(false);
        }

    }

    LocationListener locationListenerGPS = new LocationListener(){
        public void onLocationChanged(Location location)
        {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
//            String msg="New Latitude: "+latitude + "New Longitude: "+longitude;
//            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
            Log.d("ken", "long,lat : "+ location.getLongitude() + ", " + location.getLatitude());
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

    /** sets up the options in the toolbar and the rest of its visual aspects**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search_button).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        /* Getting user search data */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                homeViewModel.setUserInput(searchView.getQuery().toString());

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    /** connects the navigation controller to its respective fragment**/
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /* Checks and gets user permission for location */
    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},x);
            }
            else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},x);
            }
            return false;
        }
        else{
            return true;
        }
    }

    /* Sets up builds google API client if permission granted */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]grantResults){
        switch (requestCode){
            case x: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }
//                        mMap.setMyLocationEnabled(true);//
                    }
                } else {

                }

            }
        }
    }

    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        client.connect();

    }

    //saves to SharedPreferences
    public void savePoint(point p, String doc)
    {
        SharedPreferences mPrefs = getSharedPreferences("points", 0);
//        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        String docName = "";
        if(p.getClass() == Building.class)
            docName += "b-";
//        else if(p.getClass() == Utility.class)
//            docName += "u-";
        else
            docName += "p-";
        docName += p.getName();
        prefsEditor.putString(doc, gson.toJson(p));
        prefsEditor.commit();
//        Log.d("ken", "SavePoint " + p.toString());
    }

    //Reads points from SharedPreferences
    public static HashMap<String, point> readData(Context con)
    {
        HashMap<String, point> points = new HashMap<>();
        SharedPreferences mPrefs = con.getSharedPreferences("points", 0);
        Gson gson = new Gson();
        Map<String, ?> keys = mPrefs.getAll();
//        Log.d("ken", "points in sp: " + keys.size());
        for(String name : keys.keySet())
        {
            String json = keys.get(name).toString();
            point p = null;
            switch(name.split("-")[0]){
                case "b": p = gson.fromJson(json, new TypeToken<Building>(){}.getType());
                   break;
//                case "u" : p = gson.fromJson(json, new TypeToken<Utility>(){}.getType());
//                    break;
                default:
                    p = gson.fromJson(json, new TypeToken<point>(){}.getType());
            }
            points.put(p.getName(), p);
        }
        return points;
    }

    //Writes points from SharedPreferences to Database
    public void writePointsDB()
    {
        SharedPreferences pref = getSharedPreferences("points", 0);
        Log.d("ken", "pref size : " + pref.getAll().size());
        HashMap<String, point> points = readData(this);
        Log.d("ken", "maybe? " + points.size());
        for(String p : points.keySet()) {
            String docName = "";
            if(points.get(p).getClass() == Building.class)
            {
                docName += "b-";
            }
//            else if(points.get(p).getClass() == Utility.class)
//                docName += "u-";
            else
                docName += "p-";
            docName += p;
//            Log.d("ken", "writepoint at " + docName);
            FirebaseFirestore.getInstance().collection("points").document(docName).set(points.get(p));
        }
    }

    //Reads points from Database and stores in SharedPreferences
    public void readPointsDB()
    {
        SharedPreferences mPrefs = getSharedPreferences("points", 0);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.clear().commit();
        CollectionReference pointsDB = FirebaseFirestore.getInstance().collection("points");
        pointsDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        String type = document.getId().split("-")[0];
                        point p = null;
                        switch(type){
                            case "b": p = document.toObject(Building.class);
                        break;
//                            case "u": p = document.toObject(Utility.class);
//                            break;
                            default: p = document.toObject(point.class);
                        }
                        savePoint(p, document.getId());
                    }
                } else {
                    Log.d("point", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void KENupdateDB()
    {
        SharedPreferences prefs = getSharedPreferences("points", 0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.clear().commit();
        CollectionReference pointsDB = FirebaseFirestore.getInstance().collection("test");
        final String[] json = new String[1];
        pointsDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        Gson gson = new Gson();
                        HashMap<String, point> points = gson.fromJson((String) document.get("test"), new TypeToken<HashMap<String, point>>(){}.getType());
//                        Log.d("ken2", Integer.toString(points.keySet().size()));
                        for(String p : points.keySet())
                        {
                            savePoint(points.get(p), p);
                        }
                    }
                } else {
                    Log.d("ken", "Error getting documents: ", task.getException());
                }
            }
        });
    }
}
