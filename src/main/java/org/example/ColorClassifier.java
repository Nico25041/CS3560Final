package org.example;

import org.opencv.core.Scalar;

/** Hard-wired HSV ranges. Tune once under your room lighting. */
public final class ColorClassifier {

    public static CubeColor classify(Scalar hsv) {
        double h = hsv.val[0], s = hsv.val[1], v = hsv.val[2];

        if (s < 50 && v > 180)                       return CubeColor.WHITE;
        if (inRange(h, 20, 30) && s > 150 && v > 150) return CubeColor.YELLOW;
        if (inRange(h, 100, 130) && s > 150)          return CubeColor.BLUE;
        if (inRange(h, 40, 85) && s > 100)            return CubeColor.GREEN;
        if (inRange(h,   5, 25) && s > 150 && v > 150)return CubeColor.ORANGE;
        if ((h < 5 || h > 170) && s > 150 && v >  70) return CubeColor.RED;
        return CubeColor.UNKNOWN;
    }

    private static boolean inRange(double h, int lo, int hi) {
        return h >= lo && h <= hi;
    }

    private ColorClassifier() {}
}

