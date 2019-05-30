package ru.roboticsnt.visitorCounter;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CameraController
{

    private boolean _isStarted = false;

    private VideoCapture _camera;
    private FrameListener _listener;

    private final String _PROTO = "/home/user/Projects/intellij/OpenCVTestsProjects/resources/models/caffe/object-detection-deep-learning/MobileNetSSD_deploy.prototxt.txt";
    private final String _MODEL = "/home/user/Projects/intellij/OpenCVTestsProjects/resources/models/caffe/object-detection-deep-learning/MobileNetSSD_deploy.caffemodel";
    private final int _BLOB_SIZE = 300;

    private final double _SCALE_FACTOR = 0.007843;
    private final Scalar _MEAN = new Scalar(127.5);

    private Net _net;
    private String[] _objectClasses;

    private final int _CAMERA_PATH = 1;

    private final int _CAMERA_FRAME_WIDTH = 1920;
    private final int _CAMERA_FRAME_HEIGHT = 1080;

    public static final int FRAME_SIZE = 1080;

    private Rect _cropRect;


    public CameraController()
    {
        int cropStartX = (_CAMERA_FRAME_WIDTH - FRAME_SIZE) / 2;
        _cropRect = new Rect(cropStartX, 0, FRAME_SIZE, FRAME_SIZE);
    }


    public void setOnFrameListener(FrameListener listener)
    {
        _listener = listener;
    }


    public boolean isStarted()
    {
        return _isStarted;
    }


    public void start()
    {
        _net = Dnn.readNetFromCaffe(_PROTO, _MODEL);

        _camera = new VideoCapture(_CAMERA_PATH);

        _camera.set(Videoio.CAP_PROP_FRAME_WIDTH, _CAMERA_FRAME_WIDTH);
        _camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, _CAMERA_FRAME_HEIGHT);

        _isStarted = true;

        new Thread(() ->
        {
            while (_isStarted)
            {
                if(!_camera.isOpened())
                {
                    continue;
                }

                Mat frame = new Mat();

                _camera.read(frame);

                if(frame.empty())
                {
                    continue;
                }

                frame = cropToRectangle(frame);

                Mat detectionResult = detectObjectsOnFrame(frame);

                if (_listener != null)
                {
                    _listener.onFrame(frame, detectionResult);
                }

                detectionResult.release();
                frame.release();
            }
        }).start();
    }


    public void stop()
    {
        _isStarted = false;

        _camera.release();
        _net = null;
        _camera = null;
    }


    public interface FrameListener
    {
        void onFrame(Mat frame, Mat detectionResult);
    }


    private Mat detectObjectsOnFrame(Mat frame)
    {
        Mat inputBlob = Dnn.blobFromImage(frame, _SCALE_FACTOR, new Size(_BLOB_SIZE, _BLOB_SIZE), _MEAN, false, true);

        _net.setInput(inputBlob);

        Mat detectResult = _net.forward();

        detectResult = detectResult.reshape(1, (int) (detectResult.total()/7));

        return detectResult;
    }


    private Mat cropToRectangle(Mat frame)
    {
        return new Mat(frame, _cropRect);
    }

}
