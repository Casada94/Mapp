package com.example.mapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.example.mapp.entityObjects.point;

import java.util.ArrayList;
import java.util.List;

import io.opencensus.resource.Resource;

public class RouteDrawer {
    private Paint mPaint = new Paint();
    private List<Point> points = new ArrayList<>();
    private Path mPath = new Path();

    public RouteDrawer() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(7f);
    }

    public RouteDrawer(List<Point> listOfPoints) {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(7f);

        points = new ArrayList<Point>(listOfPoints);
    }

    public RouteDrawer(ArrayList<point> points)
    {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(7f);

        this.points = new ArrayList<Point>();
        for(point p : points)
        {
            this.points.add(new Point((int) p.getX(), (int) p.getY()));
        }
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void addPointList(List<Point> listOfPoints)
    {
        points.addAll(listOfPoints);
    }

    public void drawPath(Context context, Canvas canvas) {
        mPath.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            mPath.lineTo(points.get(i).x, points.get(i).y);
            }
        canvas.drawPath(mPath, mPaint);
        Bitmap marker = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker);
        canvas.drawBitmap(marker, points.get(points.size() - 1).x, points.get(points.size() - 1).y, null);
        canvas.drawCircle(points.get(points.size() - 1).x, points.get(points.size() - 1).y, 10, mPaint);
    }

    public void drawCircle(Canvas canvas, Point point, int radius) {
        canvas.drawCircle(point.x, point.y, radius, mPaint);
    }

    public void resetPaths() {
        mPath.reset();
    }

    public void clearPoints() {
        points.clear();
    }
}