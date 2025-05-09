package org.example;

import java.util.Arrays;

/** Faces ordered U R F D L B (standard solver notation). */
public class CubeState {

    private final CubeColor[][] faces = new CubeColor[6][9];
    private int nextFace = 0;

    public boolean recordFace(CubeColor[] nine) {
        if (nine == null || nine.length != 9) return false;
        faces[nextFace] = Arrays.copyOf(nine, 9);
        nextFace++;
        return nextFace == 6;   // returns true when cube is fully mapped
    }

    public CubeColor[][] getFaces() { return faces; }
}

