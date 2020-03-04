package com.example.mapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProviders;
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

public class ScheduleFragment extends Fragment {

    private ScheduleViewModel scheduleViewModel;
    private RecyclerView currSchedule;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String[] tempData = {"temp 1", "temp 2", "temp 3", "temp 4", "temp 5"};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        scheduleViewModel =
                ViewModelProviders.of(this).get(ScheduleViewModel.class);
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);

        /* set up for the list view of classes in user's schedule**/
        currSchedule = root.findViewById(R.id.currentSchedule);
        currSchedule.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(root.getContext());
        currSchedule.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(tempData);
        currSchedule.setAdapter(mAdapter);

        /* connects framelayout in XML to java code and hides its visibility**/
        final FrameLayout addClass = root.findViewById(R.id.addClassView);
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