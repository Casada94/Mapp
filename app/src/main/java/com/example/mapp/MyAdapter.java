package com.example.mapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.mapp.Classes;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;


/** This class is needed for the implementation of the list of classes in the current schedule fragment
 * TEXT VIEW BELOW WILL BE REPLACED WITH A CUSTOM VIEW **/
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Classes> schedule = new ArrayList<>();
    private Context context;

    /* Provide a reference to the views for each data item
       Complex data items may need more than one view per item, and
       you provide access to all the views for a data item in a view holder  */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView className;
        private TextView location;
        private TextView days;
        private TextView time;
        private TextView isPm;


        public MyViewHolder(View v) {
            super(v);

            className = v.findViewById(R.id.className);
            location = v.findViewById(R.id.location);
            days = v.findViewById(R.id.days);
            time = v.findViewById(R.id.thisClassTime);
            isPm = v.findViewById(R.id.isPm);
        }

        public void setClassName(String className) {
            this.className.setText(className);
        }

        public void setLocation(String location) {
            this.location.setText(location);
        }
        public void setDays(String days){
            this.days.setText(days);
        }
        public void setTime(String time){
            this.time.setText(time);
        }
        public void setIsPm(Boolean isPm){
            if(isPm)
                this.isPm.setText("PM");
            else
                this.isPm.setText("AM");
        }
    }

    /* Provide a suitable constructor (depends on the kind of dataset)*/
    public MyAdapter(Context context, ArrayList<Classes> schedule) {
        this.context = context;
        this.schedule = schedule;
    }

    /* Create new views (invoked by the layout manager)*/
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        /* create a new view*/
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext()) ;
        View view = layoutInflater.inflate(R.layout.schedule_view, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    /* Replace the contents of a view (invoked by the layout manager) */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Classes classes = schedule.get(position);

        holder.setClassName(classes.getClassName());
        holder.setLocation(classes.getLocation());
        holder.setDays(classes.getDays());
        holder.setTime(classes.getTime());
        holder.setIsPm(classes.isPm());

    }

    /* Return the size of your dataset (invoked by the layout manager) */
    @Override
    public int getItemCount() {
        return schedule.size();
    }
}
