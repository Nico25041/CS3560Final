package org.example;

public enum CubeColor {
    WHITE, YELLOW, RED, ORANGE, BLUE, GREEN, UNKNOWN;

    /** Returns the display color for JOGL. */
    public float[] rgb() {
        return switch (this) {
            case WHITE -> new float[]{1f, 1f, 1f};
            case YELLOW -> new float[]{1f, 1f, 0f};
            case RED -> new float[]{1f, 0f, 0f};
            case ORANGE -> new float[]{1f, 0.6f, 0f};
            case BLUE -> new float[]{0f, 0f, 1f};
            case GREEN -> new float[]{0f, 1f, 0f};
            default -> new float[]{0.2f, 0.2f, 0.2f};
        };
    }
}

