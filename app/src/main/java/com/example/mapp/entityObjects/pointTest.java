package com.example.mapp.entityObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
public class pointTest{
    private static ArrayList<point> points;
    static Scanner in = new Scanner(System.in);

    public static void main(String[]args) throws NumberFormatException, IOException
    {
        readPoints();
        int option = 1;
        while(option != 0)
        {
            System.out.println("Enter option: ");
            System.out.println("(1) Add point (2) View points (3) Find Path");
            option = in.nextInt();
            switch(option)
            {
                case 1: makePoint();
                    break;
                case 2: viewPoints();
                    break;
                case 3: FindPath();
                    break;
                case 0:
                    break;
                default: System.out.println("Invalid option");
            }
        }
        writePoints();
        in.close();
    }
    public static void makePoint()
    {
        System.out.println("Enter point name: ");
        String name = in.next();
        point p = new point(name);
        System.out.println("Enter X coordinate");
        p.setX(in.nextDouble());
        System.out.println("Enter Y coordinate");
        p.setY(in.nextDouble());
        int stop = 1;
        while(stop != 0)
        {
            System.out.println("Select neighbor(s): ");
            for(int i = 0; i < points.size(); i++)
            {
                System.out.println("(" + (i+1) + ") " + points.get(i).getName());
            }
            System.out.println("0 to exit");
            stop = in.nextInt();
            if(stop != 0 && !p.getNeighbors().contains(points.get(stop-1)))
            {
                p.addNeighbor(points.get(stop-1));
                points.get(stop-1).addNeighbor(p);
            }
        }
        p.setIndex(points.size());
        points.add(p);
        System.out.println("point added!");
    }

    public static void viewPoints()
    {
        for(int i = 0; i < points.size(); i++)
        {
            point p = points.get(i);
            System.out.println("[" + (i+1) + "] " + p.getName() + " " + p.getX() + "," + p.getY());
        }
    }

    public static void FindPath()
    {
        System.out.println("Select Starting point: ");
        viewPoints();
        int sel = in.nextInt();
        point start = points.get(sel -1);
        System.out.println("Select destination: ");
        viewPoints();
        sel = in.nextInt();
        point dest = points.get(sel - 1);
        ArrayList<point> neighbors = new ArrayList<point>();
        neighbors.add(start);
        point[] parent = new point[points.size()];
        double[] distance = new double[points.size()];
        for(int i = 0; i < distance.length; i++)
        {
            distance[i] = Double.MAX_VALUE;
        }
        distance[start.getIndex()] = 0;
        while(neighbors.size() > 0)
        {
            point curr = neighbors.get(0);
            neighbors.remove(curr);
            for(int p : curr.getNeighbors())
            {
                if(parent[p] == null && points.get(p) != start)
                {
                    neighbors.add(points.get(p));
                }
                double newDistance = curr.distance(points.get(p)) + distance[curr.getIndex()];
                if(newDistance < distance[p])
                {
                    distance[p] = newDistance;
                    parent[p] = curr;
                }
            }
        }

        ArrayList<point> path = new ArrayList<point>();
        while(dest != start)
        {
            path.add(0, dest);
            dest = parent[dest.getIndex()];
        }
        path.add(0, start);
        for(int i = 0; i < path.size()-1; i++)
        {
            String dir = "";
            double y = path.get(i).getY() - path.get(i+1).getY();
            if(y > 50)
                dir += "north";
            else if(y < -50)
                dir += "south";
            double x = path.get(i).getX() - path.get(i+1).getX();
            if(x > 50)
                dir += "west";
            else if(x < -50)
                dir += "east";
            System.out.println("Head " + dir + " towards: " + path.get(i+1).getName());
        }
        System.out.println("Destination reached!");
    }

    public static void readPoints() throws NumberFormatException, IOException
    {
        File file = new File("app\\src\\main\\java\\com\\example\\mapp\\entityObjects\\points.txt");
        file.createNewFile();
        FileReader reader = new FileReader(file);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        points = gson.fromJson(reader, new TypeToken<List<point>>(){}.getType());
        if(points == null)
            points = new ArrayList<point>();
    }

    public static void writePoints() throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        File file = new File("app\\src\\main\\java\\com\\example\\mapp\\entityObjects\\points.txt");
        FileWriter writer = new FileWriter(file);
        writer.write(gson.toJson(points));
        writer.flush();
        writer.close();

    }
}


