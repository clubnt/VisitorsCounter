package ru.roboticsnt.visitorCounter;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;


public class Counter
{

    private ArrayList<People> _peoplesList;

    private int _incomingCount;

    private int _leavingCount;

    private final double _MIN_DISTANCE_BETWEEN_PEOPLE = 0.1;

    private final double _CONFIDENCE_THRESHOLD = 0.7;

    private final int _PEOPLE_CLASS_ID = 15;

    private final int _MAX_FRAMES_WITHOUT_DETECTION = 5;


    public Counter()
    {
        _peoplesList = new ArrayList<>();

        _incomingCount = 0;
        _leavingCount = 0;
    }


    public ArrayList<People> getPeoplesList()
    {
        return _peoplesList;
    }


    public int getIncomingCount()
    {
        return _incomingCount;
    }


    public int getLeavingCount()
    {
        return _leavingCount;
    }


    public void update(Mat result)
    {

        for (int i = 0; i < result.rows(); ++i)
        {
            double confidence = result.get(i, 2)[0];
            int classId = (int)result.get(i, 1)[0];

            // Если определился не человек или не прошел по порогу уверенности, то пропускаем
            if(classId != _PEOPLE_CLASS_ID || confidence < _CONFIDENCE_THRESHOLD)
            {
                continue;
            }

            double xLeftTop = result.get(i, 3)[0];
            double yLeftTop = result.get(i, 4)[0];
            double xRightBottom = result.get(i, 5)[0];
            double yRightBottom = result.get(i, 6)[0];

            Point leftTopPoint = new Point(xLeftTop, yLeftTop);
            Point rightBottomPoint = new Point(xRightBottom, yRightBottom);
            Point centerPoint = new Point((xLeftTop + xRightBottom) / 2, (yLeftTop + yRightBottom) / 2);

            boolean isNewPeople = true;

            for (People people : _peoplesList)
            {
                people.incrementFramesWithoutDetection();

                Point peopleCenter = people.getCenter();

                double distance = distanceBetweenPoints(peopleCenter, centerPoint);

                if (distance < _MIN_DISTANCE_BETWEEN_PEOPLE)
                {
                    people.update(leftTopPoint, rightBottomPoint);
                    isNewPeople = false;
                    break;
                }
            }

            if (isNewPeople)
            {
                _peoplesList.add(new People(leftTopPoint, rightBottomPoint));
            }
        }

        /*
        * Проходим по списку людей
        *  - проверяем на пересечение с центральной линией
        *  - удаляем из списка того кто не определялся больше чем _MAX_FRAMES_WITHOUT_DETECTION кадров
        * */

        for (int j = 0; j < _peoplesList.size(); j++)
        {
            People people = _peoplesList.get(j);

            Point prevCenter = people.getPrevCenter();

            if (prevCenter != null)
            {
                Point curCenter = people.getCenter();

                if(curCenter.x > 0.5 && prevCenter.x <= 0.5)
                {
                    if(people.getType() == People.TYPE_INCOMING)
                    {
                        _incomingCount++;
                    }else
                    {
                        _leavingCount--;
                    }
                }

                if(curCenter.x < 0.5 && prevCenter.x >= 0.5)
                {
                    if(people.getType() == People.TYPE_INCOMING)
                    {
                        _incomingCount--;
                    }else
                    {
                        _leavingCount++;
                    }
                }
            }

            people.incrementFramesWithoutDetection();

            if(people.getFramesWithoutDetectionCount() >= _MAX_FRAMES_WITHOUT_DETECTION)
            {
                _peoplesList.remove(people);
            }
        }
    }


    private double distanceBetweenPoints(Point p1, Point p2)
    {
        double xDiff = p1.x - p2.x;
        double yDiff = p1.y - p2.y;
        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }

}
