package com.example.mapp.entityObjects;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
//			System.out.println("parent : ");
//			for(point p : parent)
//			{
//				String name = "";
//				if(p != null)
//					name = p.getName();
//				System.out.println(name);
//			}
//			System.out.println("distance: ");
//			for(double d : distance)
//			{
//				System.out.println(d);
//			}
            for(point p : curr.getNeighbors())
            {
                if(parent[p.getIndex()] == null && p != start)
                {
                    neighbors.add(p);
                }
                double newDistance = curr.distance(p) + distance[curr.getIndex()];
                if(newDistance < distance[p.getIndex()])
                {
                    distance[p.getIndex()] = newDistance;
                    parent[p.getIndex()] = curr;
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
        points = new ArrayList<point>();
        File file = new File("app\\src\\main\\java\\com\\example\\mapp\\entityObjects\\points.txt");
        file.createNewFile();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        int count = 0;
        ArrayList<int[]> neighborsList = new ArrayList<int[]>();
        while((line = br.readLine()) != null)
        {
            String[] param = line.split(";");
            point p = new point(param[0]);
            String[] coord = param[1].split(",");
            p.setX(Double.valueOf(coord[0]));
            p.setY(Double.valueOf(coord[1]));
            String[] neighbors = param[2].split(",");
            int[] neighborIndex = new int[neighbors.length];
            for(int i = 0; i < neighbors.length; i++)
            {
                neighborIndex[i] = Integer.valueOf(neighbors[i]);
            }
            neighborsList.add(neighborIndex);
            p.setIndex(count++);
            points.add(p);
        }
        for(int i = 0; i < neighborsList.size(); i++)
        {
            ArrayList<point> neighbors = new ArrayList<point>();
            for(int j = 0; j < neighborsList.get(i).length; j++)
            {
                neighbors.add(points.get(neighborsList.get(i)[j]));
            }
            points.get(i).setNeighbors(neighbors);
        }
        System.out.println("points read");
    }

    public static void writePoints() throws FileNotFoundException, UnsupportedEncodingException
    {
        File file = new File("app\\src\\main\\java\\com\\example\\mapp\\entityObjects\\points.txt");
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        for(int i = 0; i < points.size(); i++)
        {
            writer.println(points.get(i).toString());
        }
        writer.close();
    }
}


