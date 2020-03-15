package com.example.mapp;

public class Polygon {

    String name;
    SimplePoint[] points;
    int maxX, minX;
    int maxY, minY;
    int n;



    public Polygon(){
        points = new SimplePoint[4];
        n =0;
    }

    public Polygon(int[] x, int y[], int n, String name){
        this.name = name;
        points = new SimplePoint[n];
        maxX = x[0];  minX = x[0];
        maxY = y[0];  minY = y[0];

        for(int i = 0; i<n; i++){
            maxX = Math.max(x[i], maxX);
            minX = Math.min(x[i], minX);
            maxY = Math.max(y[i], maxY);
            minY = Math.min(y[i], minY);

            points[i] = new SimplePoint(x[i],y[i]);
        }
        this.n = n;
    }

    static boolean onSegment(SimplePoint p, SimplePoint q, SimplePoint r)
    {
        if (q.x <= Math.max(p.x, r.x) &&
                q.x >= Math.min(p.x, r.x) &&
                q.y <= Math.max(p.y, r.y) &&
                q.y >= Math.min(p.y, r.y))
        {
            return true;
        }
        return false;
    }

    static int orientation(SimplePoint p, SimplePoint q, SimplePoint r)
    {
        int val = (q.y - p.y) * (r.x - q.x)
                - (q.x - p.x) * (r.y - q.y);

        if (val == 0)
        {
            return 0; // colinear
        }
        return (val > 0) ? 1 : 2; // clock or counterclock wise
    }

    static boolean doIntersect(SimplePoint p1, SimplePoint q1, SimplePoint p2, SimplePoint q2)
    {
        // Find the four orientations needed for
        // general and special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
        {
            return true;
        }

        // Special Cases
        // p1, q1 and p2 are colinear and
        // p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1))
        {
            return true;
        }

        // p1, q1 and p2 are colinear and
        // q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1))
        {
            return true;
        }

        // p2, q2 and p1 are colinear and
        // p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2))
        {
            return true;
        }

        // p2, q2 and q1 are colinear and
        // q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2))
        {
            return true;
        }

        // Doesn't fall in any of the above cases
        return false;
    }
    public boolean contains(int x, int y){
        if(x > minX && x < maxX && y > minY && y < maxY){
            SimplePoint userPoint = new SimplePoint(x,y);
            SimplePoint extreme = new SimplePoint(5000, userPoint.y);

            int count = 0, i =0;
            do {
                int next = (i + 1) % n;

                if(doIntersect(points[i], points[next], userPoint, extreme)){
                    if(orientation(points[i], userPoint, points[next]) == 0){
                        return onSegment(points[i], userPoint, points[next]);
                    }
                    count++;
                }
                i = next;
            }while(i !=0);
            return (count % 2 ==1);
        }
        else
            return false;
    }
}
