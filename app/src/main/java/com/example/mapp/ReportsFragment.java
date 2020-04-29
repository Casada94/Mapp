package com.example.mapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportsFragment extends Fragment {

    private AutoCompleteTextView buildingSearch;
    private Button search;
    private Spinner reasonSpinner;
    private Spinner status;
    private RecyclerView reportsList;

    private CardView reportDetailsCard;
    private TextView buildingNameChange;
    private TextView reasonChange;
    private TextView descriptionChange;
    private TextView reportedDateChange;
    private TextView reportedByChange;
    private TextView resolvedOnTxt;
    private TextView resolvedOnChange;
    private TextView resolvedByTxt;
    private TextView resolvedByChange;
    private Button resolve;

    private reportAdapter myAdapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_reports, container, false);

        /* Connect the XML and Java code
        * Elements of search/filter at the top of the app page */
        buildingSearch = root.findViewById(R.id.report_search);
        search = root.findViewById(R.id.report_search_btn);
        reportsList = root.findViewById(R.id.report_recyclerview);
        reasonSpinner = root.findViewById(R.id.type_spinner);
        status = root.findViewById(R.id.status_spinner);

        /* Connect the XML and Java code
        * Elements of the report details card that slides up from the bottom of the screen */
        reportDetailsCard = root.findViewById(R.id.report_details_card);
        buildingNameChange = root.findViewById(R.id.building_name);
        reasonChange = root.findViewById(R.id.reason_changeable);
        descriptionChange = root.findViewById(R.id.description_changeable);
        reportedDateChange = root.findViewById(R.id.reported_on_changeable);
        reportedByChange = root.findViewById(R.id.reported_by_changeable);
        resolvedByTxt = root.findViewById(R.id.closedByTxt);
        resolvedByChange = root.findViewById(R.id.closedBy_changeable);
        resolvedOnTxt = root.findViewById(R.id.closedOnTxt);
        resolvedOnChange = root.findViewById(R.id.closedOn_changeable);
        resolve = root.findViewById(R.id.resolved_button);

        /* Array adapters for the spinners and the auto complete text box */
        final ArrayAdapter<CharSequence> reasonAdapter = ArrayAdapter.createFromResource(getContext(), R.array.reportedReasons, android.R.layout.simple_spinner_dropdown_item);
        reasonSpinner.setAdapter(reasonAdapter);
        final ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(getContext(), R.array.status, android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(statusAdapter);
        final ArrayAdapter<CharSequence> textAdapter = ArrayAdapter.createFromResource(getContext(), R.array.buildings, android.R.layout.simple_dropdown_item_1line);
        buildingSearch.setAdapter(textAdapter);

        /* arraylist for the active and solved reports */
        final ArrayList<HashMap<String, String>> activeReports = new ArrayList<>();
        final ArrayList<HashMap<String, String>> solvedReports = new ArrayList<>();
        final ArrayList<HashMap<String, String>> filteredReports = new ArrayList<>();

        /* Layout Manager set up used for the recycler view of reports */
        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        reportsList.setLayoutManager(layoutManager);

        /* Variables needed for determining the position of the report details card
        * and tracking which element in the recycler view the details card is pertaining to */
        final boolean[] detailsShowing = new boolean[1];
        detailsShowing[0] = false;
        final String[] currentDocID = new String [1];
        final int[] currentPosition = new int[1];

        /* Click listener for when an element of the recycler view is selected by the user
        * may/may not slide the report details card up into view, depending on current visibility state
        * populates the report details card with information associated with the selected report */
        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {

                HashMap<String, String> filler = filteredReports.get(position);
                currentPosition[0] = position;


                buildingNameChange.setText(filler.get("facility"));
                reasonChange.setText(filler.get("reason"));
                descriptionChange.setText(filler.get("description"));
                reportedDateChange.setText(filler.get("reportedOn").toString());
                reportedByChange.setText(filler.get("reportedBy"));

                /* Logic to change the visibility/layout of element in the report details card
                * according to the status of the report */
                if(filler.get("status").equals("active")){
                    resolvedByTxt.setVisibility(View.INVISIBLE);
                    resolvedByChange.setVisibility(View.INVISIBLE);
                    resolvedOnTxt.setVisibility(View.INVISIBLE);
                    resolvedOnChange.setVisibility(View.INVISIBLE);
                    resolve.setVisibility(View.VISIBLE);
                }
                else{
                    resolvedByTxt.setVisibility(View.VISIBLE);
                    resolvedByChange.setVisibility(View.VISIBLE);
                    resolvedOnTxt.setVisibility(View.VISIBLE);
                    resolvedOnChange.setVisibility(View.VISIBLE);
                    resolve.setVisibility(View.INVISIBLE);

                    resolvedOnChange.setText(filler.get("resolvedOn"));
                    resolvedByChange.setText(filler.get("resolvedBy"));
                }

                currentDocID[0] = Objects.requireNonNull(filler.get("docID"));
                System.out.println(currentDocID[0]);

                /* animates the upward movement of the report card if its not currently visible */
                if(!detailsShowing[0]){
                    detailsShowing[0] = true;
                    ViewPropertyAnimator animation = reportDetailsCard.animate();
                    animation.translationY(-reportDetailsCard.getHeight()+15);
                    animation.setDuration(750);
                    animation.start();
                }

            }
        };

        /* Initializes and sets the adapter for the recycler view of reports */
        myAdapter = new reportAdapter(getContext(), filteredReports, listener);
        reportsList.setAdapter(myAdapter);

        /* Gets all of the active reports currently in the database
        * stores them temporarily into memory */
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activeReports").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()){
                        List<DocumentSnapshot> temp = querySnapshot.getDocuments();
                        for(int i = 0; i < querySnapshot.size(); i++){
                            HashMap<String, String> reportSnapshot = new HashMap<>();
                            reportSnapshot.put("facility", temp.get(i).get("facility").toString());
                            reportSnapshot.put("reason", temp.get(i).get("reason").toString());
                            reportSnapshot.put("status", temp.get(i).get("status").toString());
                            reportSnapshot.put("reportedOn", temp.get(i).get("reportedOn").toString());
                            reportSnapshot.put("reportedBy", temp.get(i).get("reportedBy").toString());
                            reportSnapshot.put("docID", temp.get(i).getId());
                            reportSnapshot.put("description",temp.get(i).get("description").toString());

                            activeReports.add(reportSnapshot);

                        }
                        filteredReports.addAll( activeReports);
                        myAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        /* Gets all of the solved reports from the database
        * stores them temporarily into the database */
        db.collection("solvedReports").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()){
                        List<DocumentSnapshot> temp = querySnapshot.getDocuments();
                        for(int i = 0; i < querySnapshot.size(); i++){
                            HashMap<String, String> reportSnapshot = new HashMap<>();
                            reportSnapshot.put("facility", temp.get(i).get("facility").toString());
                            reportSnapshot.put("reason", temp.get(i).get("reason").toString());
                            reportSnapshot.put("status", temp.get(i).get("status").toString());
                            reportSnapshot.put("reportedOn", temp.get(i).get("reportedOn").toString());
                            reportSnapshot.put("reportedBy", temp.get(i).get("reportedBy").toString());
                            reportSnapshot.put("resolvedBy", temp.get(i).get("resolvedBy").toString());
                            reportSnapshot.put("resolvedOn", temp.get(i).get("resolvedOn").toString());
                            reportSnapshot.put("docID", temp.get(i).getId());
                            reportSnapshot.put("description",temp.get(i).get("description").toString());

                            solvedReports.add(reportSnapshot);
                        }
                        filteredReports.addAll(solvedReports);
                        myAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        /* Validates the user input search
        * verifies that the user input is a building on campus */
        buildingSearch.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(CharSequence text) {
                for(int i = 0; i < textAdapter.getCount(); i ++){
                    if(text.equals(textAdapter.getItem(i))){
                        buildingSearch.setText(textAdapter.getItem(i));
                        return true;
                    }
                }
                return false;
            }

            /* Changes the user search data to the closest matching verified building data */
            @Override
            public CharSequence fixText(CharSequence invalidText) {
                String temp = "oo";
                int best  = 100;
                int tested;
                for(int i = 0; i < textAdapter.getCount(); i++){
                    tested =  invalidText.toString().compareTo(textAdapter.getItem(i).toString());
                    if(Math.abs(tested) < best){
                        best = Math.abs(tested);
                        temp = textAdapter.getItem(i).toString();
                    }
                }
                return temp;
            }
        });

        /* Trigger for the verification and text fixing of the search view */
        buildingSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(buildingSearch.getText() != null && !hasFocus)
                    ((AutoCompleteTextView)v).performValidation();
            }
        });

        /* search button click listener
        * performs validation
        * filters reports
        * updates recycler view accordingly */
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildingSearch.performValidation();
                filteredReports.clear();
                filteredReports.addAll(filter(activeReports, solvedReports, buildingSearch.getText().toString(), reasonSpinner.getSelectedItem().toString(), status.getSelectedItem().toString()));
                myAdapter.notifyDataSetChanged();

                buildingSearch.dismissDropDown();
                buildingSearch.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });

        /* Listener for the dropdown menu
        * filters reports
        * updates report list accordingly */
        reasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filteredReports.clear();
                filteredReports.addAll(filter(activeReports, solvedReports, buildingSearch.getText().toString(), reasonSpinner.getSelectedItem().toString(), status.getSelectedItem().toString()));
                System.out.println(filteredReports);
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filteredReports.clear();
                filteredReports.addAll(filter(activeReports, solvedReports, buildingSearch.getText().toString(), reasonSpinner.getSelectedItem().toString(), status.getSelectedItem().toString()));
                myAdapter.notifyDataSetChanged();
            }
        });

        /* Listener for the dropdown menu
         * filters reports
         * updates report list accordingly */
        status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filteredReports.clear();
                filteredReports.addAll(filter(activeReports, solvedReports, buildingSearch.getText().toString(), reasonSpinner.getSelectedItem().toString(), status.getSelectedItem().toString()));
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        /* Resolve button click listener
        * gets all of the needed data of the active report
        * adds addition data for moving the report to solvedReports
        * writes the report to solvedReport collection
        * removes report from activeReport collection */
        resolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("activeReports").document(currentDocID[0]).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot moving = task.getResult();
                            final Date date = Calendar.getInstance().getTime();
                            final Map<String, Object> temp = new HashMap<>();

                            temp.put("facility", moving.get("facility").toString());
                            temp.put("reason", moving.get("reason").toString());
                            temp.put("description", moving.get("description").toString());
                            temp.put("status", "resolved");
                            temp.put("reportedOn", moving.get("reportedOn").toString());
                            temp.put("reportedBy", moving.get("reportedBy").toString());
                            temp.put("resolvedBy", currentUser);
                            temp.put("resolvedOn", date.toString());

                            final HashMap<String, String> temp2 = new HashMap<>();
                            temp2.put("facility", moving.get("facility").toString());
                            temp2.put("reason", moving.get("reason").toString());
                            temp2.put("description", moving.get("description").toString());
                            temp2.put("status", "resolved");
                            temp2.put("reportedOn", moving.get("reportedOn").toString());
                            temp2.put("reportedBy", moving.get("reportedBy").toString());
                            temp2.put("resolvedBy", currentUser);
                            temp2.put("resolvedOn", date.toString());

                            db.collection("solvedReports").document(currentDocID[0]).set(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    System.out.println(task.isSuccessful());
                                    if(task.isSuccessful()){
                                        db.collection("activeReports").document(currentDocID[0]).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    System.out.println("finally complete");
                                                    filteredReports.get(currentPosition[0]).replace("status", "active", "resolved");
                                                    filteredReports.get(currentPosition[0]).put("resolvedOn", date.toString());
                                                    filteredReports.get(currentPosition[0]).put("resolvedBy", currentUser);

                                                    for(int i = 0; i < activeReports.size(); i++){
                                                        if (activeReports.get(i).get("facility").equals(temp.get("facility"))){
                                                            if (activeReports.get(i).get("reason").equals(temp.get("reason"))){
                                                                if (activeReports.get(i).get("description").equals(temp.get("description"))){
                                                                    if (activeReports.get(i).get("reportedOn").equals(temp.get("reportedOn"))){
                                                                        if (activeReports.get(i).get("reportedBy").equals(temp.get("reportedBy"))){
                                                                            activeReports.remove(i);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    solvedReports.add(temp2);
                                                    myAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        /* TouchListener needed for swipe down to hide functionality of the report details card */
        final float[] y2 = new float[2];
        reportDetailsCard.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y2[0] = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        y2[1] = event.getY();

                        /* animated transition */
                        if ((y2[0] - y2[1]) < -300) {
                            ViewPropertyAnimator animate = reportDetailsCard.animate();
                            animate.translationY(reportDetailsCard.getHeight());
                            animate.setDuration(750);
                            animate.start();
                            detailsShowing[0]= false;
                        }
                        break;

                    default:
                }
                return true;
            }
        });


        return root;
    }

    /* Filters reports for the recycler view of reports according to the currently selected
    * drop down menu options and the user search criteria */
    public ArrayList<HashMap<String,String>> filter(ArrayList<HashMap<String, String>> active, ArrayList<HashMap<String, String>> solved, String userInput, String reason, String status){
        ArrayList<HashMap<String,String>> filtered = new ArrayList<>();
        ArrayList<HashMap<String, String>> total = new ArrayList<>();

        if(userInput == null || userInput.isEmpty()) {
            if (reason.toLowerCase().equals("all")) {
                if (status.toLowerCase().equals("all")) {
                    total.addAll(solved);
                    total.addAll(active);
                    return total;
                }
            }
        }

        if(status.equals("All")){
            for(int i = 0; i < active.size(); i++){
                if(reason.equals("All")){
                    if(userInput == null || userInput.isEmpty()){
                        return active;
                    }
                    else if(Objects.requireNonNull(active.get(i).get("facility")).equals(userInput)){
                        filtered.add(active.get(i));
                    }
                }
                else if(Objects.requireNonNull(active.get(i).get("reason")).equals(reason)){
                    if(userInput == null|| userInput.isEmpty()){
                        filtered.add(active.get(i));
                    }
                    else if (Objects.requireNonNull(active.get(i).get("facility")).equals(userInput)){
                        filtered.add(active.get(i));
                    }
                }
            }
            for(int i = 0; i < solved.size(); i++){
                if(reason.equals("All")){
                    if(userInput == null|| userInput.isEmpty()){
                        return solved;
                    }
                    else if(Objects.requireNonNull(solved.get(i).get("facility")).equals(userInput)){
                        filtered.add(solved.get(i));
                    }
                }
                else if(Objects.requireNonNull(solved.get(i).get("reason")).equals(reason)){
                    if(userInput == null|| userInput.isEmpty()){
                        filtered.add(solved.get(i));
                    }
                    else if(Objects.requireNonNull(solved.get(i).get("facility")).equals(userInput)){
                        filtered.add(solved.get(i));
                    }
                }
            }
        }
        else if(status.equals("Active")) {
            for(int i = 0; i < active.size(); i++){
                if(reason.equals("All")){
                    if(userInput == null || userInput.isEmpty()){
                        return active;
                    }
                    else if(Objects.requireNonNull(active.get(i).get("facility")).equals(userInput)){
                        filtered.add(active.get(i));
                    }
                }
                else if(Objects.requireNonNull(active.get(i).get("reason")).equals(reason)){
                    if(userInput == null|| userInput.isEmpty()){
                        filtered.add(active.get(i));
                    }
                    else if (Objects.requireNonNull(active.get(i).get("facility")).equals(userInput)){
                        filtered.add(active.get(i));
                    }
                }
            }

        }
        else{
            for(int i = 0; i < solved.size(); i++){
                if(reason.equals("All")){
                    if(userInput == null|| userInput.isEmpty()){
                        return solved;
                    }
                    else if(Objects.requireNonNull(solved.get(i).get("facility")).equals(userInput)){
                        filtered.add(solved.get(i));
                    }
                }
                else if(Objects.requireNonNull(solved.get(i).get("reason")).equals(reason)){
                    if(userInput == null|| userInput.isEmpty()){
                        filtered.add(solved.get(i));
                    }
                    else if(Objects.requireNonNull(solved.get(i).get("facility")).equals(userInput)){
                        filtered.add(solved.get(i));
                    }
                }
            }
        }

        return filtered;
    }

    /* call back interface */
    public interface RecyclerViewClickListener{
        void onClick(View view, int position);
    }

}
