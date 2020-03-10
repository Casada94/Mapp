package com.example.mapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment {

    private MyAdapter myAdapter;
    private ArrayList<Classes> schedule = new ArrayList<>();
    private RecyclerView currSchedule;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_schedule, container, false);

        /* set up for the list view of classes in user's schedule**/
        currSchedule = root.findViewById(R.id.currentSchedule);
        currSchedule.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        currSchedule.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter(getContext(), schedule);
        currSchedule.setAdapter(myAdapter);

        //for(int i = 0; i< 5; i++)
        //    schedule.add(new Classes("CECS " + (420 +i), "ECS" + (310 + i), "M/W", "10:" + (10*i), true));


        final TextView temp1 = root.findViewById(R.id.dbStuff);

        /* testing DB stuff */
        final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collRef = db.collection("users");

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
                            //String amPM = (String) documentSnapshots.get(i).get("AmPm");
                            /*boolean PM;
                            if(amPM.equals("AM"))
                                PM = false;
                            else
                                PM = true;*/

                            schedule.add(new Classes(tempName, templocation, tempDays,tempTime, false));
                        }
                        myAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        collRef.document(currUser.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        temp1.setText(document.get("userEmail").toString());
                    }
                }
            }
        });





        //myAdapter.notifyDataSetChanged();

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

        final EditText className = root.findViewById(R.id.className);

        final String[] buildings = {"ECS", "VEC"};



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

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                String temp = "oo";
                int best  = 100;
                int tested;
                for(int i = 0; i < buildings.length-1; i++){
                    tested =  invalidText.toString().compareTo(buildings[i]);
                    if(Math.abs(tested) < best){
                        best = Math.abs(tested);
                        temp = buildings[i];
                    }
                }
                return temp;
            }
        });

        location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(location.getText() != null && !hasFocus)
                    ((AutoCompleteTextView)v).performValidation();
            }
        });

        Button save = root.findViewById(R.id.submitClass);

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String days = meetingDays.getSelectedItem().toString();
                final String time = meetingTime.getSelectedItem().toString();
                final String cName = className.getText().toString();
                final String cLocation = location.getText().toString();

                schedule.add(new Classes(cName, cLocation, days,time,true));
                className.clearComposingText();
                location.clearComposingText();



                db.collection("users").document(currUser.getEmail()).collection("schedule").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> data = new HashMap<>();
                            data.put("class", cName);
                            data.put("location", cLocation);
                            data.put("days", days);
                            data.put("time", time);

                            db.collection("users").document(currUser.getEmail()).collection("schedule").add(data);
                        }

                    }
                });

                addClass.setVisibility(View.INVISIBLE);
                className.onEditorAction(EditorInfo.IME_ACTION_DONE);

            }
        });


        return root;
    }
}