package com.example.mapp.entityObjects;
import java.util.ArrayList;

public class point{
    private int index;
    private double x;
    private double y;
    private ArrayList<point> neighbors;
    private String name;
    public point(String name)
    {
        this.name = name;
        neighbors = new ArrayList<point>();
    }
    public point(double x, double y)
    {
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<point>();
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
        neighbors.add(p);
    }
    public ArrayList<point> getNeighbors()
    {
        return neighbors;
    }
    public void setNeighbors(ArrayList<point> neighbors)
    {
        this.neighbors = neighbors;
    }
    public void setIndex(int i)
    {
        this.index = i;
    }
    public int getIndex()
    {
        return this.index;
    }
    public double distance(point p)
    {
        return Math.sqrt(Math.pow((this.x - p.getX()),2) + Math.pow((this.y - p.getY()), 2));
    }
    public String toString()
    {
        String neighborString = "";
        for(point p : neighbors)
        {
            neighborString += "," + p.getIndex();
        }
        return name + ";" + x + "," + y + ";" + neighborString.substring(1);
    }
}