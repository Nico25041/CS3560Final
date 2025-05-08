package org.example;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import javax.vecmath.Vector3f;

public final class RayPicker {

    private static final GLU glu = new GLU();

    /** return the sticker Face under the cursor or null */
    public static Face pick(int mx, int my, int wndW, int wndH,
                            GL2 gl, Cubie[] cube) {

        // --- grab matrices (double-precision variant) ---
        double[] model = new double[16];
        double[] proj  = new double[16];
        int[]    view  = { 0, 0, wndW, wndH };

        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX,  model, 0);
        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, proj,  0);

        // --- build world-space ray ---
        double[] p0 = new double[4];
        double[] p1 = new double[4];

        glu.gluUnProject((double) mx, (double) my, 0.0,
                model, 0, proj, 0, view, 0, p0, 0);
        glu.gluUnProject((double) mx, (double) my, 1.0,
                model, 0, proj, 0, view, 0, p1, 0);

        Vector3f from = new Vector3f((float) p0[0], (float) p0[1], (float) p0[2]);
        Vector3f dir  = new Vector3f((float) (p1[0] - p0[0]),
                (float) (p1[1] - p0[1]),
                (float) (p1[2] - p0[2]));
        dir.normalize();

        // --- intersect every sticker quad, keep nearest ---
        Face best = null;
        float bestT = Float.MAX_VALUE;

        for (Cubie c : cube) {
            for (Face f : c.getFaces()) {
                Vector3f n = f.getNormal();
                Vector3f p = new Vector3f(c.getX() + 0.5f * n.x,
                        c.getY() + 0.5f * n.y,
                        c.getZ() + 0.5f * n.z);

                float denom = n.x * dir.x + n.y * dir.y + n.z * dir.z;
                if (Math.abs(denom) < 1e-3f) continue;

                float t = (n.x * (p.x - from.x) +
                        n.y * (p.y - from.y) +
                        n.z * (p.z - from.z)) / denom;
                if (t < 0 || t > bestT) continue;

                Vector3f hit = new Vector3f(dir);
                hit.scale(t); hit.add(from);

                float dx = Math.abs(hit.x - p.x);
                float dy = Math.abs(hit.y - p.y);
                float dz = Math.abs(hit.z - p.z);
                if (dx <= 0.5f && dy <= 0.5f && dz <= 0.5f) {
                    bestT = t;
                    best  = f;
                }
            }
        }
        return best;
    }

    private RayPicker() {}
}
