package org.example;

import com.jogamp.opengl.GL2;

/** A clickable button to trigger the cube solver. */
public class SolveButton {

    private float[] rect;

    /** Build button rectangle on window resize */
    public void build(int winW, int winH) {
        rect = new float[] { winW - 110, winH - 50, 100, 40 }; // Positioned in top-right
    }

    /** Draw button in pixel space */
    public void draw(GL2 gl, int winW, int winH) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, winW, 0, winH, -1, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL2.GL_DEPTH_TEST);

        float x = rect[0], y = rect[1], w = rect[2], h = rect[3];

        // Draw button background
        gl.glColor3f(0.7f, 0.7f, 0.7f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + w, y);
        gl.glVertex2f(x + w, y + h);
        gl.glVertex2f(x, y + h);
        gl.glEnd();

        // Draw border
        gl.glColor3f(0f, 0f, 0f);
        gl.glLineWidth(2f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + w, y);
        gl.glVertex2f(x + w, y + h);
        gl.glVertex2f(x, y + h);
        gl.glEnd();

        gl.glEnable(GL2.GL_DEPTH_TEST);

        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    /** Check if button was clicked */
    public boolean click(int mx, int my) {
        return mx >= rect[0] && mx <= rect[0] + rect[2] &&
                my >= rect[1] && my <= rect[1] + rect[3];
    }
}