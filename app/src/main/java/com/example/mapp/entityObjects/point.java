package com.example.mapp.entityObjects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class point{
    private double x;
    private double y;
    private ArrayList<String> neighbors;
    private String name;
    private ArrayList<String> utilities;

    public point()
    {
    }

    public point(String name)
    {
        this.name = name;
        this.x = 0;
        this.y = 0;
        neighbors = new ArrayList<String>();
        utilities = new ArrayList<String>();
    }
    public point(String name, double x, double y)
    {
        this.name = name;
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<String>();
        utilities = new ArrayList<String>();
    }
    public point(String name, double x, double y, ArrayList<String> neighbors)
    {
        this.name = name;
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
        this.utilities = new ArrayList<>();
    }
    public point(String name, double x, double y, ArrayList<String> neighbors, ArrayList<String> utilities)
    {
        this.name = name;
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
        this.utilities = utilities;
    }


    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
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
        if(!neighbors.contains(p.name))
            neighbors.add(p.getName());
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
    public String toString()
    {
        String neighborString = "";
        for(int i = 0; i < neighbors.size(); i++)
        {
            neighborString += neighbors.get(i) + ",";
        }
        return name + " (" + x + "," + y + "): " + neighborString;
    }

}