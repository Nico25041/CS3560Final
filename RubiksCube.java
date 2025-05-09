package org.example;

import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.FPSAnimator;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Interactive Rubik’s Cube puzzle with standard 6-color faces, each with 9 minifaces. */
public class RubiksCube implements GLEventListener, KeyListener, MouseListener {

    // ---------- static ----------
    private static final int DIM = 3;
    private static RubiksCube instance; // For JavaFX to access JOGL

    // ---------- scene objects ----------
    private final Cubie[] cube = new Cubie[DIM * DIM * DIM];
    private boolean isSolved = false;
    private final GLUT glut = new GLUT(); // GLUT instance for text rendering

    // ---------- camera ----------
    private float camAngleX = 30;
    private float camAngleY = 45;
    private float camDist = 5;
    private int lastX, lastY;

    private final GLU glu = new GLU();

    // ---------- window refs ----------
    private GLWindow window;
    private int winW, winH;

    // ---------- scramble state ----------
    private String difficulty;
    private final Random random = new Random();

    // Back button dimensions
    private static final float BUTTON_X = 10;
    private static final float BUTTON_Y = 10;
    private static final float BUTTON_WIDTH = 80;
    private static final float BUTTON_HEIGHT = 40;

    // JavaFX Application for UI
    public static class RubiksCubeApp extends Application {
        private static Stage currentStage; // Track the current stage

        @Override
        public void start(Stage stage) {
            currentStage = stage;
            showHomeScreen();
        }

        private void showHomeScreen() {
            Text title = new Text("Rubik’s Cube Puzzle");
            title.setFont(Font.font("Arial", 36));

            Button startButton = new Button("Start");
            startButton.setStyle("-fx-font-size: 20; -fx-padding: 10 20;");
            startButton.setOnAction(e -> showDifficultyScreen());

            VBox homeLayout = new VBox(20, title, startButton);
            homeLayout.setAlignment(Pos.CENTER);
            Scene homeScene = new Scene(homeLayout, 400, 300);

            currentStage.setTitle("Rubik’s Cube Puzzle");
            currentStage.setScene(homeScene);
            currentStage.show();
        }

        private void showDifficultyScreen() {
            Text title = new Text("Select Difficulty");
            title.setFont(Font.font("Arial", 28));

            Button beginnerButton = new Button("Beginner");
            beginnerButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20;");
            beginnerButton.setOnAction(e -> startGame("Beginner"));

            Button intermediateButton = new Button("Intermediate");
            intermediateButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20;");
            intermediateButton.setOnAction(e -> startGame("Intermediate"));

            Button hardButton = new Button("Hard");
            hardButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20;");
            hardButton.setOnAction(e -> startGame("Hard"));

            VBox difficultyLayout = new VBox(20, title, beginnerButton, intermediateButton, hardButton);
            difficultyLayout.setAlignment(Pos.CENTER);
            Scene difficultyScene = new Scene(difficultyLayout, 400, 300);

            currentStage.setScene(difficultyScene);
            currentStage.show();
        }

        private void startGame(String difficulty) {
            System.out.println("Starting game with difficulty: " + difficulty);
            currentStage.hide(); // Hide the current JavaFX stage
            instance = new RubiksCube(); // Create new instance for fresh game
            instance.difficulty = difficulty;
            instance.initGLWindow();
        }

        // Static method to open a new difficulty screen
        public static void openNewDifficultyScreen() {
            Platform.runLater(() -> {
                // Close the previous stage if it exists
                if (currentStage != null) {
                    currentStage.close();
                }
                // Create a new stage for the difficulty screen
                currentStage = new Stage();
                currentStage.setTitle("Rubik’s Cube Puzzle");
                System.out.println("Opening new difficulty screen in new stage");
                new RubiksCubeApp().showDifficultyScreen();
            });
        }
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) {
        instance = new RubiksCube();
        Application.launch(RubiksCubeApp.class, args);
    }

    private void initGLWindow() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        window = GLWindow.create(caps);

        window.setTitle("Rubik’s Cube Puzzle - " + difficulty);
        window.setSize(800, 600);
        window.addGLEventListener(this);
        window.addKeyListener(this);
        window.addMouseListener(this);
        window.setVisible(true);

        new FPSAnimator(window, 60).start();
    }

    // ------------------------------------------------------------------
    // OpenGL lifecycle
    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        gl.glClearColor(0.98f, 0.98f, 0.98f, 1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // Initialize cube with standard Rubik’s Cube colors
        int i = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Matrix3f m = new Matrix3f();
                    m.setIdentity();
                    cube[i++] = new Cubie(m, new Vector3f(x, y, z), x, y, z);
                }
            }
        }

        // Apply scramble based on difficulty
        applyScramble();
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
        winW = w;
        winH = h;
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

        // 3D cube
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

        // 2D overlay for UI
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, winW, 0, winH, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        // Disable depth testing for 2D overlay
        gl.glDisable(GL.GL_DEPTH_TEST);

        // Draw back button
        gl.glColor3f(0.2f, 0.2f, 0.2f); // Dark gray background
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(BUTTON_X, winH - BUTTON_Y - BUTTON_HEIGHT);
        gl.glVertex2f(BUTTON_X + BUTTON_WIDTH, winH - BUTTON_Y - BUTTON_HEIGHT);
        gl.glVertex2f(BUTTON_X + BUTTON_WIDTH, winH - BUTTON_Y);
        gl.glVertex2f(BUTTON_X, winH - BUTTON_Y);
        gl.glEnd();
        gl.glColor3f(1, 1, 1); // White text
        gl.glRasterPos2f(BUTTON_X + 20, winH - BUTTON_Y - 15);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "Back");

        // Draw SOLVED!!! message if solved
        if (isSolved) {
            System.out.println("Rendering SOLVED!!! message at (" + (winW/2 - 80) + ", " + winH/2 + ")");
            gl.glColor4f(0, 0, 0, 0.5f); // Semi-transparent black background
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex2f(winW/4, winH/4);
            gl.glVertex2f(winW*3/4, winH/4);
            gl.glVertex2f(winW*3/4, winH*3/4);
            gl.glVertex2f(winW/4, winH*3/4);
            gl.glEnd();
            gl.glColor3f(1, 0, 0); // Red text
            gl.glRasterPos2f(winW/2 - 80, winH/2); // Adjusted for better centering
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "SOLVED!!!");
        }

        // Re-enable depth testing for 3D rendering
        gl.glEnable(GL.GL_DEPTH_TEST);

        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void dispose(GLAutoDrawable d) {
    }

    // ------------------------------------------------------------------
    // Mouse interaction (for camera rotation and button clicks)
    @Override
    public void mousePressed(MouseEvent e) {
        lastX = e.getX();
        lastY = winH - e.getY();
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

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = winH - e.getY(); // Flip Y-coordinate to match OpenGL
        // Check if click is within back button bounds
        if (x >= BUTTON_X && x <= BUTTON_X + BUTTON_WIDTH &&
                y >= winH - BUTTON_Y - BUTTON_HEIGHT && y <= winH - BUTTON_Y) {
            System.out.println("Back button clicked, opening new difficulty screen");
            window.destroy(); // Close JOGL window
            RubiksCubeApp.openNewDifficultyScreen(); // Open new difficulty screen
        }
    }

    // Other MouseListener methods
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    public void mouseWheelMoved(MouseEvent e, boolean[] wheelRotation) {}

    // ------------------------------------------------------------------
    // Keyboard controls
    @Override
    public void keyPressed(KeyEvent e) {
        if (isSolved) return; // No moves after solved

        char key = Character.toLowerCase(e.getKeyChar());
        boolean isUpperCase = Character.isUpperCase(e.getKeyChar());
        int dir = isUpperCase ? -1 : 1;

        switch (key) {
            case 'u':
                turnY(1, dir);
                break;
            case 'd':
                turnY(-1, -dir);
                break;
            case 'r':
                turnX(1, dir);
                break;
            case 'l':
                turnX(-1, -dir);
                break;
            case 'f':
                turnZ(1, dir);
                break;
            case 'b':
                turnZ(-1, -dir);
                break;
            case 'm':
                turnMiddleX(dir);
                break;
            case 'n':
                turnMiddleY(dir);
                break;
            case 'k':
                turnMiddleZ(dir);
                break;
        }
        checkSolved();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    // ------------------------------------------------------------------
    // Cube manipulation
    private void turnX(int idx, int dir) {
        for (Cubie c : cube) {
            if (c.getX() == idx) {
                int y = c.getY(), z = c.getZ();
                if (dir == 1) {
                    c.update(idx, -z, y);
                    c.turnFaceX(1);
                } else {
                    c.update(idx, z, -y);
                    c.turnFaceX(-1);
                }
            }
        }
    }

    private void turnY(int idx, int dir) {
        for (Cubie c : cube) {
            if (c.getY() == idx) {
                int x = c.getX(), z = c.getZ();
                if (dir == 1) {
                    c.update(z, idx, -x);
                    c.turnFaceY(1);
                } else {
                    c.update(-z, idx, x);
                    c.turnFaceY(-1);
                }
            }
        }
    }

    private void turnZ(int idx, int dir) {
        for (Cubie c : cube) {
            if (c.getZ() == idx) {
                int x = c.getX(), y = c.getY();
                if (dir == 1) {
                    c.update(-y, x, idx);
                    c.turnFaceZ(1);
                } else {
                    c.update(y, -x, idx);
                    c.turnFaceZ(-1);
                }
            }
        }
    }

    // Middle layer turns
    private void turnMiddleX(int dir) {
        for (Cubie c : cube) {
            if (c.getX() == 0) {
                int y = c.getY(), z = c.getZ();
                if (dir == 1) {
                    c.update(0, -z, y);
                    c.turnFaceX(1);
                } else {
                    c.update(0, z, -y);
                    c.turnFaceX(-1);
                }
            }
        }
    }

    private void turnMiddleY(int dir) {
        for (Cubie c : cube) {
            if (c.getY() == 0) {
                int x = c.getX(), z = c.getZ();
                if (dir == 1) {
                    c.update(z, 0, -x);
                    c.turnFaceY(1);
                } else {
                    c.update(-z, 0, x);
                    c.turnFaceY(-1);
                }
            }
        }
    }

    private void turnMiddleZ(int dir) {
        for (Cubie c : cube) {
            if (c.getZ() == 0) {
                int x = c.getX(), y = c.getY();
                if (dir == 1) {
                    c.update(-y, x, 0);
                    c.turnFaceZ(1);
                } else {
                    c.update(y, -x, 0);
                    c.turnFaceZ(-1);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Scramble generation
    private void applyScramble() {
        int moveCount = switch (difficulty) {
            case "Beginner" -> 2;    // Very easy: 2 moves
            case "Intermediate" -> 15; // Slightly harder: 15 moves
            case "Hard" -> 25;       // Fully scrambled: 25 moves
            default -> 15;
        };

        String[] moves = {"U", "U'", "D", "D'", "R", "R'", "L", "L'", "F", "F'", "B", "B'"};
        List<String> scramble = new ArrayList<>();
        String lastFace = "";
        String secondLastFace = ""; // For Beginner to avoid redundant moves

        for (int i = 0; i < moveCount; i++) {
            String move;
            String face;
            do {
                move = moves[random.nextInt(moves.length)];
                face = move.substring(0, 1);
                // For Beginner, avoid moves that undo each other (e.g., U then U')
                if (difficulty.equals("Beginner") && i == 1) {
                    String lastMove = scramble.get(0);
                    if ((move.equals("U") && lastMove.equals("U'")) ||
                            (move.equals("U'") && lastMove.equals("U")) ||
                            (move.equals("D") && lastMove.equals("D'")) ||
                            (move.equals("D'") && lastMove.equals("D")) ||
                            (move.equals("R") && lastMove.equals("R'")) ||
                            (move.equals("R'") && lastMove.equals("R")) ||
                            (move.equals("L") && lastMove.equals("L'")) ||
                            (move.equals("L'") && lastMove.equals("L")) ||
                            (move.equals("F") && lastMove.equals("F'")) ||
                            (move.equals("F'") && lastMove.equals("F")) ||
                            (move.equals("B") && lastMove.equals("B'")) ||
                            (move.equals("B'") && lastMove.equals("B"))) {
                        continue;
                    }
                }
            } while (face.equals(lastFace) || (i > 1 && face.equals(secondLastFace))); // Avoid consecutive same-face moves
            scramble.add(move);
            secondLastFace = lastFace;
            lastFace = face;
        }

        for (String move : scramble) {
            switch (move) {
                case "U" -> turnY(1, 1);
                case "U'" -> turnY(1, -1);
                case "D" -> turnY(-1, -1);
                case "D'" -> turnY(-1, 1);
                case "R" -> turnX(1, 1);
                case "R'" -> turnX(1, -1);
                case "L" -> turnX(-1, -1);
                case "L'" -> turnX(-1, 1);
                case "F" -> turnZ(1, 1);
                case "F'" -> turnZ(1, -1);
                case "B" -> turnZ(-1, -1);
                case "B'" -> turnZ(-1, 1);
            }
        }
    }

    // ------------------------------------------------------------------
    // Solve detection
    private void checkSolved() {
        // Define faces by cubie positions and expected colors
        record FaceCheck(int coord, char axis, Vector3f normal, CubeColor expectedColor) {}
        FaceCheck[] faces = {
                new FaceCheck(1, 'y', new Vector3f(0, 1, 0), CubeColor.YELLOW),  // U
                new FaceCheck(-1, 'y', new Vector3f(0, -1, 0), CubeColor.WHITE), // D
                new FaceCheck(1, 'x', new Vector3f(1, 0, 0), CubeColor.RED),     // R
                new FaceCheck(-1, 'x', new Vector3f(-1, 0, 0), CubeColor.ORANGE),// L
                new FaceCheck(1, 'z', new Vector3f(0, 0, 1), CubeColor.GREEN),   // F
                new FaceCheck(-1, 'z', new Vector3f(0, 0, -1), CubeColor.BLUE)   // B
        };

        for (FaceCheck face : faces) {
            List<CubeColor> colors = new ArrayList<>();
            int matchingColors = 0;

            // Find cubies on this face
            for (Cubie c : cube) {
                boolean onFace = switch (face.axis) {
                    case 'x' -> c.getX() == face.coord;
                    case 'y' -> c.getY() == face.coord;
                    case 'z' -> c.getZ() == face.coord;
                    default -> false;
                };
                if (onFace) {
                    // Find the miniface with the matching normal
                    for (Face f : c.getFaces()) {
                        Vector3f n = f.getNormal();
                        if (Math.abs(n.x - face.normal.x) < 0.01 &&
                                Math.abs(n.y - face.normal.y) < 0.01 &&
                                Math.abs(n.z - face.normal.z) < 0.01) {
                            CubeColor color = getColorFromRGB(f.getColor());
                            colors.add(color);
                            if (color == face.expectedColor) {
                                matchingColors++;
                            }
                            break;
                        }
                    }
                }
            }

            // Debug: Log colors on this face
            String faceName = switch (face.expectedColor) {
                case YELLOW -> "Up";
                case WHITE -> "Down";
                case RED -> "Right";
                case ORANGE -> "Left";
                case GREEN -> "Front";
                case BLUE -> "Back";
                default -> "Unknown";
            };
            System.out.println("Face " + faceName + ": " + colors + ", Matching: " + matchingColors + "/9");

            if (matchingColors != 9) {
                isSolved = false;
                return;
            }
        }
        isSolved = true;
        System.out.println("Cube solved!");
    }

    private CubeColor getColorFromRGB(float[] rgb) {
        for (CubeColor color : CubeColor.values()) {
            float[] colorRGB = color.rgb();
            if (Math.abs(colorRGB[0] - rgb[0]) < 0.01 &&
                    Math.abs(colorRGB[1] - rgb[1]) < 0.01 &&
                    Math.abs(colorRGB[2] - rgb[2]) < 0.01) {
                return color;
            }
        }
        return CubeColor.UNKNOWN;
    }
}