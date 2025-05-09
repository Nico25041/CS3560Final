package org.example;

import com.jogamp.opengl.GL2;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class Cubie {

    private final Matrix3f matrix;
    private final Vector3f translation;
    private int x, y, z;
    private final org.example.Face[] faces;

    public Cubie(Matrix3f m, Vector3f t, int x, int y, int z) {
        this.matrix = m;
        this.translation = t;
        this.x = x; this.y = y; this.z = z;
        this.faces = createFaces();
    }

    public Face[] getFaces() { return faces; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    private Face[] createFaces() {
        return new Face[] {
                new Face(new Vector3f(0, 0, -1), new float[]{0f, 0f, 1f}),   // Blue
                new Face(new Vector3f(0, 0, 1),  new float[]{0f, 1f, 0f}),   // Green
                new Face(new Vector3f(0, 1, 0),  new float[]{1f, 1f, 1f}),   // White
                new Face(new Vector3f(0, -1, 0), new float[]{1f, 1f, 0f}),   // Yellow
                new Face(new Vector3f(1, 0, 0),  new float[]{1f, 0.6f, 0f}), // Orange
                new Face(new Vector3f(-1, 0, 0), new float[]{1f, 0f, 0f})    // Red
        };
    }

    public void update(int x, int y, int z) {
        this.x = x; this.y = y; this.z = z;
        matrix.setIdentity();
        translation.set(x, y, z);
    }

    public void turnFaceX(int dir) { rotateFaces(dir, 'x'); }
    public void turnFaceY(int dir) { rotateFaces(dir, 'y'); }
    public void turnFaceZ(int dir) { rotateFaces(dir, 'z'); }

    private void rotateFaces(int dir, char axis) {
        float a = dir * (float) Math.PI / 2;
        for (Face f : faces) switch (axis) {
            case 'x' -> f.turnX(a);
            case 'y' -> f.turnY(a);
            case 'z' -> f.turnZ(a);
        }
    }

    public void show(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(translation.x, translation.y, translation.z);

        float[] m = {
                matrix.m00, matrix.m01, matrix.m02, 0f,
                matrix.m10, matrix.m11, matrix.m12, 0f,
                matrix.m20, matrix.m21, matrix.m22, 0f,
                0f,          0f,         0f,        1f
        };
        gl.glMultMatrixf(m, 0);

        for (Face f : faces) {
            f.show(gl);
        }

        gl.glColor3f(0f, 0f, 0f);
        float s = 0.5f;

        gl.glBegin(GL2.GL_LINES);
        // front square
        gl.glVertex3f(-s, -s,  s); gl.glVertex3f( s, -s,  s);
        gl.glVertex3f( s, -s,  s); gl.glVertex3f( s,  s,  s);
        gl.glVertex3f( s,  s,  s); gl.glVertex3f(-s,  s,  s);
        gl.glVertex3f(-s,  s,  s); gl.glVertex3f(-s, -s,  s);
        // back square
        gl.glVertex3f(-s, -s, -s); gl.glVertex3f( s, -s, -s);
        gl.glVertex3f( s, -s, -s); gl.glVertex3f( s,  s, -s);
        gl.glVertex3f( s,  s, -s); gl.glVertex3f(-s,  s, -s);
        gl.glVertex3f(-s,  s, -s); gl.glVertex3f(-s, -s, -s);
        // connecting edges
        gl.glVertex3f(-s, -s,  s); gl.glVertex3f(-s, -s, -s);
        gl.glVertex3f( s, -s,  s); gl.glVertex3f( s, -s, -s);
        gl.glVertex3f( s,  s,  s); gl.glVertex3f( s,  s, -s);
        gl.glVertex3f(-s,  s,  s); gl.glVertex3f(-s,  s, -s);
        gl.glEnd();

        gl.glPopMatrix();
    }


}

