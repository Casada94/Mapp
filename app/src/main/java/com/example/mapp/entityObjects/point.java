package com.example.mapp.entityObjects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class point{
    private double x;
    private double y;
    private List<String> neighbors;
    private String name;

    public point()
    {
    }

    public point(String name)
    {
        this.name = name;
        this.x = 0;
        this.y = 0;
        neighbors = new ArrayList<String>();
    }
    public point(String name, double x, double y)
    {
        this.name = name;
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<String>();
    }
    public point(String name, double x, double y, ArrayList<String> neighbors)
    {
        this.name = name;
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
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
    public double getY()
    {
        return y;
    }
    public void setX(double x)
    {
        this.x = x;
    }
    public void setY(double y)
    {
        this.y = y;
    }
    public void addNeighbor(point p)
    {
        neighbors.add(p.getName());
    }
    public List<String> getNeighbors()
    {
        return neighbors;
    }
    public void setNeighbors(ArrayList<String> neighbors)
    {
        this.neighbors = neighbors;
    }
//    public void setIndex(int i)
//    {
//        this.index = i;
//    }
//    public int getIndex()
//    {
//        return this.index;
//    }
    public double distance(point p)
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