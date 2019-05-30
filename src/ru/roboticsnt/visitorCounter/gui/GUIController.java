package ru.roboticsnt.visitorCounter.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import ru.roboticsnt.visitorCounter.CameraController;
import ru.roboticsnt.visitorCounter.Counter;
import ru.roboticsnt.visitorCounter.People;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;


public class GUIController
{
    private final int _CENTRE_CIRCLE_RADIUS = 10;
    private final int _LINE_THICKNESS = 2;
    private final int _FONT_SCALE = 3;

    private final Scalar _DIVIDING_LINE_COLOR = new Scalar(10, 10, 240);
    private final int _DIVIDING_LINE_THICKNESS = 6;

    private Counter _counter;
    private CameraController _cameraController;


    @FXML
    ImageView imageView;

    @FXML
    TextField leavingCountTf;

    @FXML
    TextField incomingCountTf;

    @FXML
    AnchorPane imagePane;

    @FXML
    Button startBut;

    @FXML
    Button resetBut;

    @FXML
    Slider thresholdSlider;

    @FXML
    Slider minDistanceSlider;


    public void init(Counter counter, CameraController cameraController)
    {
        _counter = counter;
        _cameraController = cameraController;

        leavingCountTf.setText("0");
        incomingCountTf.setText("0");

        thresholdSlider.setValue(_counter.getConfidenceThreshold());
        minDistanceSlider.setValue(_counter.getMinDistanceBetweenPeoples());

        startBut.setOnAction(actionEvent ->
        {
            if(_cameraController.isStarted())
            {
                _cameraController.stop();
                startBut.setText("Старт");
            }else
            {
                _cameraController.start();
                startBut.setText("Стоп");
            }
        });

        resetBut.setOnAction(actionEvent ->
        {
            _counter.reset();
        });

        thresholdSlider.valueProperty().addListener((observableValue, number, t1) ->
        {
            System.out.println(thresholdSlider.getValue());
            _counter.setConfidenceThreshold(thresholdSlider.getValue());
        });

        minDistanceSlider.valueProperty().addListener((observableValue, number, t1) ->
        {
            System.out.println(minDistanceSlider.getValue());
            _counter.setMinDistanceBetweenPeoples(minDistanceSlider.getValue());
        });
    }


    public void update(Mat frame)
    {
        int frameSize = CameraController.FRAME_SIZE;

        ArrayList<People> peoplesList = _counter.getPeoplesList();

        for (People people : peoplesList)
        {
            Scalar drawColor = people.getColor();

            // Draw rectangle
            Point leftPoint = new Point(people.getLeftTopPoint().x * frameSize, people.getLeftTopPoint().y * frameSize);
            Point rightPoint = new Point(people.getRightBottomPoint().x * frameSize, people.getRightBottomPoint().y * frameSize);
            Imgproc.rectangle(frame, leftPoint, rightPoint, drawColor, _LINE_THICKNESS);

            // Draw center point
            Point centerPoint = new Point(people.getCenter().x * frameSize, people.getCenter().y * frameSize);
            Imgproc.circle(frame, centerPoint, _CENTRE_CIRCLE_RADIUS, drawColor, Imgproc.FILLED);

            // Draw ID
            Point textPosition = new Point(centerPoint.x + 10, centerPoint.y - 10);
            Imgproc.putText(frame, "ID:" + people.getId(), textPosition, Imgproc.FONT_HERSHEY_PLAIN, _FONT_SCALE, drawColor, _LINE_THICKNESS);

            // Draw people path
            ArrayList<Point> pointArrayList = people.getPathPointsList();
            ArrayList<Point> pointArrayList2 = new ArrayList<>();
            for (Point point : pointArrayList)
            {
                pointArrayList2.add(new Point(point.x * CameraController.FRAME_SIZE, point.y * CameraController.FRAME_SIZE));
            }
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint.fromList(pointArrayList2);
            List<MatOfPoint> matOfPointList = new ArrayList();
            matOfPointList.add(matOfPoint);
            Imgproc.polylines(frame, matOfPointList, false, drawColor, _LINE_THICKNESS);
        }

        // Draw counter values on frame
        Point incomingTextPosition = new Point(frameSize * 0.2, frameSize * 0.7);
        Imgproc.putText(frame, Integer.toString(_counter.getIncomingCount()), incomingTextPosition, Imgproc.FONT_HERSHEY_PLAIN, 25, _DIVIDING_LINE_COLOR, 6);
        Point leavingTextPosition = new Point(frameSize * 0.7, frameSize * 0.7);
        Imgproc.putText(frame, Integer.toString(_counter.getLeavingCount()), leavingTextPosition, Imgproc.FONT_HERSHEY_PLAIN, 25, _DIVIDING_LINE_COLOR, 6);


        // Show peoples count
        incomingCountTf.setText(Integer.toString(_counter.getIncomingCount()));
        leavingCountTf.setText(Integer.toString(_counter.getLeavingCount()));

        // Draw dividing line
        Point p1 = new Point(frameSize / 2, 30);
        Point p2 = new Point(frameSize / 2, frameSize - 30);
        Imgproc.line(frame, p1, p2, _DIVIDING_LINE_COLOR, _DIVIDING_LINE_THICKNESS);


        showFrame(frame);
    }


    private void showFrame(Mat frame)
    {
        // Resize to container dimensions and offset x
        double windowWidth = imagePane.getWidth();
        double windowHeight = imagePane.getHeight();

        int imgWidth = frame.width();
        int imgHeight = frame.height();

        double k;

        if((double) imgWidth/imgHeight > windowWidth/windowHeight)
        {
            k =  windowWidth/imgWidth;
        }else
        {
            k = windowHeight/imgHeight;
        }

        double newWidth = (int) (imgWidth * k);
        double newHeight = (int) (imgHeight * k);

        // Сдвиг не работает пока в билдере нулевая привязка к краям контейнера
        double imageX = 0;

        if(newWidth < windowWidth)
        {
            imageX = (windowWidth - newWidth) / 2;
        }

        imageView.setLayoutX(imageX);
        Imgproc.resize(frame, frame, new Size(newWidth, newHeight));

        // Draw
        MatOfByte _buffer = new MatOfByte();

        Imgcodecs.imencode(".jpeg", frame, _buffer);

        ByteArrayInputStream is = new ByteArrayInputStream(_buffer.toArray());

        Image img = new Image(is);

        imageView.setImage(img);

        is.reset();
    }


}
