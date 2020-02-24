package com.example.mapp;


import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;


import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
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
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AppBarConfiguration mAppBarConfiguration;
    private boolean loggedIn = true;
    private GoogleMap mMap;
    final int x = 0;
    private GoogleApiClient client;
    private FusedLocationProviderClient mFusedLocationProviderClient;


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
                drawer.closeDrawer(Gravity.LEFT, true);
                navController.navigate(R.id.nav_home);
                loggedIn = false;
                Intent reStart = new Intent(getApplicationContext(), MainActivity.class);
                reStart.putExtra("loggedIn", false);
                startActivity(reStart);
                MainActivity.this.finish();
                return false;
            }
        });

        /* determines what is shown in the drawer layout (navigation menu; top left)**/
        if(!loggedIn){
            userInNav.setTextSize(18);
            userInNav.setText("Hey, who are you!?");
            schedule.setVisible(false);
            login.setVisible(true);
            signup.setVisible(true);
            signout.setVisible(false);
        }
        else {
            userInNav.setText("Its a Me, Mario!");
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
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search_button).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        return true;
    }

    /** connects the navigation controller to its respective fragment**/
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


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

}
