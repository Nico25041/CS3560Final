package org.example;

import com.jogamp.opengl.GL2;
import java.util.EnumMap;
import java.util.Map;

/** Sidebar with six cube-colour swatches. */
public class Palette {

    private final Map<CubeColor, float[]> rect = new EnumMap<>(CubeColor.class);
    private CubeColor current = CubeColor.RED;

    /** build rectangles once on window-resize */
    public void build(int winW, int winH) {
        rect.clear();
        int y = winH - 50;
        for (CubeColor c : CubeColor.values()) {
            if (c == CubeColor.UNKNOWN) continue;
            rect.put(c, new float[] { 10, y, 40, 40 });
            y -= 60;
        }
    }

    /** draw swatches in pixel space */
    public void draw(GL2 gl, int winW, int winH) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, winW, 0, winH, -1, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL2.GL_DEPTH_TEST);

        for (var e : rect.entrySet()) {
            CubeColor col = e.getKey();
            float[] r = e.getValue();
            float x = r[0], y = r[1], w = r[2], h = r[3];

            // fill
            gl.glColor3fv(col.rgb(), 0);
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex2f(x,     y    );
            gl.glVertex2f(x + w, y    );
            gl.glVertex2f(x + w, y + h);
            gl.glVertex2f(x,     y + h);
            gl.glEnd();

            // white border if selected
            if (col == current) {
                gl.glColor3f(1f, 1f, 1f);
                gl.glLineWidth(3f);
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glVertex2f(x,     y    );
                gl.glVertex2f(x + w, y    );
                gl.glVertex2f(x + w, y + h);
                gl.glVertex2f(x,     y + h);
                gl.glEnd();
            }
        }

        gl.glEnable(GL2.GL_DEPTH_TEST);

        gl.glPopMatrix();                 // MODELVIEW
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    /** true if a swatch was hit and current colour changed */
    public boolean click(int mx, int my) {
        for (var e : rect.entrySet()) {
            float[] r = e.getValue();
            if (mx >= r[0] && mx <= r[0] + r[2] &&
                    my >= r[1] && my <= r[1] + r[3]) {
                current = e.getKey();
                return true;
            }
        }
        return false;
    }

    public CubeColor current() { return current; }
}

