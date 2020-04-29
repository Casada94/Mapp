package com.example.mapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
public class reportAdapter extends RecyclerView.Adapter<reportAdapter.MyViewHolder>{
    private ArrayList<HashMap<String, String>> reports = new ArrayList<>();
    private Context context;
    private ReportsFragment.RecyclerViewClickListener clickListener;
    /* Provide a reference to the views for each data item
       Complex data items may need more than one view per item, and
       you provide access to all the views for a data item in a view holder  */
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView buildingName;
        private TextView reportReason;
        private TextView status;
        private int position;
        private CardView cardView;
        private ReportsFragment.RecyclerViewClickListener mListener;


        public MyViewHolder(View v, ReportsFragment.RecyclerViewClickListener listener) {
            super(v);

            buildingName = v.findViewById(R.id.b_name);
            reportReason = v.findViewById(R.id.reason);
            status = v.findViewById(R.id.status);
            cardView = v.findViewById(R.id.individual_report_cards);
            mListener = listener;

            cardView.setOnClickListener(this);

        }

        /* Click call back interface to recognize clicks on individual elements of the recycler view */
        @Override
        public void onClick(View view){
            mListener.onClick(view, getAdapterPosition());
        }

        /* Setters for the textviews of the individual elements within a given line in the recycler
        * view */
        public void setBuildingName(String buildingName) { this.buildingName.setText(buildingName); }
        public void setReason(String reason) { this.reportReason.setText(reason); }
        public void setStatus(String status){ this.status.setText(status); }
        public void setStatusColor(String statusColor){
            if(statusColor.equals("red"))
                this.status.setTextColor(Color.RED);
            else
                this.status.setTextColor(Color.GREEN);
        }
        public void setPosition(int position){this.position = position;}

    }

    /* Provide a suitable constructor */
    public reportAdapter(Context context, ArrayList<HashMap<String,String>> reports, ReportsFragment.RecyclerViewClickListener listener) {
        this.context = context;
        this.reports = reports;
        clickListener = listener;
    }

    /* Create new views (invoked by the layout manager)*/
    @Override
    public reportAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        /* create a new view*/
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext()) ;
        View view = layoutInflater.inflate(R.layout.reports_view, parent, false);

        MyViewHolder vh = new MyViewHolder(view, clickListener);
        return vh;
    }

    /* Replace the contents of a view (invoked by the layout manager) */
    @Override
    public void onBindViewHolder(reportAdapter.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final HashMap<String, String> report = reports.get(position);

        holder.setBuildingName(report.get("facility"));
        holder.setReason(report.get("reason"));

        if(report.get("status").equals("active"))
            holder.setStatusColor("red");
        else
            holder.setStatusColor("green");

        holder.setStatus(report.get("status"));
        holder.setPosition(position);

    }

    /* Return the size of your dataset (invoked by the layout manager) */
    @Override
    public int getItemCount() {
        return reports.size();
    }

    public ArrayList<HashMap<String, String >> getReports(){ return reports;}
}
