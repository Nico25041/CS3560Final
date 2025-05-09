package org.example;

import com.jogamp.opengl.GL2;
import javax.vecmath.Vector3f;

public class Face {

    private Vector3f normal;
    private float[] color;

    public Face(Vector3f n, float[] c) {
        this.normal = new Vector3f(n);
        this.color = c;
    }

    // rotation helpers
    public void turnZ(float a) { normal = rotate(normal, a, 'z'); }
    public void turnY(float a) { normal = rotate(normal, a, 'y'); }
    public void turnX(float a) { normal = rotate(normal, a, 'x'); }

    private Vector3f rotate(Vector3f v, float a, char axis) {
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);
        Vector3f r = new Vector3f(v);

        switch (axis) {
            case 'z' -> { r.x = Math.round(v.x * c - v.y * s); r.y = Math.round(v.x * s + v.y * c); }
            case 'y' -> { r.x = Math.round(v.x * c - v.z * s); r.z = Math.round(v.x * s + v.z * c); }
            case 'x' -> { r.y = Math.round(v.y * c - v.z * s); r.z = Math.round(v.y * s + v.z * c); }
        }
        return r;
    }

    // accessors for scanner
    public Vector3f getNormal() { return new Vector3f(normal); }
    public void setColor(float[] rgb) { this.color = rgb; }
    public float[] getColor() { return color; }


    public void show(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(0.5f * normal.x, 0.5f * normal.y, 0.5f * normal.z);

        if (Math.abs(normal.y) > 0)      gl.glRotatef(90, 1, 0, 0);
        else if (Math.abs(normal.x) > 0) gl.glRotatef(90, 0, 1, 0);
        else                              gl.glRotatef(90, 0, 0, 1);

        gl.glColor3fv(color, 0);
        gl.glBegin(GL2.GL_QUADS);
        float s = 0.5f;
        gl.glVertex3f(-s, -s, 0);
        gl.glVertex3f( s, -s, 0);
        gl.glVertex3f( s,  s, 0);
        gl.glVertex3f(-s,  s, 0);
        gl.glEnd();

        gl.glPopMatrix();
    }
}

