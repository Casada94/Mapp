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
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapp.entityObjects.Building;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
                        Toast.makeText(MainActivity.this, "Getting Current Location", Toast.LENGTH_SHORT).show();

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

        final PanoViewModel panoViewModel = new ViewModelProvider(this).get(PanoViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        /* Determines if the points on the mapp need to be updated from the database */
        if(!upToDate("lastPointsUpdate")){
            readPointsDB();
        }
        else{
            homeViewModel.setDone();
        }




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
        /* displays reports in navigation bar for admin users only*/
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
//        Log.d("ken", locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString());

    }

    /* Checks to see when the last time building detail and bounds was updated */
    private boolean upToDate(String key){
        Context context = this.getApplicationContext();
        SharedPreferences mPrefs = context.getSharedPreferences("com.example.mapp.upToDatePoints", Context.MODE_PRIVATE);

        String last = mPrefs.getString(key, "00000000");
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        return today.equals(last);
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


        /* Gets user's search input data from search bar*/
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
//        if(p.getClass() == Building.class)
//            docName += "b-";
////        else if(p.getClass() == Utility.class)
////            docName += "u-";
//        else
//            docName += "p-";
        docName += p.getAbbr();
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
            String type = "p";
            try{
                Integer.parseInt(name);
            } catch (NumberFormatException nfe)
            {
                type = "b";
            }
            switch(type){
                case "b": p = gson.fromJson(json, new TypeToken<Building>(){}.getType());
                   break;
//                case "u" : p = gson.fromJson(json, new TypeToken<Utility>(){}.getType());
//                    break;
                default:
                    p = gson.fromJson(json, new TypeToken<point>(){}.getType());
            }
            points.put(p.getAbbr(), p);
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
//            if(points.get(p).getClass() == Building.class)
//            {
//                docName += "b-";
//            }
////            else if(points.get(p).getClass() == Utility.class)
////                docName += "u-";
//            else
//                docName += "p-";
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
        CollectionReference pointsDB = FirebaseFirestore.getInstance().collection("points2");
        pointsDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        String type = document.getId();
                        try{
                            Integer.parseInt(type);
                            type = "p";
                        } catch(NumberFormatException nfe)
                        {
                            type = "b";
                        }
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

                SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("com.example.mapp.upToDatePoints", Context.MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = mPrefs.edit();
                prefEditor.clear();
                prefEditor.putString("lastPointsUpdate", new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()));
                prefEditor.commit();
                homeViewModel.setDone();
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
    public void KENupdateDB2() {
//        CollectionReference pointsDB = FirebaseFirestore.getInstance().collection("points");
//        final CollectionReference points2DB = FirebaseFirestore.getInstance().collection("points2");
//        final ArrayList<point2> points2 = new ArrayList<point2>();
//        final HashMap<String, String> buildings = new HashMap<>();
//        {
//            buildings.put("AS", "Academic Services");
//        buildings.put("AS.1", "Academic Services");
//        buildings.put("ANNEX", "Art Annex");
//        buildings.put("BAC", "Barrett Athletic Administration building");
//        buildings.put("BKS", "Bookstore");
//        buildings.put("BH", "Brothman Hall");
//        buildings.put("COB", "College of Business");
//        buildings.put("CAFE", "Cafeteria");
//        buildings.put("CDC", "Child Development Center");
//        buildings.put("CLA", "College of Liberal Atrs Administration");
//        buildings.put("CPIE", "College of Professional and International Education");
//        buildings.put("CPIE.2", "College of Professional and International Education");
//        buildings.put("CPAC", "Carpenter Performing Arts Center");
//        buildings.put("CP", "Central Plant");
//        buildings.put("CORP", "Corporation Yard");
//        buildings.put("DC", "Dance Center");
//        buildings.put("DESN", "Design");
//        buildings.put("DESN.2", "Design");
//        buildings.put("ED2", "Education 2");
//        buildings.put("EED", "Bob and Barbara Ellis Education Building");
//        buildings.put("EED.2", "Bob and Barbara Ellis Education Building");
//        buildings.put("EN2", "Engineering 2");
//        buildings.put("EN3", "Engineering 3");
//        buildings.put("EN4", "Engineering 4");
//        buildings.put("ECS", "Engineering and Computer Science");
//        buildings.put("ET", "Engineering Technology");
//        buildings.put("ET.2", "Engineering Technology");
//        buildings.put("FM", "Facilities Management");
//        buildings.put("FO2", "Faculty Office 2");
//        buildings.put("FO3", "Faculty Office 3");
//        buildings.put("FO4", "Faculty Office 4");
//        buildings.put("FO4.1", "Faculty Office 4");
//        buildings.put("FO5`", "Faculty Office 5");
//        buildings.put("FCS", "Family and Consumer Services");
//        buildings.put("FA1", "Fine Arts 1");
//        buildings.put("FA2", "Fine Arts 2");
//        buildings.put("FA2.1", "Fine Arts 2");
//        buildings.put("FA3", "Fine Arts 3");
//        buildings.put("FA4", "Fine Arts 4");
//        buildings.put("FA4.1", "Fine Arts 4");
//        buildings.put("FND", "Foundation");
//        buildings.put("FND.1", "Foundation");
//        buildings.put("HSCI", "Hall of Science");
//        buildings.put("HHS1", "Health & Human Services 1");
//        buildings.put("HHS2", "Health & Human Services 2");
//        buildings.put("HSC", "Hillside Service Center");
//        buildings.put("HC", "Horn Center");
//        buildings.put("HRL", "Housing & Residential Life Office");
//        buildings.put("HSD", "Human Services & Design");
//        buildings.put("IH", "International House");
//        buildings.put("IH.1", "International House");
//        buildings.put("JG", "Japanses Garden");
//        buildings.put("KCAM", "Kleefeld Contemporary Art Museum");
//        buildings.put("KCAM.1", "Kleefeld Contemporary Art Museum");
//        buildings.put("IH", "International House");
//        buildings.put("JG", "Japanses Garden");
//        buildings.put("KCAM", "Kleefeld Contemporary Art Museum");
//        buildings.put("KIN", "Kinesiology");
//        buildings.put("LAB", "Language Arts");
//        buildings.put("LH", "Lecture Hall 150-151");
//        buildings.put("LA1", "Liberal Arts 1");
//        buildings.put("LA2", "Liberal Arts 2");
//        buildings.put("LA3", "Liberal Arts 3");
//        buildings.put("LA4", "Liberal Arts 4");
//        buildings.put("LA5", "Liberal Arts 5");
//        buildings.put("LIB", "Library");
//        buildings.put("LAH", "Los Alamitos Hall");
//        buildings.put("LCH", "Los Cerritos Hall");
//        buildings.put("MHB", "McIntosh Humanities Building");
//        buildings.put("MIC", "Microbiology");
//        buildings.put("MLSC", "Molecular & Life Sciences Center");
//        buildings.put("MMC", "Multimedia Center");
//        buildings.put("NUR", "Nursing");
//        buildings.put("OP", "Outpost");
//        buildings.put("PTS", "Parking and Transportation Services");
//        buildings.put("PSC", "Parkside College");
//        buildings.put("PH1", "Peterson Hall 1");
//        buildings.put("PSY", "Psychology");
//        buildings.put("PYR", "Pyramid");
//        buildings.put("RC", "Recycling Center");
//        buildings.put("REPR", "Reprographics");
//        buildings.put("SSPA", "Social Science/Public Affairs");
//        buildings.put("SOR", "Soroptimist House");
//        buildings.put("SHS", "Student Health Services");
//        buildings.put("SHS.1", "Student Health Services");
//        buildings.put("SRWC", "Student Recreation and Wellness Center");
//        buildings.put("SSC", "Student Success Center");
//        buildings.put("SSCH", "Soccer and Softball Clubhouse");
//        buildings.put("TA", "Theatre Arts");
//        buildings.put("UMC", "University Music Center");
//        buildings.put("UP", "University Police BLDG");
//        buildings.put("USU", "University Student Union");
//        buildings.put("USU.1", "University Student Union");
//        buildings.put("UTC", "University Telecommunications Center");
//        buildings.put("UT", "University Theatre");
//        buildings.put("VIC", "Visitor Information Center");
//        buildings.put("VEC", "Vivian Engineering Center");
//        buildings.put("VEC.1", "Vivian Engineering Center");
//        buildings.put("PSD", "Parkside Dining");
//        buildings.put("PSG", "Parkside Dorm G");
//        buildings.put("PSH", "Parkside Dorm H");
//        buildings.put("PSJ", "Parkside Dorm J");
//        buildings.put("PSK", "Parkside Dorm K");
//        buildings.put("PSL", "Parkside Dorm L");
//        buildings.put("PSM", "Parkside Dorm M");
//        buildings.put("PSN", "Parkside Dorm N");
//        buildings.put("PSP", "Parkside Dorm P");
//        buildings.put("PSQ", "Parkside Dorm Q");
//        buildings.put("PSC", "Parkside Service Center");
//        buildings.put("HSDA", "Hillside Dorm A");
//        buildings.put("HSDB", "Hillside Dorm B");
//        buildings.put("HSDC", "Hillside Dorm C");
//        buildings.put("HSDD", "Hillside Dorm D");
//        buildings.put("HSCN", "Hillside Commons");
//        buildings.put("HD", "Hillside Dining");
//        buildings.put("HD.1", "Hillside Dining");
//        buildings.put("HSDE", "Hillside Dorm E");
//        buildings.put("HSDF", "Hillside Dorm F");
//    }
//        pointsDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()){
//                    for(QueryDocumentSnapshot document : task.getResult())
//                    {
//                        Map<String, Object> hmap = document.getData();
//                        point2 p;
//                        String name = buildings.get(hmap.get("name"));
//                        String abbr = (String) hmap.get("name");
////                        Object X = hmap.get("x");
//                        Double x = document.getDouble("x");
////                        if(X.getClass() == Long.class)
////                            x = Double.longBitsToDouble((long) X);
////                        else
////                            x = (Double) X;
////                        Object Y = hmap.get("y");
//                        Double y = document.getDouble("y");
////                        if(Y.getClass() == Long.class)
////                            y = Double.longBitsToDouble((long) Y);
////                        else
////                            y = (Double) Y;
//                        ArrayList<String> neighbors = (ArrayList<String>) hmap.get("neighbors");
//                        ArrayList<String> utilities = (ArrayList<String>) hmap.get("utilities");
//                        if(neighbors.size() ==0)
//                            Log.d("ken4", "point " + abbr + "has 0 neighbors!");
//                        if(hmap.size() == 6)
//                            p = new Building2(abbr, x, y, neighbors, utilities, name);
//                        else
//                            p = new point2(abbr, x, y, neighbors, utilities);
//                        points2.add(p);
//                    }
//                } else {
//                    Log.d("ken", "Error getting documents: ", task.getException());
//                }
//                for(point2 p : points2)
//                {
//                    String str = " ";
//                    if(p.getClass() == Building2.class)
//                    {
//
//                        str = p.getAbbr() + " " +  ((Building2) p).getName();
//                    }else
//                        str = p.getAbbr();
//                    str += " " + p.getX() + ", " + p.getY();
//                    Log.d("ken2", str);
//                    points2DB.document(p.getAbbr()).set(p);
//
//                }
//                Log.d("ken3", "points2 size = " + points2.size());
//            }
//        });
    }
}
