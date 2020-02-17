package com.example.mapp;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private boolean loggedIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Checks if this activity was launched from a previous activity. for login status purposes**/
        Intent loginStatus = getIntent();
        if(loginStatus.hasExtra("loggedIn"))
            loggedIn = loginStatus.getExtras().getBoolean("loggedIn");


        /** connects the tool bar to the java code and some of its view attributes**/
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolBar);
        myToolBar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        myToolBar.setTitle("Mapp");

        /** connects the drawer layout XML to the code**/
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        /** sets up the drawer layout**/
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.schedule_frag, R.id.login_frag, R.id.signup_frag)
                .setDrawerLayout(drawer)
                .build();

        /** sets up the navigation controller
         * used to change fragments**/
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        final NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView,navController);

        /** connects all the XML elements to the java code**/
        TextView userInNav = (TextView) navigationView.getHeaderView(0).findViewById(R.id.whoIsIt);
        TextView home = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_home);
        Menu menu = (Menu) navigationView.getMenu();
        MenuItem login = menu.findItem(R.id.login_frag);
        MenuItem schedule = menu.findItem(R.id.schedule_frag);
        MenuItem signup = menu.findItem(R.id.signup_frag);
        MenuItem signout = menu.findItem(R.id.signout_frag);

        /** sets the click functionality of the sign out button**/
        signout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                drawer.closeDrawer(Gravity.LEFT, true);
                navController.navigate(R.id.nav_home);
                loggedIn = false;
                Intent reStart = new Intent(getApplicationContext(), MainActivity.class);
                reStart.putExtra("loggedIn", false);
                startActivity(reStart);
                return false;
            }
        });

        /** determines what is shown in the drawer layout (navigation menu; top left)**/
        if(!loggedIn){
            userInNav.setTextSize(18);
            userInNav.setText("Hey, who are you!?");
            schedule.setVisible(false);
            login.setVisible(true);
            signup.setVisible(true);
            signout.setVisible(false);
        }
        else{
            userInNav.setText("Its a Me, Mario!");
            userInNav.setVisibility(View.VISIBLE);
            schedule.setVisible(true);
            login.setVisible(false);
            signup.setVisible(false);
            signout.setVisible(true);

        }


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
}
