package org.example;

import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.ArrayDeque;
import java.util.Queue;

/** Manual Rubik’s-cube painter with step-by-step solve playback. */
public class RubiksCube implements GLEventListener,
        KeyListener,
        MouseListener {

    // ---------- static ----------
    private static final int DIM = 3;

    // ---------- scene objects ----------
    private final Cubie[] cube   = new Cubie[DIM * DIM * DIM];
    private final Palette palette = new Palette();

    // ---------- camera ----------
    private float camAngleX = 30;
    private float camAngleY = 45;
    private float camDist   = 5;
    private int   lastX, lastY;

    private final GLU glu = new GLU();

    // ---------- mapping / solving state ----------
    private int lockedFaces = 0;
    private final Queue<String> solution = new ArrayDeque<>();
    private boolean animating = false;
    private long lastTurn = 0;

    // ---------- window refs ----------
    private GLWindow window;
    private int winW, winH;

    // ------------------------------------------------------------------
    public static void main(String[] args) {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        GLWindow win = GLWindow.create(caps);

        RubiksCube app = new RubiksCube();
        app.window = win;

        win.setTitle("Rubik Cube");
        win.setSize(800, 600);
        win.addGLEventListener(app);
        win.addKeyListener(app);
        win.addMouseListener(app);
        win.setVisible(true);

        new FPSAnimator(win, 60).start();
    }

    // ------------------------------------------------------------------
    // OpenGL lifecycle
    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        gl.glClearColor(0.98f, 0.98f, 0.98f, 1f);
        gl.glEnable(GL.GL_DEPTH_TEST);

        int i = 0;
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++)
                for (int z = -1; z <= 1; z++) {
                    Matrix3f m = new Matrix3f(); m.setIdentity();
                    cube[i++] = new Cubie(m, new Vector3f(x, y, z), x, y, z);
                }
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
        winW = w;
        winH = h;
        palette.build(w, h);                             // updated call

        GL2 gl = d.getGL().getGL2();
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, (float) w / h, 0.1, 100);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // 3-D cube -----------------------------------------------------
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(camDist * Math.sin(Math.toRadians(camAngleY))
                        * Math.cos(Math.toRadians(camAngleX)),
                camDist * Math.sin(Math.toRadians(camAngleX)),
                camDist * Math.cos(Math.toRadians(camAngleY))
                        * Math.cos(Math.toRadians(camAngleX)),
                0, 0, 0,
                0, 1, 0);
        gl.glScalef(0.5f, 0.5f, 0.5f);
        for (Cubie c : cube) c.show(gl);

        // palette overlay ---------------------------------------------
        palette.draw(gl, winW, winH);                    // updated call

        // solve playback ----------------------------------------------
        if (animating && System.currentTimeMillis() - lastTurn > 600) advanceMove();
    }

    @Override public void dispose(GLAutoDrawable d) {}

    // ------------------------------------------------------------------
    // mouse interaction
    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = winH - e.getY();
        lastX = mx;
        lastY = my;

        if (palette.click(mx, my)) return;               // hit swatch

        // run picking on GL thread
        window.invoke(false, drawable -> {
            GL2 gl = drawable.getGL().getGL2();
            Face hit = RayPicker.pick(mx, my, winW, winH, gl, cube);
            if (hit != null && lockedFaces < 6) {
                hit.setColor(palette.current().rgb());
            }
            return true;
        });
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int mx = e.getX();
        int my = winH - e.getY();
        int dx = mx - lastX;
        int dy = my - lastY;
        camAngleY += dx * 0.5f;
        camAngleX -= dy * 0.5f;
        lastX = mx;
        lastY = my;
    }

    // unused callbacks
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseMoved   (MouseEvent e) {}
    @Override public void mouseClicked (com.jogamp.newt.event.MouseEvent e) {}
    @Override public void mouseEntered (MouseEvent e) {}
    @Override public void mouseExited  (MouseEvent e) {}
    @Override public void mouseWheelMoved(MouseEvent e) {}

    // ------------------------------------------------------------------
    // keyboard
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER -> lockFace();          // finish a side
            case KeyEvent.VK_SPACE -> { if (animating) advanceMove(); }
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    public void keyTyped   (KeyEvent e) {}

    // ------------------------------------------------------------------
    // mapping and solving
    private void lockFace() {
        lockedFaces++;
        if (lockedFaces == 6) startSolver();
        else rotateCubeForNext();
    }

    private void rotateCubeForNext() { for (Cubie c : cube) c.turnFaceY(1); }

    private void startSolver() {
        // TODO: integrate two-phase solver; placeholder sequence
        solution.add("R");
        solution.add("U");
        solution.add("R'");
        animating = true;
        lastTurn = System.currentTimeMillis();
    }

    private void advanceMove() {
        if (solution.isEmpty()) {
            animating = false;
            return;
        }
        String mv = solution.poll();
        switch (mv) {
            case "R"  -> turnX( 1,  1);
            case "R'" -> turnX( 1, -1);
            case "U"  -> turnY( 1,  1);
            case "U'" -> turnY( 1, -1);
        }
        lastTurn = System.currentTimeMillis();
    }

    // ------------------------------------------------------------------
    // cube rotation helpers
    private void turnX(int idx, int dir) {
        for (Cubie c : cube) if (c.getX() == idx) {
            int y = c.getY(), z = c.getZ();
            if (dir == 1) c.update(idx, -z, y); else c.update(idx,  z, -y);
            c.turnFaceX(dir);
        }
    }
    private void turnY(int idx, int dir) {
        for (Cubie c : cube) if (c.getY() == idx) {
            int x = c.getX(), z = c.getZ();
            if (dir == 1) c.update( z, idx, -x); else c.update(-z, idx,  x);
            c.turnFaceY(dir);
        }
    }
}

