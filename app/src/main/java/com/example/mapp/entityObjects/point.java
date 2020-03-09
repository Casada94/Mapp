package com.example.mapp.entityObjects;
import java.util.ArrayList;

public class point{
    private int index;
    private double x;
    private double y;
    private ArrayList<Integer> neighbors;
    private String name;
    public point(String name)
    {
        this.name = name;
        neighbors = new ArrayList<Integer>();
    }
    public point(double x, double y)
    {
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<Integer>();
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
        neighbors.add(p.getIndex());
    }
    public ArrayList<Integer> getNeighbors()
    {
        return neighbors;
    }
    public void setNeighbors(ArrayList<Integer> neighbors)
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
        for(int i = 0; i < neighbors.size(); i++)
        {
            neighborString += neighbors.get(i) + ",";
        }
        return name + ";" + x + "," + y + ";" + neighborString;
    }
}