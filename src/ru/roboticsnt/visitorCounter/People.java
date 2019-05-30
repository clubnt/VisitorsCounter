package ru.roboticsnt.visitorCounter;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Random;


public class People
{
    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_LEAVING = 1;

    private static final int _MAX_PATH_SIZE = 20;

    private int _type;
    private int _id;
    private Scalar _color;
    private Point _leftTopPoint;
    private Point _rightBottomPoint;

//    private boolean _isCounted = false;

    private ArrayList<Point> _pathPointsList;

    private int _framesWithoutDetection = 0;

    private static int _nextId = 1;

    private static int getNextId()
    {
        return _nextId++;
    }

    private static Scalar getRandomColor()
    {
        Random rand = new Random();

        double r = rand.nextDouble() * 255;
        double g = rand.nextDouble() * 255;
        double b = rand.nextDouble() * 255;

        return new Scalar(b, g, r);
    }


    public People(Point leftTopPoint, Point rightBottomPoint)
    {
        _id = getNextId();
        _color = getRandomColor();

        _pathPointsList = new ArrayList<>();

        update(leftTopPoint, rightBottomPoint);

        Point center = getCenter();

        if(center.x < 0.5)
        {
            _type = TYPE_INCOMING;
        }else
        {
            _type = TYPE_LEAVING;
        }

    }


    public void update(Point leftTopPoint, Point rightBottomPoint)
    {
        _framesWithoutDetection = 0;

        _leftTopPoint = leftTopPoint;
        _rightBottomPoint = rightBottomPoint;
        Point _center = new Point((leftTopPoint.x + rightBottomPoint.x) / 2, (leftTopPoint.y + rightBottomPoint.y) / 2);

        _pathPointsList.add(_center);

        if(_pathPointsList.size() > _MAX_PATH_SIZE)
        {
            _pathPointsList.remove(0);
        }
    }


    public void incrementFramesWithoutDetection()
    {
        _framesWithoutDetection++;
    }


    public int getFramesWithoutDetectionCount()
    {
        return _framesWithoutDetection;
    }


    public int getType()
    {
        return _type;
    }


//    public void setCounted(boolean value)
//    {
//        _isCounted = value;
//    }
//
//
//    public boolean isCounted()
//    {
//        return _isCounted;
//    }


    public int getId()
    {
        return _id;
    }


    public Scalar getColor()
    {
        return _color;
    }


    public Point getLeftTopPoint()
    {
        return _leftTopPoint;
    }


    public Point getRightBottomPoint()
    {
        return _rightBottomPoint;
    }


    public Point getCenter()
    {
        return _pathPointsList.get(_pathPointsList.size() - 1);
    }


    public Point getPrevCenter()
    {
        if(_pathPointsList.size() >= 2)
        {
            return _pathPointsList.get(_pathPointsList.size() - 2);
        }else
        {
            return null;
        }
    }


    public ArrayList<Point> getPathPointsList()
    {
        return _pathPointsList;
    }

}