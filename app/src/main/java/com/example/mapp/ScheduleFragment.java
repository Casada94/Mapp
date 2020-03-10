package com.example.mapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;

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

        for(int i = 0; i< 5; i++)
            schedule.add(new Classes("CECS " + (420 +i), "ECS" + (310 + i), "M/W", "10:" + (10*i), true));

        myAdapter.notifyDataSetChanged();

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

        Spinner meetingDays = root.findViewById(R.id.meetingDays);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.meetingDays, android.R.layout.simple_spinner_dropdown_item);
        meetingDays.setAdapter(adapter);

        Spinner meetingTime = root.findViewById(R.id.classTime);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.classTime, android.R.layout.simple_spinner_dropdown_item);
        meetingTime.setAdapter(adapter1);

        final TextView temp1 = root.findViewById(R.id.dbStuff);

        /* testing DB stuff */
        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collRef = db.collection("users");
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



        return root;
    }
}