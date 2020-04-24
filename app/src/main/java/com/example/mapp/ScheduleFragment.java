package com.example.mapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapp.entityObjects.point;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ScheduleFragment extends Fragment {

    private MyAdapter myAdapter;
    private ArrayList<Classes> schedule = new ArrayList<>();
    private RecyclerView currSchedule;

    private MyAdapter myAdapter2;
    private ArrayList<Classes> tdaySchedule = new ArrayList<>();
    private RecyclerView todaySchedule;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private HomeViewModel homeViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_schedule, container, false);

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        /* Hides search button in action bar */
        Toolbar toolbar = ((MainActivity) getActivity()).findViewById(R.id.toolBar);
        MenuItem menuItem = toolbar.getMenu().getItem(0);
        menuItem.setVisible(false);


        /* Call back and dialog for removing a class */
        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.app_name);
                builder.setMessage("Are you sure you want to remove the class?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        remove(position);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        };

        /* set up for the list view of classes in user's schedule**/
        currSchedule = root.findViewById(R.id.currentSchedule);
        currSchedule.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        currSchedule.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter(getContext(), schedule, listener);
        currSchedule.setAdapter(myAdapter);


        /* Set up for the list view of Todays classes in user schedule */
        todaySchedule = root.findViewById(R.id.todaySchedule);
        todaySchedule.setHasFixedSize(true);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(root.getContext());
        todaySchedule.setLayoutManager(layoutManager2);
        myAdapter2 = new MyAdapter(getContext(), tdaySchedule);
        todaySchedule.setAdapter(myAdapter2);

        /*  TESTING GROUND
        Button go = root.findViewById(R.id.todayDirections);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ArrayList<String> destinations = new ArrayList<>();
                ArrayList<Classes> schedule = myAdapter2.getClasses();

                for(int i = 0; i < myAdapter2.getItemCount(); i++){
                     destinations.add("b-" + schedule.get(i).getLocation());
                }

                homeViewModel.setPath(destinations);

                NavController navController = Navigation.findNavController(Objects.requireNonNull(getActivity()).findViewById(R.id.nav_host_fragment));
                navController.navigate(R.id.nav_home);
            }
        });*/



        /* Get Users Schedule */
        final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

        CollectionReference collRef = db.collection("users");
        final Calendar calendar = Calendar.getInstance();
        int day= 0;
        final TextView none = root.findViewById(R.id.none);

        db.collection("users").document(currUser.getEmail()).collection("schedule").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()){
                        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();

                        for(int i = 0; i< documentSnapshots.size(); i++){
                            String tempName = (String) documentSnapshots.get(i).get("class");
                            String templocation = (String) documentSnapshots.get(i).get("location");
                            String tempDays = (String) documentSnapshots.get(i).get("days");
                            String tempTime = (String) documentSnapshots.get(i).get("time");
                            String amPM = (String) documentSnapshots.get(i).get("AmPm");
                            boolean PM;
                            if(amPM.equals("AM"))
                                PM = false;
                            else
                                PM = true;

                            schedule.add(new Classes(tempName, templocation, tempDays,tempTime, PM));

                            String weekDay = Day(calendar.get(Calendar.DAY_OF_WEEK));

                            if(tempDays.contains(weekDay)){
                                none.setVisibility(View.INVISIBLE);
                                tdaySchedule.add(new Classes(tempName, templocation, tempDays,tempTime, PM));
                            }

                        }
                        myAdapter.notifyDataSetChanged();
                        myAdapter2.notifyDataSetChanged();
                    }
                }
            }
        });


        /* connects framelayout in XML to java code and hides its visibility**/
        final CardView addClass = root.findViewById(R.id.addClassView);
        addClass.setContentPadding(40,20,40,20);
        addClass.setCardElevation(30);
        addClass.setRadius(50);
        addClass.setVisibility(View.INVISIBLE);

        /* connects button from XML with java code**/
        Button add = root.findViewById(R.id.addClass);

        /* Sets functionality for add button **/
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClass.setVisibility(View.VISIBLE);
            }
        });

        /* DropDown menu stuff */
        final Spinner meetingDays = root.findViewById(R.id.meetingDays);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.meetingDays, android.R.layout.simple_spinner_dropdown_item);
        meetingDays.setAdapter(adapter);

        final Spinner meetingTime = root.findViewById(R.id.classTime);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.classTime, android.R.layout.simple_spinner_dropdown_item);
        meetingTime.setAdapter(adapter1);

        final Spinner amPm = root.findViewById(R.id.ampm);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(), R.array.amPm, android.R.layout.simple_spinner_dropdown_item);
        amPm.setAdapter(adapter2);

        /* Connects editable fields with java code */
        final EditText className = root.findViewById(R.id.className);
        HashMap<String, point> points = new HashMap<>();
        points = MainActivity.readData(getContext());
        final String[] buildings = new String[points.size()];
        for(int i = 0; i < buildings.length; i++)
        {
            buildings[i] = points.get(i).getName();
        }


        /* Set up for auto complete text view of location
        * This ensures that the user selects a valid, known location */
        ArrayAdapter<String> textAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, buildings);
        final AutoCompleteTextView location = root.findViewById(R.id.classLocation);
        location.setAdapter(textAdapter);
        location.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(CharSequence text) {
                boolean found = false;
                for(int i = 0; i < buildings.length -1; i++){
                    if(buildings[i].equals(text.toString())){
                        location.setText(buildings[i]);
                        return true;
                    }
                }
                return false;
            }

            /* Changes user location input to the closest matching location; if needed */
            @Override
            public CharSequence fixText(CharSequence invalidText) {
                String temp = "oo";
                int best  = 100;
                int tested;
                for(int i = 0; i < buildings.length; i++){
                    tested =  invalidText.toString().compareTo(buildings[i]);
                    if(Math.abs(tested) < best){
                        best = Math.abs(tested);
                        temp = buildings[i];
                    }
                }
                return temp;
            }
        });

        /* when the user clicks away from location input, the location validation is performed */
        location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(location.getText() != null && !hasFocus)
                    ((AutoCompleteTextView)v).performValidation();
            }
        });

        /* Set up for "Save" button
        * this adds the new class to firestore and updates the current schedule showing on screen */
        Button save = root.findViewById(R.id.submitClass);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                /* gets user input */
                final String days = meetingDays.getSelectedItem().toString();
                final String time = meetingTime.getSelectedItem().toString();
                final String cName = className.getText().toString();
                final String cLocation = location.getText().toString();
                final String isPM = amPm.getSelectedItem().toString();
                boolean amPM;
                if(isPM.equals("PM"))
                    amPM = true;
                else
                    amPM = false;

                /* adds to current schedule locally and closes extraneous UI */
                schedule.add(new Classes(cName, cLocation, days,time, amPM));
                //className.clearComposingText();
                //location.clearComposingText();
                className.onEditorAction(EditorInfo.IME_ACTION_DONE);
                location.onEditorAction(EditorInfo.IME_ACTION_DONE);
                addClass.setVisibility(View.INVISIBLE);

                /* Adds new class to firestore */
                db.collection("users").document(currUser.getEmail()).collection("schedule").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> data = new HashMap<>();
                            data.put("class", cName);
                            data.put("location", cLocation);
                            data.put("days", days);
                            data.put("time", time);
                            data.put("AmPm", isPM);

                            db.collection("users").document(currUser.getEmail()).collection("schedule").add(data);
                        }
                    }
                });
            }
        });

        /* Simple button to close the "add class" card view */
        ImageButton close = root.findViewById(R.id.close_new_class);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                className.onEditorAction(EditorInfo.IME_ACTION_DONE);
                addClass.setVisibility(View.INVISIBLE);
            }
        });

        return root;
    }

    public float[] getSchedulePath(HashMap<String, point> points, ArrayList<point> schedule){
        ArrayList<point> path = new ArrayList<>();
        for(int i = 0; i < schedule.size() - 1; i++)
        {
            path.addAll(findPath(points, schedule.get(i), schedule.get(i+1)));
        }
        float [] schedulePath = new float[path.size()*2];
        for (int i = 0; i < schedulePath.length; i++)
        {
            if( i % 2 == 0)
                schedulePath[i] = (float) path.get(i/2).getX();
            else
                schedulePath[i] = (float) path.get(i/2).getY();
        }
        return schedulePath;
    }

    /* removes class from firestore and local class array */
    public void remove(final int position){
        final String temp = schedule.get(position).getClassName();
        final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currUser.getEmail()).collection("schedule").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot  querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()){
                        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                        for(int i = 0; i < documentSnapshots.size(); i++){
                            if(temp.equals(documentSnapshots.get(i).get("class"))){
                                db.collection("users").document(currUser.getEmail()).collection("schedule").document(documentSnapshots.get(i).getId()).delete();
                                schedule.remove(position);
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });
    }

    public interface RecyclerViewClickListener{
        void onClick(View view, int position);
    }

    public String Day(int a){
        String weekDay;

        switch(a){
            case 2:
                weekDay = "M";
                break;
            case 3:
                weekDay = "T";
                break;
            case 4:
                weekDay = "W";
                break;
            case 5:
                weekDay = "Th";
                break;
            case 6:
                weekDay = "F";
                break;
            case 7:
                weekDay = "S";
                break;
            default:
                weekDay = "Sun";
        }

        return weekDay;
    }

    public static ArrayList<point> findPath(HashMap<String, point> points, point start, point dest)
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
        distance.put(start.getName(), 0.0);
        System.out.println("Distance: " );
        for(String x : distance.keySet())
        {
            System.out.println(x + " " + distance.get(x));
        }
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
                if(points.get(p) != null && distance.get(curr.getName()) != null)
                    newDistance = curr.distance(points.get(p)) + distance.get(curr.getName());
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
            dest = parent.get(dest.getName());
        }
        path.add(0, start);
        return path;
    }
}