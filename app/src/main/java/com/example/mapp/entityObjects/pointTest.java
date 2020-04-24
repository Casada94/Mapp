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
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class pointTest{
    private static HashMap<String, point> points;
    static Scanner in = new Scanner(System.in);

    public static void main(String[]args) throws NumberFormatException, IOException
    {
        points = new HashMap<>();
        updatePoints();
        checkEdges();
        writePoints();
        System.exit(0);
        readPointsA();
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
                case 3: FindPath(points);
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
            for(String k : points.keySet())
            {
                System.out.println("(" + (k) + ") ");
            }
            System.out.println("0 to exit");
            stop = in.nextInt();
            if(stop != 0 && !p.getNeighbors().contains(String.valueOf(stop)))
            {
                p.addNeighbor(points.get(String.valueOf(stop)));
                points.get(String.valueOf(stop)).addNeighbor(p);
            }
        }
//        p.setIndex(points.size());
        points.put(p.getName(), p);
        System.out.println("point added!");
    }

    public static void viewPoints()
    {
        Set<String> keys = points.keySet();
        int i = 0;
        for(String key : keys)
        {
            point p = points.get(key);
            System.out.println("(" + i++ + ") [" + p.getName() + "] " + p.getX() + "," + p.getY());
        }
    }

    public static void FindPath(HashMap<String, point> points)
    {
        int n = points.keySet().size();
        List<point> pointsA = new ArrayList<point>(n);
        for (String x : points.keySet())
            pointsA.add(points.get(x));
        System.out.println("Select Starting point: ");
        viewPoints();
        int sel = in.nextInt();
        point start = pointsA.get(sel);
        System.out.println("Select destination: ");
        viewPoints();
        sel = in.nextInt();
        point dest = pointsA.get(sel);
        ArrayList<point> neighbors = new ArrayList<point>();
        neighbors.add(start);
        HashMap<String, point> parent = new HashMap<String, point>();
        HashMap<String, Double> distance = new HashMap<String, Double>();
        Set<String> keys = points.keySet();
        for(String key : keys)
        {
            distance.put(key, Double.MAX_VALUE);
            parent.put(key, null);
        }
        distance.put(start.getName(), 0.0);
        System.out.println("Distance: " );
        for(String x : distance.keySet())
        {
            System.out.println(x + " " + distance.get(x));
        }
        while(neighbors.size() > 0)
        {
            point curr = neighbors.get(0);
            neighbors.remove(curr);
            for(String p : curr.getNeighbors())
            {
                if(parent.get(p) == null && points.get(p) != start)
                {
                    neighbors.add(points.get(p));
                }
                double newDistance = Double.MAX_VALUE;
                if(points.get(p) != null && distance.get(curr.getName()) != null)
                    newDistance = curr.distance(points.get(p)) + distance.get(curr.getName());
                if(newDistance < distance.get(p))
                {
                    distance.put(p, newDistance);
                    parent.put(p, curr);
                }
            }
        }

        ArrayList<point> path = new ArrayList<point>();
        while(dest != start)
        {
            path.add(0, dest);
            dest = parent.get(dest.getName());
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
        ArrayList<point> pointsA = gson.fromJson(reader, new TypeToken<HashMap<String, point>>(){}.getType());
        if(points == null)
            points = new HashMap<String, point>();
        for(point p : pointsA)
            points.put(p.getName(), p);
        if(points == null)
            points = new HashMap<String, point>();
    }
    public static void readPointsA() throws NumberFormatException, IOException
    {
        File file = new File("app\\src\\main\\java\\com\\example\\mapp\\entityObjects\\points.txt");
        file.createNewFile();
        FileReader reader = new FileReader(file);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ArrayList<point> pointsA = gson.fromJson(reader, new TypeToken<List<point>>(){}.getType());
        if(points == null)
            points = new HashMap<String, point>();
        for(point p : pointsA)
            points.put(p.getName(), p);
        if(points == null)
            points = new HashMap<String, point>();
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

    public static void updatePoints() throws IOException{
        File file = new File("app\\src\\main\\java\\com\\example\\mapp\\entityObjects\\points.txt");
        FileReader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);
        String st;
        while((st = br.readLine()) != null)
        {
            String [] txt = st.split("//");
            String doc = txt[0];
            Double x = 0.0;
            Double y = 0.0;
            ArrayList<String> neighbors = new ArrayList<>();
            x = Double.valueOf(txt[1]);
            y = Double.valueOf(txt[2]);
            if(txt.length > 3)
            {
                String [] N = txt[3].split(";");
                for(String n : N)
                {
                    n = n.replaceAll("\\s","");
                    neighbors.add(n);
                }
            }
            point p;
            String name = doc.split("-")[1];
            if(doc.split("-")[0].equals("p"))
            {
                p = new point(name, x, y, neighbors);
            }
            else
                p = new Building(name, x, y);
            points.put(doc, p);
        }
    }

    public static void checkEdges(){
        Set<String> S = points.keySet();
        for(String name : S)
        {
            point p = points.get(name);
            for(String n : p.getNeighbors())
            {
                try{
                    int index = Integer.parseInt(n);
                    System.out.println("p-" + n);
                    points.get("p-" + n).addNeighbor(p);
                } catch( NumberFormatException nfe)
                {
                    System.out.println(p.getName() + "b-" + n);
                    points.get("b-" + n).addNeighbor(p);
                }
            }
        }
    }
}


