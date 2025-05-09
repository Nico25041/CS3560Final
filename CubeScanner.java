package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

public class CubeScanner implements AutoCloseable {

    static { OpenCV.loadLocally(); }

    private final VideoCapture cam = new VideoCapture(0);
    private final List<Rect> roi = new ArrayList<>();

    public CubeScanner() {
        if (!cam.isOpened()) throw new IllegalStateException("Camera not found");

        // grab one frame to learn the true resolution
        Mat probe = new Mat();
        if (!cam.read(probe) || probe.empty())
            throw new IllegalStateException("Cannot read from camera");

        int w = probe.cols();
        int h = probe.rows();
        probe.release();

        /*
         * Build a centred 3×3 grid.
         * step is the sticker square size; we leave equal margins around.
         */
        int step = Math.min(w, h) / 6;               // generous margin
        int x0  = (w - 3 * step) / 2;
        int y0  = (h - 3 * step) / 2;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                roi.add(new Rect(x0 + c * step,
                        y0 + r * step,
                        step, step));
    }

    /** Returns nine colours in row-major order or null on failure. */
    public CubeColor[] captureFace() {
        Mat frame = new Mat();
        if (!cam.read(frame) || frame.empty()) {
            System.out.println("Camera frame empty");
            return null;
        }
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);

        CubeColor[] out = new CubeColor[9];
        int i = 0;
        for (Rect r : roi) {
            Mat sub = frame.submat(r);
            Scalar hsv = Core.mean(sub);
            out[i++] = ColorClassifier.classify(hsv);
            sub.release();
        }
        frame.release();
        return out;
    }

    @Override public void close() { cam.release(); }
}

