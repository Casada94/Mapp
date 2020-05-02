package com.example.mapp.entityObjects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class point{
    private double x;
    private double y;
    private ArrayList<String> neighbors;
    private String abbr;
    private ArrayList<String> utilities;

    public point()
    {
    }

    public point(String abbr)
    {
        this.abbr = abbr;
        this.x = 0;
        this.y = 0;
        neighbors = new ArrayList<String>();
        utilities = new ArrayList<String>();
    }
    public point(String abbr, double x, double y)
    {
        this.abbr = abbr;
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<String>();
        utilities = new ArrayList<String>();
    }
    public point(String abbr, double x, double y, ArrayList<String> neighbors)
    {
        this.abbr = abbr;
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
        this.utilities = new ArrayList<>();
    }
    public point(String abbr, double x, double y, ArrayList<String> neighbors, ArrayList<String> utilities)
    {
        this.abbr = abbr;
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
        this.utilities = utilities;
    }


    public String getAbbr()
    {
        return abbr;
    }
    public void setAbbr(String abbr)
    {
        this.abbr = abbr;
    }
    public double getX()
    {
        return x;
    }
    public void setX(double x)
    {
        this.x = x;
    }
    public double getY()
    {
        return y;
    }
    public void setY(double y)
    {
        this.y = y;
    }
    public ArrayList<String> getNeighbors()
    {
        return neighbors;
    }
    public void setNeighbors(ArrayList<String> neighbors)
    {
        this.neighbors = neighbors;
    }
    public void addNeighbor(point p)
    {
        if(!neighbors.contains(p.abbr))
            neighbors.add(p.getAbbr());
    }
    public ArrayList<String> getUtilities()
    {
        return utilities;
    }
    public void setUtilities(ArrayList<String> utilities)
    {
        this.utilities = utilities;
    }    public double distance(point p)
    {
        return Math.sqrt(Math.pow((this.x - p.getX()),2) + Math.pow((this.y - p.getY()), 2));
    }
    public boolean hasUtility(String utility)
    {
        if(this.utilities.size() != 0)
            for(String str : utilities)
            {
                if(str.toLowerCase().charAt(0) == utility.toLowerCase().charAt(0))
                    return true;
            }
        return false;
    }
    public String toString()
    {
        String neighborString = "";
        for(int i = 0; i < neighbors.size(); i++)
        {
            neighborString += neighbors.get(i) + ",";
        }
        return abbr + " (" + x + "," + y + "): " + neighborString;
    }

}