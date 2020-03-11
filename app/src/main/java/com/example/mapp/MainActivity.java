package com.example.mapp;


import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;


import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Reads points from the database and stores it in the SharedPreference
        readPointsDB();



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

        /* sets up the drawer layout**/
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.schedule_frag, R.id.login_frag, R.id.signup_frag)
                .setDrawerLayout(drawer)
                .build();

        /* sets up the navigation controller
          used to change fragments**/
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        final NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView,navController);



        /* connects all the XML elements to the java code**/
        TextView userInNav = (TextView) navigationView.getHeaderView(0).findViewById(R.id.whoIsIt);
        TextView home = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_home);
        Menu menu = (Menu) navigationView.getMenu();
        MenuItem login = menu.findItem(R.id.login_frag);
        MenuItem schedule = menu.findItem(R.id.schedule_frag);
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

        /* determines what is shown in the drawer layout (navigation menu; top left)**/
        if(currentUser == null){
            userInNav.setTextSize(18);
            userInNav.setText("Hey, who are you!?");
            schedule.setVisible(false);
            login.setVisible(true);
            signup.setVisible(true);
            signout.setVisible(false);
        }
        else {
            userInNav.setText(currentUser.getEmail().split("\\.")[0]);
            userInNav.setVisibility(View.VISIBLE);
            schedule.setVisible(true);
            login.setVisible(false);
            signup.setVisible(false);
            signout.setVisible(true);
        }

        checkLocationPermission();

    }


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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                db.collection("facilities")
                            .whereEqualTo("name", searchView.getQuery().toString())
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult())
                                        System.out.println(document);
                                } else
                                    System.out.println("failed");
                            }
                        });

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
                        //mMap.setMyLocationEnabled(true);
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

    public void savePoint(point p)
    {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        prefsEditor.putString(p.getName(), gson.toJson(p));
        prefsEditor.apply();
    }

    //Reads points from SharedPreferences
    public ArrayList<point> readData()
    {
        ArrayList<point> points = new ArrayList<>();
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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

    //Writes points from SharedPreferences to Database
    public void writePointsDB()
    {
        ArrayList<point> points = readData();
        Gson gson = new Gson();

        for(point p : points) {
            FirebaseFirestore.getInstance().collection("points").document(p.getName()).set(p);
        }
    }

    //Reads points from Database and stores in SharedPreferences
    public void readPointsDB()
    {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.clear().commit();
        CollectionReference pointsDB = FirebaseFirestore.getInstance().collection("points");
        pointsDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        point p = document.toObject(point.class);
                        savePoint(p);
                    }
                } else {
                    Log.d("point", "Error getting documents: ", task.getException());
                }
            }
        });
    }
}
